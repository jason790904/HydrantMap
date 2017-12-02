package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.icu.text.CompactDecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.warkiz.widget.IndicatorSeekBar;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.renai.firedepartment.keelong.hydrantmap.InvestgationResultActivity.selectedColor;

public class InvestgationResultActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    ResultListAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    SQLiteDatabase db;
    ImageView btnConcern;
    ImageView btnCancel;

    public static final String BOOLEAN_ARG_ISINVESTGATION = "investigation";
    public static final String STRINGARRAY_ARG_RANGES = "ranges";
    public static final String BOOLEAN_IS_SEARCH = "search";
    public static final String WHERE_STRING = "whereString";

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;


    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    ProgressDialog progressDialog;

    GooglePlayService checkServices;
    ArrayList<String> ranges;
    String whereString;
    boolean isInvestigation;
    boolean isSearch;
    boolean itemPicking = false;
    boolean itemRecovering = false;

    ArrayList<View> selectedView;

    public final static int selectedColor = Color.argb(255,37,241,227);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investgation_result);

        mRecyclerView = (RecyclerView)findViewById(R.id.investgation_result_recyclerView);
        mRecyclerView.setHasFixedSize(true);

        selectedView = new ArrayList<>();


        btnConcern = (ImageView)findViewById(R.id.investgation_result_btnConcern);
        btnCancel = (ImageView)findViewById(R.id.investgation_result_btnCancel);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

        List<Hydrant> dataSet = new ArrayList<>();

        db = new MyDBHelper(this).getReadableDatabase();

        isInvestigation = getIntent().getBooleanExtra(BOOLEAN_ARG_ISINVESTGATION,true);
        isSearch = getIntent().getBooleanExtra(BOOLEAN_IS_SEARCH,false);

        if(isInvestigation) {
            Cursor cs = db.query(MyDBHelper.TABLE_TEMP, new String[]{"*"}, null, null, null, null, MyDBHelper.COLUMN_ID + MyDBHelper.ASC);

            if(cs.getCount()>0){
                cs.moveToFirst();
                do{
                    Hydrant hydrant = new Hydrant(cs);

                    dataSet.add(hydrant);
                }while (cs.moveToNext());

            }

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InvestgationResultActivity.this);
                    builder.setTitle("不儲存")
                            .setMessage("確定要放棄修改?\n(暫存資料將會被清空)")
                            .setNegativeButton("取消",null)
                            .setPositiveButton("不儲存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.delete(MyDBHelper.TABLE_TEMP,null,null);
                                    Intent intent = new Intent();
                                    intent.setClass(InvestgationResultActivity.this,MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    InvestgationResultActivity.this.finish();
                                }
                            }).show();
                }
            });


            btnConcern.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InvestgationResultActivity.this);
                    builder.setTitle("請確認資料無誤")
                            .setMessage("將結果儲存並上傳?\n(上傳後原始資料將被覆蓋，並且無法再回復)")
                            .setNegativeButton("取消",null)
                            .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    upload();
                                }
                            }).show();
                }
            });

        }else if(!isSearch){

            ranges = getIntent().getStringArrayListExtra(STRINGARRAY_ARG_RANGES);


            if(ranges.size()>0) {
                if (ranges.get(0).compareTo("all") == 0) {
                    Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT, new String[]{"*"}, null, null, null, null, null);
                    if (cs.moveToFirst()) {
                        do {
                            Hydrant hydrant = new Hydrant(cs);
                            dataSet.add(hydrant);
                        } while (cs.moveToNext());
                    }

                } else {

                    ArrayList<Cursor> css;
                    try {
                        css = MyDBHelper.getDatas(db, ranges);
                        for (Cursor cs : css) {
                            if (cs.moveToFirst() && cs.getCount() > 0) {
                                do {
                                    Hydrant hydrant = new Hydrant(cs);
                                    dataSet.add(hydrant);
                                } while (cs.moveToNext());
                            }
                        }

                    } catch (Exception e) {
                        Log.e("IRA 198",e.getMessage());
                        return;
                    }
                }

                setTitle("查詢結果 (共 "+dataSet.size()+" 筆)");

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("查無結果!").setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
            }

            btnCancel.setVisibility(View.INVISIBLE);
            btnConcern.setVisibility(View.INVISIBLE);

        }else {
            whereString = getIntent().getStringExtra(WHERE_STRING);

            Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT, new String[]{"*"}, whereString, null, null, null, null);
            if (cs.getCount()>0&& cs.moveToFirst()) {
                do {
                    Hydrant hydrant = new Hydrant(cs);
                    dataSet.add(hydrant);
                } while (cs.moveToNext());


                setTitle("查詢結果 (共 "+dataSet.size()+" 筆)");

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("查無結果!").setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
            }

            btnCancel.setVisibility(View.INVISIBLE);
            btnConcern.setVisibility(View.INVISIBLE);


        }


        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled(){
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        };


        if(!isInvestigation) {
           TextView textView =  (TextView) findViewById(R.id.textView15);
           ViewGroup viewGroup = (ViewGroup) textView.getParent();
           viewGroup.removeView(textView);

        }
        mLayoutManager =new LinearLayoutManager(this);
        mLayoutManager.setAutoMeasureEnabled(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ResultListAdapter(this,dataSet,db);

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(clickToOpenMap);
        mAdapter.setOnItemLongClickListener(new ResultListAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, int position) {

                if(isSearch && !itemPicking && !itemRecovering && !mAdapter.isItemDeleted(position) ) {
                    itemPicking = true;
                    invalidateOptionsMenu();
                    mAdapter.setOnItemClickListener(clickToSelect);
                    view.callOnClick();
                    return true;
                }else if(isSearch && !itemPicking && mAdapter.isItemDeleted(position) && !itemRecovering){
                    itemRecovering = true;
                    invalidateOptionsMenu();
                    mAdapter.setOnItemClickListener(clickToSelect);
                    view.callOnClick();
                    return true;
                }else
                    return false;
            }
        });
    }

    ResultListAdapter.OnItemClickListener clickToOpenMap = new ResultListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = new Intent();
            intent.setClass(InvestgationResultActivity.this,MapsActivity.class);
            if(!isSearch){
                intent.putExtra(MapsActivity.STRINGARRAY_ARG_RANGES,ranges);
            }else{
                intent.putExtra(MapsActivity.WHERE_STRING,whereString);
            }
            intent.putExtra(MapsActivity.BOOLEAN_ARG_ISINVESTGATION,isInvestigation);
            intent.putExtra(BOOLEAN_IS_SEARCH,isSearch);
            intent.putExtra(MapsActivity.ARG_TARGET_HYDRANT_ID,mAdapter.getHydrantId(position));
            startActivity(intent);
        }
    };

    ResultListAdapter.OnItemClickListener clickToSelect = new ResultListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if(itemPicking &&  !mAdapter.isItemDeleted(position)) {
                if (selectedView.contains(view)) {
                    ((CardView) view.findViewById(R.id.item_view_CardView)).setCardBackgroundColor(Color.WHITE);
                    selectedView.remove(view);
                    mAdapter.removeSelected(position);
                } else {
                    ((CardView) view.findViewById(R.id.item_view_CardView)).setCardBackgroundColor(selectedColor);
                    selectedView.add(view);
                    mAdapter.addSelected(position);
                }
            }else if(itemRecovering && mAdapter.isItemDeleted(position)){
                if (selectedView.contains(view)) {
                    ((CardView) view.findViewById(R.id.item_view_CardView)).setCardBackgroundColor(Color.WHITE);
                    selectedView.remove(view);
                    mAdapter.removeSelected(position);
                } else {
                    ((CardView) view.findViewById(R.id.item_view_CardView)).setCardBackgroundColor(selectedColor);
                    selectedView.add(view);
                    mAdapter.addSelected(position);
                }
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.result_list_manu, menu);

        if(itemPicking){
            menu.findItem(R.id.action_map).setIcon(R.drawable.ic_delete);
        }else if(itemRecovering) {
            menu.findItem(R.id.action_map).setIcon(R.drawable.ic_recover);
        }else{
            menu.findItem(R.id.action_map).setIcon(R.drawable.ic_map);
        }
        return true;

    }

    @Override
    public void onBackPressed(){
        if(itemPicking) {
            itemPicking = !itemPicking;
            invalidateOptionsMenu();
            mAdapter.setOnItemClickListener(clickToOpenMap);
            selectedView.clear();
            mAdapter.clearSelected();
            mAdapter.notifyDataSetChanged();

        }else if(itemRecovering){
            itemRecovering = !itemRecovering;
            invalidateOptionsMenu();
            mAdapter.setOnItemClickListener(clickToOpenMap);
            mAdapter.clearSelected();
            mAdapter.notifyDataSetChanged();
        }else {
            super.onBackPressed();
        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_map&& !itemPicking && !itemRecovering) {

            Intent intent = new Intent();
            intent.setClass(InvestgationResultActivity.this,MapsActivity.class);
            intent.putExtra(MapsActivity.BOOLEAN_ARG_ISINVESTGATION,isInvestigation);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(BOOLEAN_IS_SEARCH,isSearch);
            if(!isSearch) {
                intent.putExtra(MapsActivity.STRINGARRAY_ARG_RANGES, ranges);
            }else {
                intent.putExtra(MapsActivity.WHERE_STRING,whereString);
            }
            startActivity(intent);

            return true;
        }else if(id == R.id.action_map&& itemPicking){
            if(selectedView.size()>0)
                deleteHydrant();
            else {
                onBackPressed();
            }
        }else if(id == R.id.action_map&& itemRecovering){
            if(selectedView.size()>0)
                recoverHydrant();
            else {
                onBackPressed();
            }
        }

        return super.onOptionsItemSelected(item);

    }

    private void recoverHydrant() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意").setMessage("確定要將已刪除的消防栓恢復?\n(將會回復到刪除前狀態,再次顯示在地圖上)")
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onBackPressed();
                    }
                }).setNeutralButton("取消",null)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int position:mAdapter.getSelectedList()){
                            mAdapter.recoveryDeleted(position);
                            MyDBHelper.replaceTempHydrant(db,mAdapter.getHydrant(position));
                        }
                        upload();
                    }
                }).show();


    }

    private void deleteHydrant(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意").setMessage("確定要將消防栓刪除?\n(已刪除的消防栓將不會在顯示在地圖上，也無法修改)")
                .setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onBackPressed();
                    }
                }).setNeutralButton("取消",null)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                         for(int position : mAdapter.getSelectedList()){
                             mAdapter.deleteHydrant(position);
                             MyDBHelper.replaceTempHydrant(db,mAdapter.getHydrant(position));
                          }
                        upload();
                    }
                }).show();

    }

    private void onUploadSusses(){
        onBackPressed();
    }

    private  void onUploadFailure(){

        db.delete(MyDBHelper.TABLE_TEMP,null,null);
        finish();

    };


    private void upload(){

        if(hasPermission()) {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在上傳資料...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new uploadData().execute();
        }

    }

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
                    upload();
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
            upload();
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

        return false;
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
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

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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

    private class uploadData extends AsyncTask<Void,Void,Integer> {

        private Exception mError = null;

        @Override
        protected Integer doInBackground(Void... params) {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleSheetConnecter gsc = new GoogleSheetConnecter(mCredential,transport,jsonFactory);



            try{
                return GoogleSheetConnecter.uploadDataToSheet(db,gsc);
            }catch (Exception e){
                mError = e;
                cancel(true);

                return null;
            }


        }

        @Override
        protected void onPostExecute(Integer output) {
            progressDialog.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(InvestgationResultActivity.this);
            if(output>=0 && isInvestigation) {
                Toast.makeText(InvestgationResultActivity.this,"資料上傳完成",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(InvestgationResultActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                InvestgationResultActivity.this.finish();

            }else if(output >= 0 && isSearch) {
                Toast.makeText(InvestgationResultActivity.this,"資料上傳完成",Toast.LENGTH_SHORT).show();
                onUploadSusses();
            }else{
                dialog.setMessage("發生錯誤\n代碼:"+output).setTitle("錯誤").setCancelable(false).setNegativeButton("結束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }).show();
                if(isSearch)
                    onUploadFailure();
            }
        }

        @Override
        protected void onCancelled(){
            progressDialog.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(InvestgationResultActivity.this);
            if (mError != null) {
                if (mError instanceof GooglePlayServicesAvailabilityIOException) {
                    checkServices.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mError)
                                    .getConnectionStatusCode());
                } else if (mError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    dialog.setMessage("The following error occurred:\n"
                            + mError.getMessage()).setTitle("取消").show();
                }
            } else {
                dialog.setMessage("Request cancelled.").setTitle("取消").show();
            }
            if(isSearch)
                onUploadFailure();
        }


    }




}

