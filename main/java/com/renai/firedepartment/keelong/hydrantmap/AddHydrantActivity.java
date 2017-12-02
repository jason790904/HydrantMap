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
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

import static com.renai.firedepartment.keelong.hydrantmap.InvestgationActivity.EditPageViewFragment.ARG_HYDRANT_ID;

public class AddHydrantActivity extends AppCompatActivity  {

    TextView textViewTitle;
    Spinner spinnerType;
    EditText editTextPress;
    ImageButton buttonPressMinus;
    ImageButton buttonPressPlus;
    Button buttonStates;
    Spinner spinnerHasMarked;
    Spinner spinnerMakedStates;
    EditText editTextMarkedStatesEtc;
    Spinner spinnerDist;
    Spinner spinnerVil;
    EditText editTextAddress;
    ImageView imageViewLocationButtom;
    EditText editTextPs;
    ImageView buttonRevert;
    ImageView buttonWindowControl;

    ImageView seekBarImage;
    TextView seekBarText;

    TextView trafficText;
    DiscreteSeekBar trafficSeekBar;

    ImageView concernButton;
    ImageView cancelButton;

    Hydrant mHydrant;

    SQLiteDatabase db;

    public final static int REQUEST_CHOOSE_LOCATION = 999;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    ProgressDialog progressDialog;
    GoogleAccountCredential mCredential;
    GooglePlayService checkServices;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hydrant);

        db = new MyDBHelper(this).getWritableDatabase();


        buttonRevert = (ImageView)findViewById(R.id.editPageRevertButton);


        textViewTitle = (TextView) findViewById(R.id.editPageTitle);
        spinnerType= (Spinner) findViewById(R.id.editPageType);
        editTextPress= (EditText) findViewById(R.id.editPagePressInputText);
        buttonPressMinus= (ImageButton) findViewById(R.id.editPagePressMinusButtom);
        buttonPressPlus= (ImageButton) findViewById(R.id.editPagerPressPlusButtom);
        buttonStates= (Button) findViewById(R.id.editPageStates);
        spinnerHasMarked= (Spinner) findViewById(R.id.editPageHasMarked);
        spinnerMakedStates= (Spinner) findViewById(R.id.editPageMarkedStates);
        editTextMarkedStatesEtc= (EditText) findViewById(R.id.editPageMarkedStatesEtcInput);
        spinnerDist= (Spinner) findViewById(R.id.editPageDist);
        spinnerVil= (Spinner) findViewById(R.id.editPageVil);
        editTextAddress= (EditText) findViewById(R.id.editPageAddress);
        imageViewLocationButtom= (ImageView) findViewById(R.id.editPageLocation);
        editTextPs= (EditText) findViewById(R.id.editPagePs);

        seekBarImage=(ImageView)findViewById(R.id.investgetionSeekImage);
        seekBarText = (TextView) findViewById(R.id.investgationSeekText);

        trafficText = (TextView)findViewById(R.id.editPageTraficText);
        trafficSeekBar = (DiscreteSeekBar)findViewById(R.id.editPageTrafficSeek);

        cancelButton = (ImageView)findViewById(R.id.cancel_button);
        concernButton = (ImageView)findViewById(R.id.concern_button);

        buttonWindowControl =(ImageView)findViewById(R.id.editPageBtnWindowControl);
        buttonWindowControl.setVisibility(View.INVISIBLE);
        buttonRevert.setVisibility(View.INVISIBLE);

        textViewTitle.setText("");

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());



        concernButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNewHydrant();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                builder.setTitle("結束").setMessage(
                        "確定要取消新增消防栓?"
                ).setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AddHydrantActivity.this.finish();
                    }
                }).setNegativeButton("取消",null).show();
            }
        });

    }

    private void checkUnSavedData(){
        Cursor cs = db.query(MyDBHelper.TABLE_TEMP,new String[]{MyDBHelper.COLUMN_ID},null,null,null,null,null);
        if(cs.getCount()>0){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(AddHydrantActivity.this);
            builder1.setMessage("發現有上次未儲存的資料，\n請問是否先將舊資料儲存?\n否則資料將會一起上傳")
                    .setNeutralButton("這次一起上傳", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(AddHydrantActivity.this,"舊資料將會連同這次的新增一起上傳",Toast.LENGTH_LONG).show();
                            showIdInput();
                        }
                    })
                    .setNegativeButton("放棄舊資料", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.delete(MyDBHelper.TABLE_TEMP,null,null);
                            Toast.makeText(AddHydrantActivity.this,"資料刪除完成，請繼續執行新增",Toast.LENGTH_LONG).show();
                            showIdInput();
                        }
                    }).setPositiveButton("先編輯舊資料", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setClass(AddHydrantActivity.this,InvestgationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    AddHydrantActivity.this.finish();
                }
            }).setCancelable(false).show();
        }else{
            showIdInput();
        }
    }

    private void checkNewHydrant() {

        if(mHydrant.getType() == -1){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("類型未設定").setPositiveButton("了解", null).show();
            return;
        }else if(mHydrant.getStates().size() <= 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("類型未設定").setPositiveButton("了解", null).show();
            return;
        }else if(mHydrant.getPress() == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("壓力未設定").setPositiveButton("了解", null).show();
            return;
        }else if(mHydrant.getDist() ==-1){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("行政區未設定").setPositiveButton("了解", null).show();
            return;
        }else if(mHydrant.getVil() == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("里名未設定").setPositiveButton("了解", null).show();
            return;
        }else if(mHydrant.getAddress() == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("地址未設定").setPositiveButton("了解", null).show();
            return;
        }else if(mHydrant.getLatlng()==null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
            builder.setTitle("錯誤").setMessage("GPS未設定\n是否從地址自動判斷?")
                    .setNeutralButton("取消",null)
                    .setNegativeButton("否,手動設定GPS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setClass(AddHydrantActivity.this, ChooseLocationActivity.class);
                            intent.putExtra(ChooseLocationActivity.STATE_FIND_LOCATION_SUSSES, false);
                            startActivityForResult(intent, REQUEST_CHOOSE_LOCATION);
                        }
                    })
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                     @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                          String address = editTextAddress.getText().toString();
                          address.replace(" ","");
                          if(address.compareTo("")==0){
                               AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                                builder.setMessage("請先輸入地址在執行定位").setTitle("訊息").setPositiveButton("確認",null).show();
                             return;
                          }else if(address.length()<3){
                             AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                             builder.setMessage("你輸入的地址過短，無法定位").setTitle("訊息").setPositiveButton("確認",null).show();
                              return;
                          }else {
                               if(mHydrant.getLatlng()==null) {
                                    if (address.substring(0, 1).compareTo("基隆") != 0)
                                        address = "基隆市" + address;
                                       Geocoder gc = new Geocoder(AddHydrantActivity.this);
                                        try {
                                            List<Address> lstAddress = gc.getFromLocationName(address, 3);
                                        if (lstAddress.size() > 0) {
                                            Address ad = lstAddress.get(0);
                                            mHydrant.setLatlng(ad.getLatitude(),ad.getLongitude());
                                            checkNewHydrant();
                                       } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                                            builder.setMessage("輸入的地址無法自動抓取GPS資料。\n是否手動設定").setTitle("訊息")
                                                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent intent = new Intent();
                                                            intent.setClass(AddHydrantActivity.this, ChooseLocationActivity.class);
                                                            intent.putExtra(ChooseLocationActivity.STATE_FIND_LOCATION_SUSSES, false);
                                                            startActivityForResult(intent, REQUEST_CHOOSE_LOCATION);
                                                        }
                                                    })
                                                    .setNegativeButton("重新輸入地址", null).show();
                                        }
                                        } catch (IOException e) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                                            builder.setMessage(e.getMessage() + "\n" + TextUtils.join("\n", e.getStackTrace())).setTitle("訊息").setPositiveButton("確認", null).show();
                                    }
                            }}
                    }
                    }).show();
            return;
        }

        ArrayList<String> statesList = new ArrayList<>();
        for(int code:mHydrant.getStates())
            statesList.add(Hydrant.getChineseState(code));

        DecimalFormat df = new DecimalFormat("#.00000");
        AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
        builder.setTitle("新增").setMessage(
                "以下是您新增的消防栓內容，\n請確認資料無誤後再上傳:\n"+
                        "ID :"+mHydrant.getId()+"\n"+
                        "類型 :"+Hydrant.getChineseType(mHydrant.getType())+"\n"+
                        "消防栓狀態 :"+TextUtils.join(",",statesList)+"\n"+
                        "告示牌 :"+(mHydrant.hasMark()?"有設":"無設")+"\n"+
                        "告示牌狀態 :"+mHydrant.getMark_States()+"\n"+
                        "測壓 :"+mHydrant.getPress()+"\n"+
                        "地址 :"+Hydrant.getChineseDist(mHydrant.getDist())+
                        " "+mHydrant.getVil()+"\n"+
                        "\t "+mHydrant.getAddress()+"\n"+
                        "道路寬度: "+mHydrant.getTrafficLevel()+"級，"+Hydrant.TRAFFIC.LevelName().get(mHydrant.getTrafficLevel())+"可通行 \n"+
                        "GPS定位 :\n"+df.format(mHydrant.getLatlng().latitude)+","+df.format(mHydrant.getLatlng().longitude)+"\n"+
                        "備註 :"+mHydrant.getPs()+"\n"
        ).setNegativeButton("取消", null).setPositiveButton("確認儲存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyDBHelper.replaceTempHydrant(db,mHydrant);
                upload();
            }
        }).show();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mHydrant == null){
            checkUnSavedData();
        }
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
            intent.putExtra(ManualActivity.ARG_MANUAL_TYPE,ManualActivity.MANUAL_TYPE_ADD);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void upload(){

        if(hasPermission()) {

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在上傳資料...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new uploadData().execute();
        }

    }

    private void showIdInput(){
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新增").setMessage("請輸入愈新增的ID編號:\n").setView(input).setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AddHydrantActivity.this.finish();
            }
        }).setCancelable(false).setPositiveButton("確認", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String inputId = input.getText().toString();
                if(inputId.compareTo("")!=0){
                    int newId = Integer.valueOf(inputId);
                    Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{"*"},MyDBHelper.COLUMN_ID+" = "+newId,null,null,null,null);
                    if(cs.getCount()<=0) {
                        if(newId==0 || newId>10000){
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(AddHydrantActivity.this);
                            builder1.setTitle("錯誤").setMessage("不合法的範圍!\n請輸入0001~9999間的整數!").setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    showIdInput();
                                }
                            }).show();
                            dialogInterface.dismiss();
                        }else {
                            mHydrant = new Hydrant(newId);
                            DecimalFormat df = new DecimalFormat("0000");
                            textViewTitle.setText(df.format(newId));
                            initialUi();
                        }
                    }else {
                        cs.moveToFirst();
                        Hydrant hydrant = new Hydrant(cs);
                        if(hydrant.getStates().contains(Hydrant.STATES.REMOVED)){
                            mHydrant = new Hydrant(newId);
                            DecimalFormat df = new DecimalFormat("0000");
                            textViewTitle.setText(df.format(newId));
                            initialUi();
                        }else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(AddHydrantActivity.this);
                            builder1.setTitle("錯誤").setMessage("輸入的編號已經存在!").setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    showIdInput();
                                }
                            }).show();
                            dialogInterface.dismiss();
                        }
                    }
                }else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(AddHydrantActivity.this);
                    builder1.setTitle("錯誤").setMessage("不能輸入空值!").setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showIdInput();
                        }
                    }).show();
                    dialogInterface.dismiss();
                }
            }
        }).setCancelable(false).show();
    }

    private void initialUi(){

        setSpinnerType();

        editTextPress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().compareTo("")!=0){
                    mHydrant.setPress(s.toString());
                }
            }
        });
        editTextPress.setText("1.20");


        buttonPressMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DecimalFormat df = new DecimalFormat("#0.00");
                editTextPress.setText(df.format(Double.valueOf(editTextPress.getText().toString())-0.05));
            }
        });
        buttonPressPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DecimalFormat df = new DecimalFormat("#0.00");
                editTextPress.setText(df.format(Double.valueOf(editTextPress.getText().toString())+0.05));
            }
        });

        mHydrant.setPress(editTextPress.getText().toString());


        setButtonStates();



        spinnerHasMarked.setAdapter(new ArrayAdapter<String>(this,R.layout.simple_list_item_1,new String[]{"無設","有設"}));

        spinnerHasMarked.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position ==0){
                    spinnerMakedStates.setSelection(0);
                    editTextMarkedStatesEtc.setVisibility(View.INVISIBLE);
                    spinnerMakedStates.setOnItemSelectedListener(null);
                    mHydrant.setMark_States(Hydrant.MARK_STATES.NULL);
                    spinnerMakedStates.setEnabled(false);
                    spinnerMakedStates.setAdapter(null);

                }else{
                    spinnerMakedStates.setEnabled(true);
                    setSpinnerMarkState();


                }
                mHydrant.setMarkExist(position==1);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        setSpinnerMarkState();


        List<String> distList = new ArrayList<>();
        final List<Integer> allDist = Hydrant.DIST.getAllDist();

        for(int i  :allDist )
            distList.add(Hydrant.getChineseDist(i));
        spinnerDist.setAdapter(new ArrayAdapter<String>(this,R.layout.simple_list_item_1,distList));
        spinnerDist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerVil.setAdapter(new ArrayAdapter<String>(AddHydrantActivity.this,R.layout.simple_list_item_1,Hydrant.VIL.getVil(allDist.get(position))));
                mHydrant.setDist(allDist.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerVil.setAdapter(null);
            }
        });



        spinnerVil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mHydrant.setVil(Hydrant.VIL.getVil(mHydrant.getDist()).get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        editTextAddress.setText("");
        editTextAddress.setHint("請輸入地址");
        editTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().compareTo("")!=0){
                    mHydrant.setAddress(s.toString());
                }
            }
        });

        setTrafficLevelUi();

        editTextPs.setText("");
        editTextPs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().compareTo("")!=0){
                    mHydrant.setPs(s.toString());
                }else{
                    String none = "NONE";
                    mHydrant.setPs(none);
                }
            }
        });

        imageViewLocationButtom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = editTextAddress.getText().toString();
                address.replace(" ","");
                if(address.compareTo("")==0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                    builder.setMessage("請先輸入地址在執行定位").setTitle("訊息").setPositiveButton("確認",null).show();
                    return;
                }else if(address.length()<3){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                    builder.setMessage("你輸入的地址過短，無法定位").setTitle("訊息").setPositiveButton("確認",null).show();
                    return;
                }else {
                    if(mHydrant.getLatlng()==null) {
                        if (address.substring(0, 1).compareTo("基隆") != 0)
                            address = "基隆市" + address;
                        Geocoder gc = new Geocoder(AddHydrantActivity.this);
                        try {
                            List<Address> lstAddress = gc.getFromLocationName(address, 3);
                            if (lstAddress.size() > 0) {
                                Address ad = lstAddress.get(0);
                                Intent intent = new Intent();
                                intent.setClass(AddHydrantActivity.this, ChooseLocationActivity.class);
                                intent.putExtra(ChooseLocationActivity.EXTRA_LATITUDE, ad.getLatitude());
                                intent.putExtra(ChooseLocationActivity.EXTRA_LONGITUDE, ad.getLongitude());
                                intent.putExtra(ChooseLocationActivity.STATE_FIND_LOCATION_SUSSES, true);
                                startActivityForResult(intent, REQUEST_CHOOSE_LOCATION);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                                builder.setMessage("輸入的地址無法自動抓取GPS資料。\n是否手動設定").setTitle("訊息")
                                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent();
                                                intent.setClass(AddHydrantActivity.this, ChooseLocationActivity.class);
                                                intent.putExtra(ChooseLocationActivity.STATE_FIND_LOCATION_SUSSES, false);
                                                startActivityForResult(intent, REQUEST_CHOOSE_LOCATION);
                                            }
                                        })
                                        .setNegativeButton("重新輸入地址", null).show();
                            }
                        } catch (IOException e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                            builder.setMessage(e.getMessage() + "\n" + TextUtils.join("\n", e.getStackTrace())).setTitle("訊息").setPositiveButton("確認", null).show();
                        }
                    }else{
                        Intent intent = new Intent();
                        intent.setClass(AddHydrantActivity.this, ChooseLocationActivity.class);
                        intent.putExtra(ChooseLocationActivity.EXTRA_LATITUDE, mHydrant.getLatlng().latitude);
                        intent.putExtra(ChooseLocationActivity.EXTRA_LONGITUDE, mHydrant.getLatlng().longitude);
                        intent.putExtra(ChooseLocationActivity.STATE_FIND_LOCATION_SUSSES, true);
                        startActivityForResult(intent, REQUEST_CHOOSE_LOCATION);
                    }

                }
            }
        });
    }

    private void setSpinnerMarkState() {


        final List<String> MarkStatesList = Hydrant.MARK_STATES.getAllStates();
        spinnerMakedStates.setAdapter(new ArrayAdapter<String>(this,R.layout.simple_list_item_1,MarkStatesList));

        spinnerMakedStates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==parent.getCount()-1){
                    editTextMarkedStatesEtc.setVisibility(View.VISIBLE);
                    editTextMarkedStatesEtc.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if(s.toString().compareTo("")!=0){
                                mHydrant.setMark_States(s.toString());
                            }
                        }
                    });
                }else {
                    editTextMarkedStatesEtc.setVisibility(View.INVISIBLE);
                    mHydrant.setMark_States(MarkStatesList.get(position));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerMakedStates.setSelection(Hydrant.MARK_STATES.getAllStates().indexOf(Hydrant.MARK_STATES.GOOD));
            }
        });
    }

    private void setButtonStates() {


        final ArrayList<String> statesStringList = new ArrayList<>();
        for(int code:Hydrant.STATES.getAllStates())
            statesStringList.add(Hydrant.getChineseState(code));
        statesStringList.remove(Hydrant .STATES.getAllStates().indexOf(Hydrant.STATES.GOOD));

        final ArrayList<Integer>selectedStatesIndex = new ArrayList<>();
        buttonStates.setText(Hydrant.getChineseState(Hydrant.STATES.GOOD));
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(Hydrant.STATES.GOOD);
        mHydrant.setStates(temp);

        buttonStates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builder =new AlertDialog.Builder(AddHydrantActivity.this);

                final List<Integer> tempSelectedIndex = new ArrayList<Integer>();
                tempSelectedIndex.addAll(selectedStatesIndex);
                final CharSequence[] statesListArray = new CharSequence[statesStringList.size()];
                for(int i =0;i<statesStringList.size();i++)
                    statesListArray[i] = statesStringList.get(i);
                final boolean[] checkedList = new boolean[statesStringList.size()];
                for (int i = 0 ;i<statesStringList.size();i++)
                    checkedList[i]=false;

                for(int i :tempSelectedIndex)
                    checkedList[i] = true;
                builder.setMultiChoiceItems(statesListArray, checkedList, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if(isChecked && !tempSelectedIndex.contains(which)){
                            tempSelectedIndex.add(which);
                        }else if(!isChecked && tempSelectedIndex.contains(which)){
                            tempSelectedIndex.remove((Object)which);


                        }

                    }
                }).setNegativeButton("取消", null).setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<String> resultStatesString = new ArrayList<String>();
                        ArrayList<Integer> newStates = new ArrayList<Integer>();
                        selectedStatesIndex.clear();
                        selectedStatesIndex.addAll(tempSelectedIndex);
                        if(selectedStatesIndex.size()==0){
                            resultStatesString.add(Hydrant.getChineseState(Hydrant.STATES.GOOD));
                            newStates.add(Hydrant.STATES.GOOD);
                        }else {
                            for (int selected : selectedStatesIndex) {
                                resultStatesString.add(statesStringList.get(selected));
                                newStates.add(Hydrant.STATES.getAllStates().get(selected+1));
                            }
                        }
                        mHydrant.setStates(newStates);

                        buttonStates.setText(TextUtils.join(",",resultStatesString.toArray()));
                    }
                }).show();
            }
        });




    }


    private void setSpinnerType() {

        final List<String> types = new ArrayList<>();
        for(int i : Hydrant.TYPE.getAllType())
            types.add(Hydrant.getChineseType(i));

        spinnerType.setAdapter(new ArrayAdapter<String>(this,R.layout.simple_list_item_1,types));
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mHydrant.setType(Hydrant.TYPE.getAllType().get(position));


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CHOOSE_LOCATION:
                if(resultCode == RESULT_OK){

                    LatLng selectedPosition = new LatLng(data.getDoubleExtra(ChooseLocationActivity.EXTRA_LATITUDE,0),data.getDoubleExtra(ChooseLocationActivity.EXTRA_LONGITUDE,0));
                    mHydrant.setLatlng(selectedPosition);
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                    builder.setMessage("GPS設定成功!").setTitle("訊息").setPositiveButton("確認", null).show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddHydrantActivity.this);
                    builder.setMessage("GPS設定取消").setTitle("訊息").setPositiveButton("確認", null).show();

                }
                break;
        }
    };

    private void setTrafficLevelUi(){


        trafficText.setText(Hydrant.TRAFFIC.LevelName().get(2)+" 可通行");
        trafficSeekBar.setMax(Hydrant.TRAFFIC.LevelName().size()-1);
        trafficSeekBar.setMin(1);
        trafficSeekBar.setProgress(2);
        mHydrant.setTrafficLevel(2);

        switch (mHydrant.getTrafficLevel()){
            case 1:
                trafficSeekBar.setScrubberColor(getResources().getColor(R.color.colorPrimaryDark));
                trafficSeekBar.setThumbColor(getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark));
                break;
            case 2:
                trafficSeekBar.setScrubberColor(getResources().getColor(R.color.dangerOrange));
                trafficSeekBar.setThumbColor(getResources().getColor(R.color.dangerOrange),getResources().getColor(R.color.dangerOrange));
                break;
            case 3:
                trafficSeekBar.setScrubberColor(getResources().getColor(R.color.nomal_green));
                trafficSeekBar.setThumbColor(getResources().getColor(R.color.nomal_green),getResources().getColor(R.color.nomal_green));
                break;
            case 4:
                trafficSeekBar.setScrubberColor(getResources().getColor(R.color.good_green));
                trafficSeekBar.setThumbColor(getResources().getColor(R.color.good_green),getResources().getColor(R.color.good_green));
                break;
        }



        trafficSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                switch (value){
                    case 1:
                        //seekBar.setTrackColor(getResources().getColor(R.color.colorPrimaryDark));
                        seekBar.setScrubberColor(getResources().getColor(R.color.colorPrimaryDark));
                        seekBar.setThumbColor(getResources().getColor(R.color.colorPrimaryDark),getResources().getColor(R.color.colorPrimaryDark));
                        seekBar.setRippleColor(getResources().getColor(R.color.colorPrimaryDark));
                        seekBarImage.setImageResource(R.drawable.icon_pump);
                        seekBarText.setText("Pump");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.colorPrimaryDark));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.colorPrimaryDark));
                        }
                        break;
                    case 2:
                        //seekBar.setTrackColor(getResources().getColor(R.color.dangerOrange));
                        seekBar.setScrubberColor(getResources().getColor(R.color.dangerOrange));
                        seekBar.setThumbColor(getResources().getColor(R.color.dangerOrange),getResources().getColor(R.color.dangerOrange));
                        seekBar.setRippleColor(getResources().getColor(R.color.dangerOrange));

                        seekBarImage.setImageResource(R.drawable.icon_51_car_s);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.dangerOrange));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.dangerOrange));
                        }
                        seekBarText.setText("51");
                        break;
                    case 3:
                        //seekBar.setTrackColor(getResources().getColor(R.color.nomal_green));
                        seekBar.setScrubberColor(getResources().getColor(R.color.nomal_green));
                        seekBar.setThumbColor(getResources().getColor(R.color.nomal_green),getResources().getColor(R.color.nomal_green));
                        seekBar.setRippleColor(getResources().getColor(R.color.nomal_green));

                        seekBarImage.setImageResource(R.drawable.icon_11_car_s);
                        seekBarText.setText("11");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.nomal_green));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.nomal_green));
                        }
                        break;
                    case 4:
                        //seekBar.setTrackColor(getResources().getColor(R.color.good_green));
                        seekBar.setScrubberColor(getResources().getColor(R.color.good_green));
                        seekBar.setThumbColor(getResources().getColor(R.color.good_green),getResources().getColor(R.color.good_green));
                        seekBar.setRippleColor(getResources().getColor(R.color.good_green));

                        seekBarImage.setImageResource(R.drawable.icon_61_car_s);
                        seekBarText.setText("61");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.good_green));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.good_green));
                        }
                        break;
                }

                mHydrant.setTrafficLevel(value);
                trafficText.setText(Hydrant.TRAFFIC.LevelName().get(mHydrant.getTrafficLevel())+" 可通行");

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                switch (seekBar.getProgress()){
                    case 1:
                        seekBarImage.setImageResource(R.drawable.icon_pump);
                        seekBarText.setText("Pump");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.colorPrimaryDark));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.colorPrimaryDark));
                        }
                        break;
                    case 2:
                        seekBarImage.setImageResource(R.drawable.icon_51_car_s);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.dangerOrange));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.dangerOrange));
                        }
                        seekBarText.setText("51");
                        break;
                    case 3:
                        seekBarImage.setImageResource(R.drawable.icon_11_car_s);
                        seekBarText.setText("11");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.nomal_green));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.nomal_green));
                        }
                        break;
                    case 4:
                        seekBarImage.setImageResource(R.drawable.icon_61_car_s);
                        seekBarText.setText("61");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            seekBarImage.getBackground().setTint(getResources().getColor(R.color.good_green));
                        }else{
                            DrawableCompat.setTint(seekBarImage.getBackground(),getResources().getColor(R.color.good_green));
                        }
                        break;
                }
                Animation fadeIn = AnimationUtils.loadAnimation(AddHydrantActivity.this,R.anim.alpha);
                fadeIn.setDuration(200);
                seekBarImage.startAnimation(fadeIn);
                seekBarText.startAnimation(fadeIn);
                seekBarImage.setVisibility(View.VISIBLE);
                seekBarText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                Animation fadeOut = AnimationUtils.loadAnimation(AddHydrantActivity.this,R.anim.alpha_reverse);
                fadeOut.setDuration(200);
                seekBarImage.startAnimation(fadeOut);
                seekBarText.startAnimation(fadeOut);
                seekBarImage.setVisibility(View.INVISIBLE);
                seekBarText.setVisibility(View.INVISIBLE);
            }
        });


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


    private void onUploadSusses(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("上傳完成").setMessage("是否繼續新增?").setNegativeButton("否,結束", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent();
                intent.setClass(AddHydrantActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                AddHydrantActivity.this.finish();

            }
        }).setPositiveButton("是,繼續", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                textViewTitle.setText("");
                mHydrant = null;
                showIdInput();
            }
        }).setCancelable(false).show();

    };

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
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddHydrantActivity.this);
            if(output>=0) {

                onUploadSusses();

            }else {
                dialog.setMessage("發生錯誤\n代碼:"+output).setTitle("錯誤").setCancelable(false).setNegativeButton("結束", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }).show();

            }
        }

        @Override
        protected void onCancelled(){
            progressDialog.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(AddHydrantActivity.this);
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
        }


    }

}
