package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class QueryActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    private EditText editTextQuery;
    FloatingActionButton btnQuery;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;


    SQLiteDatabase db;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qurey);
        btnQuery = (FloatingActionButton) findViewById(R.id.activity_query_btnQuery);
        editTextQuery = (EditText) findViewById(R.id.query_editTextQuery);




        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Sheets API ...");

        db = new MyDBHelper(this).getWritableDatabase();

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnQuery.setEnabled(false);

                final String queryString = editTextQuery.getText().toString();
                for(int i = 0,count=0,next=-1; i<queryString.length();i++ ){

                    if(queryString.charAt(i)=='-'){
                        count++;
                        if(i-1>=0 && (queryString.charAt(i-1) == ','||queryString.charAt(i-1) == '\n'||queryString.charAt(i-1) == ' ')){
                            AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                            inputErrorDialog.setMessage("輸入錯誤! \n \"-\"符號兩邊必須是編號!").setTitle("錯誤").setPositiveButton("確定",null).show();
                            btnQuery.setEnabled(true);

                            return;
                        }else if(i-1<0){
                            AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                            inputErrorDialog.setMessage("輸入錯誤! \n\"-\"符號兩邊必須是編號!").setTitle("錯誤").setPositiveButton("確定",null).show();
                            btnQuery.setEnabled(true);
                            return;
                        }

                        if(i+1<queryString.length() && (queryString.charAt(i+1) == ','||queryString.charAt(i+1) == '\n'||queryString.charAt(i+1) == ' ')){
                            AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                            inputErrorDialog.setMessage("輸入錯誤!\n  \"-\"符號兩邊必須是編號!").setTitle("錯誤").setPositiveButton("確定",null).show();
                            btnQuery.setEnabled(true);
                            return;
                        }else if(i+1>=queryString.length()){
                            AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                            inputErrorDialog.setMessage("輸入錯誤!\n \"-\"符號兩邊必須是編號!").setTitle("錯誤").setPositiveButton("確定",null).show();
                            btnQuery.setEnabled(true);
                            return;
                        }
                    }else if(queryString.charAt(i)==',' ||queryString.charAt(i)==' ' || queryString.charAt(i)=='\n' )
                        count = 0;
                    if(count>1){
                        AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                        inputErrorDialog.setMessage("輸入錯誤!\n 不可連續出現兩次以上的\"-\"符號").setTitle("錯誤").setPositiveButton("確定",null).show();
                        btnQuery.setEnabled(true);
                        return;
                    }
                }

                if(!(queryString.length()>0&&queryString.split(" ").length>0)){
                    AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                    inputErrorDialog.setMessage("輸入錯誤!\n 未輸入任何資料!").setTitle("錯誤").setPositiveButton("確定",null).show();
                    btnQuery.setEnabled(true);
                    return;
                }

                final Intent intent = new Intent();
                intent.setClass(QueryActivity.this,InvestgationActivity.class);

                final ArrayList<String> qureyRange = new ArrayList<String>();

                for(String sPerLine:queryString.split("\n")){
                    for(String sBySpace:sPerLine.split(" ")){
                        for (String s: sBySpace.split(","))
                            qureyRange.add(s);
                    }
                }


                ArrayList<String> nonExistData;
                try {
                    nonExistData = MyDBHelper.findNonExistDatas(db,qureyRange);
                }catch (Exception e){
                    System.out.print(e.toString()+e.getMessage());
                    return;
                }


                if(nonExistData.size()<=0) {
                    try {
                        MyDBHelper.copyDatasIntoTemp(db, qureyRange);
                    } catch (Exception e) {
                        AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                        inputErrorDialog.setMessage("輸入錯誤!\n"+e.getMessage()).setTitle("錯誤").setPositiveButton("確定",null).show();
                        return;
                    }
                    startActivity(intent);
                }else{
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(QueryActivity.this);
                    builder1.setTitle("錯誤").setMessage("輸入的範圍包含以下不存在的編號:\n"+ TextUtils.join(",",nonExistData)+"\n請問是否先新增在繼續?")
                            .setNeutralButton("重新輸入",null)
                            .setNegativeButton("否，忽略", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try {
                                        MyDBHelper.copyDatasIntoTemp(db, qureyRange);
                                    } catch (Exception e) {
                                        AlertDialog.Builder inputErrorDialog = new AlertDialog.Builder(QueryActivity.this);
                                        inputErrorDialog.setMessage("輸入錯誤!\n"+e.getMessage()).setTitle("錯誤").setPositiveButton("確定",null).show();
                                        return;
                                    }
                                    startActivity(intent);
                                }
                            }).setPositiveButton("是,先新增", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent jumpToAddPage = new Intent();
                            jumpToAddPage.setClass(QueryActivity.this, AddHydrantActivity.class);
                            startActivity(jumpToAddPage);
                            QueryActivity.this.finish();
                        }
                    }).show();
                }

 
                btnQuery.setEnabled(true);
            }
        });


        hasPermission();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {

                Intent intent = new Intent();
                intent.setClass(this,ManualActivity.class);
                intent.putExtra(ManualActivity.ARG_MANUAL_TYPE,ManualActivity.MANUAL_TYPE_INVESITIGATION);
                startActivity(intent);
                return true;
            }

            return super.onOptionsItemSelected(item);

    }
    @Override
    protected void onResume(){
        super.onResume();
        checkUnSavedData();

    }

    private void checkUnSavedData(){
        Cursor cs = db.query(MyDBHelper.TABLE_TEMP,new String[]{MyDBHelper.COLUMN_ID},null,null,null,null,null);
        if(cs.getCount()>0){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(QueryActivity.this);
            builder1.setMessage("發現有上次未儲存的資料，\n請問是否繼續編輯?")
                    .setNeutralButton("納入這次的編輯", null)
                    .setNegativeButton("放棄資料", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.delete(MyDBHelper.TABLE_TEMP,null,null);
                        }
                    }).setPositiveButton("繼續編輯", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setClass(QueryActivity.this,InvestgationActivity.class);
                    startActivity(intent);
                }
            }).setCancelable(false).show();
        }
    }

    private boolean hasPermission(){
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setTitle("Error").setMessage("No Network").show();
        }else
            return true;
        mProgress.hide();
        return false;
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        Cursor cs = db.query(MyDBHelper.TABLE_ACCOUNT_NAME,new String[]{"*"},null,null,null,null,null);
        if(cs.getCount()<=0) {
            if (EasyPermissions.hasPermissions(
                    this, Manifest.permission.GET_ACCOUNTS)) {
                String accountName = getPreferences(Context.MODE_PRIVATE)
                        .getString(PREF_ACCOUNT_NAME, null);
                if (accountName != null) {
                    ContentValues cv =new ContentValues();
                    cv.put(MyDBHelper.COLUMN_ID,1);
                    cv.put(MyDBHelper.COLUMN_NAME,accountName);
                    db.insert(MyDBHelper.TABLE_ACCOUNT_NAME,null,cv);
                    mCredential.setSelectedAccountName(accountName);
                } else {
                    // Start a dialog from which the user can choose an account
                    startActivityForResult(
                            mCredential.newChooseAccountIntent(),
                            REQUEST_ACCOUNT_PICKER);
                }
            } else {
                // Request the GET_ACCOUNTS permission via a user dialog
                EasyPermissions.requestPermissions(
                        this,
                        "This app needs to access your Google account (via Contacts).",
                        100,
                        Manifest.permission.GET_ACCOUNTS);
            }
        }else{
            cs.moveToFirst();
            mCredential.setSelectedAccountName(cs.getString(cs.getColumnIndex(MyDBHelper.COLUMN_NAME)));
        }
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setMessage(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);

                    }
                }
                break;
            case REQUEST_AUTHORIZATION:

                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


    private String showAllDataInDB(){
        String s;
        Cursor cs = db.query(MyDBHelper.TABLE_LOG,new String[]{"*"},null,null,null,null,null);
        cs.moveToFirst();
        s = "cs.getColumnCount:"+cs.getColumnCount()+"\nLOG:\n";
        if(cs.getCount()>0){
            do{
                SimpleDateFormat df = new SimpleDateFormat("YYYY/MM/dd HH:mm");
                s+= cs.getString(0)+"\t"+cs.getString(1)+"\t"+cs.getString(2)+"\t"+df.format(cs.getLong(3))+"\n";
            }while (cs.moveToNext());
        }

        cs = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{"*"},null,null,null,null,null);
        cs.moveToFirst();
        s+="cs.getColumnCount:"+cs.getColumnCount()+" \nHYDRANT:\n";
        if(cs.getCount()>0){
            do{
                for(int i =0;i<GoogleSheetConnecter.TOTAL_COLUMN_COUNT;i++)
                    s+= cs.getString(i)+"\t";
                s+="\n";
            }while (cs.moveToNext());
        }

        cs = db.query(MyDBHelper.TABLE_TEMP,new String[]{"*"},null,null,null,null,null);
        cs.moveToFirst();
        s+="TEMP:\n";
        if(cs.getCount()>0){
            do{
                for(int i =0;i<GoogleSheetConnecter.TOTAL_COLUMN_COUNT;i++)
                    s+= cs.getString(i)+"\t";
                s+="\n";
            }while (cs.moveToNext());
        }

       return s;

    }



}