class ResultListAdapter extends RecyclerView.Adapter<ResultListAdapter.ViewHolder> implements View.OnClickListener,View.OnLongClickListener{
    private List<Hydrant> mDataset;
    private ArrayList<Integer> selectedIndex;

    private OnItemClickListener mOnItemClickListener = null;
    private OnItemLongClickListener mOnItemLongClickListener = null;
    Context mContext;
    SQLiteDatabase db;

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mOnItemClickListener.onItemClick(view,(int)view.getTag());
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if(mOnItemLongClickListener != null){
            return this.mOnItemLongClickListener.onItemLongClick(view, (int)view.getTag());
        }
        return false;        }

    public static interface OnItemClickListener {
        void onItemClick(View view , int position);
    }

    public static interface OnItemLongClickListener {
        boolean onItemLongClick(View view , int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        this.mOnItemLongClickListener = listener;
    }


    public  class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextViewTitle;
        public TextView mTextViewType;
        public TextView mTextViewPress;
        public TextView mTextViewMarked;
        public TextView mTextViewStates;
        public TextView getmTextViewMarkStates;
        public TextView mTextViewDist;
        public TextView mTextViewVil;
        public TextView mTextViewAddress;
        public TextView mTextViewPs;
        public CardView mCardView;
        public TextView markType;
        public TextView markPress;
        public TextView markMarked;
        public TextView markStates;
        public TextView markMarkStates;
        public TextView markAddress;
        public TextView markPs;
        public TextView mDuty;
        public TextView markDuty;
        public TextView markTrafficLevel;
        public TextView mTrafficLevelText;
        public android.support.v7.widget.AppCompatSeekBar mTrafficLevelSeekBar;
        public View view;

        public ViewHolder(View v){
            super(v);
            mTextViewTitle =(TextView) v.findViewById(R.id.item_view_Title);
            mTextViewType =(TextView) v.findViewById(R.id.item_view_type);
            mTextViewStates =(TextView) v.findViewById(R.id.item_view_States);
            mTextViewPress =(TextView) v.findViewById(R.id.item_view_Press);
            mTextViewMarked =(TextView) v.findViewById(R.id.item_view_MarkExist);
            getmTextViewMarkStates =(TextView) v.findViewById(R.id.item_view_MarkState);
            mTextViewDist =(TextView) v.findViewById(R.id.item_view_Dist);
            mTextViewVil =(TextView) v.findViewById(R.id.item_view_Vil);
            mTextViewAddress =(TextView) v.findViewById(R.id.item_view_Address);
            mTextViewPs =(TextView) v.findViewById(R.id.item_view_Ps);
            mCardView = (CardView)v.findViewById(R.id.item_view_CardView);
            mDuty= (TextView) v.findViewById(R.id.item_view_duty);
            mTrafficLevelText = (TextView)v.findViewById(R.id.item_view_trafficLevelText);
            mTrafficLevelSeekBar=(android.support.v7.widget.AppCompatSeekBar)v.findViewById(R.id.item_view_trafficLevelSeekBar);


            markType = (TextView) v.findViewById(R.id.item_view_mark_type);
            markPress= (TextView) v.findViewById(R.id.item_view_mark_press);
            markMarked= (TextView) v.findViewById(R.id.item_view_mark_hasMark);
            markStates= (TextView) v.findViewById(R.id.item_view_mark_states);
            markMarkStates= (TextView) v.findViewById(R.id.item_view_mark_markState);
            markAddress= (TextView) v.findViewById(R.id.item_view_mark_address);
            markPs= (TextView) v.findViewById(R.id.item_view_mark_ps);
            markDuty= (TextView) v.findViewById(R.id.item_view_mark_duty);
            markTrafficLevel = (TextView)v.findViewById(R.id.item_view_mark_trafficLevel);
            view = v;

        }
    }
    HashMap<Integer,Boolean> mDeleted;

    public ResultListAdapter(Context context , List<Hydrant> dataset , SQLiteDatabase database){
        mDataset = dataset;
        mContext = context;
        db = database;
        selectedIndex = new ArrayList<>();
        mDeleted = new HashMap<>();
    }

    @Override
    public ResultListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view,parent,false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(selectedIndex.contains(position))
            holder.mCardView.setCardBackgroundColor(selectedColor);
        else
            holder.mCardView.setCardBackgroundColor(Color.WHITE);


        if(position < mDataset.size()) {

            final Hydrant hydrant = mDataset.get(position);
            ArrayList<Integer> diffirentAttr;



            holder.mTextViewTitle.setText(new DecimalFormat("0000").format(hydrant.getId()));
            holder.mTextViewType.setText(Hydrant.getChineseType(hydrant.getType()));



            String s = Hydrant.getChineseState(hydrant.getStates().get(0));
            for (int i = 1; i < hydrant.getStates().size(); i++)
                s += "," + Hydrant.getChineseState(hydrant.getStates().get(i));



            holder.mTextViewStates.setText(s);
            holder.mTextViewMarked.setText(hydrant.hasMark() ? "有設" : "無設");
            holder.mTextViewPress.setText(hydrant.getPress());
            holder.getmTextViewMarkStates.setText(hydrant.getMark_States());
            holder.mTextViewDist.setText(Hydrant.getChineseDist(hydrant.getDist()));
            holder.mTextViewVil.setText(hydrant.getVil());
            holder.mTextViewAddress.setText(hydrant.getAddress());
            holder.mTextViewPs.setText(hydrant.getPs());
            holder.mDuty.setText(Hydrant.DUTY.getDutyName(hydrant.getDutyCode()));
            holder.mTrafficLevelText.setText(Hydrant.TRAFFIC.LevelName().get(hydrant.getTrafficLevel())+"可通行");
            holder.mTrafficLevelSeekBar.setProgress(hydrant.getTrafficLevel()-1);
            holder.mTrafficLevelSeekBar.setEnabled(false);
            switch (hydrant.getTrafficLevel()){
                case 1:
                    holder.mTrafficLevelSeekBar.setThumb(ContextCompat.getDrawable(mContext, R.drawable.ic_traffic1));
                    holder.mTrafficLevelSeekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

                    /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                        holder.mTrafficLevelSeekBar.getProgressDrawable().setTint(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
                    }else{
                        DrawableCompat.setTint(holder.mTrafficLevelSeekBar.getProgressDrawable(),ContextCompat.getColor(mContext,R.color.colorPrimaryDark));
                    }*/
                    break;
                case 2:
                    holder.mTrafficLevelSeekBar.setThumb(ContextCompat.getDrawable(mContext, R.drawable.ic_traffic2));
                    holder.mTrafficLevelSeekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.dangerOrange), PorterDuff.Mode.MULTIPLY);


                    break;
                case 3:
                    holder.mTrafficLevelSeekBar.setThumb(ContextCompat.getDrawable(mContext, R.drawable.ic_traffic3));
                    holder.mTrafficLevelSeekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.nomal_green), PorterDuff.Mode.MULTIPLY);
/*
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                        holder.mTrafficLevelSeekBar.getProgressDrawable().setTint(ContextCompat.getColor(mContext, R.color.colorStateWarning));
                    }else{
                        DrawableCompat.setTint(holder.mTrafficLevelSeekBar.getProgressDrawable(),ContextCompat.getColor(mContext,R.color.colorStateWarning));
                    }*/
                    break;
                case 4:
                    holder.mTrafficLevelSeekBar.setThumb(ContextCompat.getDrawable(mContext, R.drawable.ic_traffic4));
                    holder.mTrafficLevelSeekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.good_green), PorterDuff.Mode.MULTIPLY);

                    /*if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                        holder.mTrafficLevelSeekBar.getProgressDrawable().setTint(ContextCompat.getColor(mContext, R.color.good_green));
                    }else{
                        DrawableCompat.setTint(holder.mTrafficLevelSeekBar.getProgressDrawable(),ContextCompat.getColor(mContext,R.color.good_green));
                    }*/
                    break;
            }
            holder.view.setTag(position);
            holder.view.setOnClickListener(this);
            holder.view.setOnLongClickListener(this);

            if(hydrant.getStates().contains(Hydrant.STATES.REMOVED)){
                holder.mTextViewTitle.setTextColor(Color.LTGRAY);
                holder.getmTextViewMarkStates.setTextColor(Color.LTGRAY);
                holder.markAddress.setTextColor(Color.LTGRAY);
                holder.markMarked.setTextColor(Color.LTGRAY);
                holder.markMarkStates.setTextColor(Color.LTGRAY);
                holder.markPress.setTextColor(Color.LTGRAY);
                holder.markPs.setTextColor(Color.LTGRAY);
                holder.markType.setTextColor(Color.LTGRAY);
                holder.mTextViewDist.setTextColor(Color.LTGRAY);
                holder.markStates.setTextColor(Color.LTGRAY);
                holder.mTextViewAddress.setTextColor(Color.LTGRAY);
                holder.mTextViewMarked.setTextColor(Color.LTGRAY);
                holder.mTextViewPress.setTextColor(Color.LTGRAY);
                holder.mTextViewPs.setTextColor(Color.LTGRAY);
                holder.mTextViewStates.setTextColor(Color.LTGRAY);
                holder.mTextViewVil.setTextColor(Color.LTGRAY);
                holder.mTextViewType.setTextColor(Color.LTGRAY);
                holder.markDuty.setTextColor(Color.LTGRAY);
                holder.mDuty.setTextColor(Color.LTGRAY);
                holder.markTrafficLevel.setTextColor(Color.LTGRAY);
                holder.mTrafficLevelText.setTextColor(Color.LTGRAY);
                holder.mTrafficLevelSeekBar.setVisibility(View.INVISIBLE);

                holder.mTextViewTitle.setText(holder.mTextViewTitle.getText()+"(已移除)");

                mDeleted.put(position,true);

                return;
            }else {
                holder.mTextViewTitle.setTextColor(Color.argb(255,66,66,66));
                holder.getmTextViewMarkStates.setTextColor(Color.argb(255,66,66,66));
                holder.markAddress.setTextColor(Color.argb(255,66,66,66));
                holder.markMarked.setTextColor(Color.argb(255,66,66,66));
                holder.markMarkStates.setTextColor(Color.argb(255,66,66,66));
                holder.markPress.setTextColor(Color.argb(255,66,66,66));
                holder.markPs.setTextColor(Color.argb(255,66,66,66));
                holder.markType.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewDist.setTextColor(Color.argb(255,66,66,66));
                holder.markStates.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewAddress.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewMarked.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewPress.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewPs.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewStates.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewVil.setTextColor(Color.argb(255,66,66,66));
                holder.mTextViewType.setTextColor(Color.argb(255,66,66,66));
                holder.mDuty.setTextColor(Color.argb(255,66,66,66));
                holder.markDuty.setTextColor(Color.argb(255,66,66,66));
                holder.markTrafficLevel.setTextColor(Color.argb(255,66,66,66));
                holder.mTrafficLevelText.setTextColor(Color.argb(255,66,66,66));
                holder.mTrafficLevelSeekBar.setVisibility(View.VISIBLE);


                mDeleted.put(position,false);
            }


            Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{"*"},MyDBHelper.COLUMN_ID+" = "+hydrant.getId(),null,null,null,null);
            if(cs.getCount()>0&&cs.moveToFirst()) {
                Hydrant oriHydrant = new Hydrant(cs);
                diffirentAttr = hydrant.findDiffrent(oriHydrant);
            }
            else {
                diffirentAttr = new ArrayList<>();
                holder.mTextViewTitle.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
            }
            switch (hydrant.getUsableState()){
                case Hydrant.USABLE_STATE.GOOD:
                    holder.mTextViewStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorStateGood));
                    break;
                case Hydrant.USABLE_STATE.WARNING:
                    holder.mTextViewStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorStateWarning));
                    break;
                case Hydrant.USABLE_STATE.BROKEN:
                    holder.mTextViewStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorStateBroken));
                    break;
            }


            if(hydrant.getMark_States().compareTo(Hydrant.MARK_STATES.GOOD)!=0)
                holder.getmTextViewMarkStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorStateWarning));
            else
                holder.getmTextViewMarkStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorStateGood));

            if(diffirentAttr.size()>0 ) {
                holder.mTextViewTitle.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                for (int attr : diffirentAttr){

                    switch (attr){
                        case Hydrant.ATTRIBUTE.ADDRESS:
                            holder.markAddress.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;
                        case Hydrant.ATTRIBUTE.STATES:
                            holder.markStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;
                        case Hydrant.ATTRIBUTE.MARK_SET:
                            holder.markMarked.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;
                        case Hydrant.ATTRIBUTE.MARK_STATE:
                            holder.markMarkStates.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;
                        case Hydrant.ATTRIBUTE.PRESS:
                            holder.markPress.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;
                        case Hydrant.ATTRIBUTE.PS:
                            holder.markPs.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;
                        case Hydrant.ATTRIBUTE.TYPE:
                            holder.markType.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                            break;

                        case Hydrant.ATTRIBUTE.TRAFFIC_LEVEL:
                            holder.markTrafficLevel.setTextColor(ContextCompat.getColor(mContext.getApplicationContext(),R.color.colorAccent));
                    }
                }
            }



        }else if(position==mDataset.size()){
            // holder.mCardView.setVisibility(View.INVISIBLE);

        }



    }

    public Hydrant getHydrant(int position){
        return mDataset.get(position);
    }

    public void deleteHydrant(int position){
        if(!mDeleted.get(position)) {
            mDataset.get(position).addStates(Hydrant.STATES.REMOVED);
            //mDeleted.put(position,true);
            notifyItemChanged(position);
        }
    }

    public void recoveryDeleted(int position){
        if(mDeleted.get(position)){
            mDataset.get(position).removeStates(Hydrant.STATES.REMOVED);
            //mDeleted.put(position,false);
            notifyItemChanged(position);
        }
    }

    public boolean isItemDeleted(int position){
        return mDeleted.get(position);
    }

    public int getHydrantId(int position){

        return mDataset.get(position).getId();
    }

    public void addSelected(int position){
        if(!selectedIndex.contains((Object)position))
            selectedIndex.add(position);
    }
    public void removeSelected(int position){
        if(selectedIndex.contains((Object)position))
            selectedIndex.remove((Object)position);
    }

    public void clearSelected(){
        selectedIndex.clear();
    }

    public ArrayList<Integer> getSelectedList(){
        return selectedIndex;
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}



