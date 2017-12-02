package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
        , EasyPermissions.PermissionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_SPEECH_RECOGNIZE=1004;
    static final int REQUEST_PLACE_AUTOCOMPLETE=1005;


    SQLiteDatabase db;

    GoogleAccountCredential mCredential;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    ProgressDialog progressDialog;
    GooglePlayService checkServices;

    private GoogleApiClient mGoogleApiClient;

    PlaceAutocompleteFragment autocompleteFragment;

    ImageView btnMyLocation;
    ImageView btnDirect;
    ImageButton speechButton;
    ImageButton clearButton;
    EditText placeAutoInput;
    ImageView scanImage;

    Marker targetPosition;

    final double latlngBoundRange = 0.002;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment.getMapAsync(this);

        btnMyLocation = (ImageView) findViewById(R.id.btn_my_location);
        btnDirect = (ImageView) findViewById(R.id.btn_direct);
        btnDirect.setVisibility(View.INVISIBLE);
        scanImage = (ImageView) findViewById(R.id.scan_image);

        speechButton = (ImageButton)findViewById(R.id.speechButton);
        placeAutoInput = (EditText)findViewById(R.id.placeAuto_input) ;
        clearButton = (ImageButton)findViewById(R.id.placeClearButton);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            placeAutoInput.setFocusable(View.NOT_FOCUSABLE);
        }else{
            placeAutoInput.setFocusable(false);
        }


        final ImageButton fab = (ImageButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                Cursor cs = db.rawQuery("SELECT * FRom sqlite_master",null);
                String result = "";
                if(cs.moveToFirst()) {
                    do {
                        for(int i = 0;i<cs.getColumnCount();i++){
                            result+=","+cs.getString(i);
                        }
                        result+="\n";
                    } while (cs.moveToNext());
                }
                builder.setMessage(result).show();*/
                showNearHydrant();
                Animation showScan = AnimationUtils.loadAnimation(MainActivity.this,R.anim.show_scan);
                scanImage.setVisibility(View.VISIBLE);
                scanImage.startAnimation(showScan);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanImage.setVisibility(View.INVISIBLE);
                    }
                },1300);

            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        db = new MyDBHelper(this).getWritableDatabase();

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());

        if (!EasyPermissions.hasPermissions(getApplicationContext(), new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})) {
            EasyPermissions.requestPermissions(
                    this,
                    "此App必須擁有以下權限",
                    100,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        } else {
            checkDatabaseUpdate();
        }

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

    }

    LatLngBounds currentBound;

    private void resetCurrentBound(LatLng newCenter){
        float zoomref = 18 - mMap.getCameraPosition().zoom;
        double range = latlngBoundRange;

        if(zoomref >0){
            range += 0.001 * zoomref;
        }
        LatLng sw = new LatLng(newCenter.latitude-range-0.00065,newCenter.longitude-range);
        LatLng ne = new LatLng(newCenter.latitude+range+0.00065,newCenter.longitude+range);
        //autocompleteFragment.setBoundsBias(new LatLngBounds(sw,ne));
        currentBound = new LatLngBounds(sw,ne);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera


        /*if (!EasyPermissions.hasPermissions(getApplicationContext(), new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})) {
            EasyPermissions.requestPermissions(
                    this,
                    "此App必須擁有以下權限",
                    100,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }*/


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            EasyPermissions.requestPermissions(
                    this,
                    "此App必須擁有以下權限",
                    100,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                ;
                resetCurrentBound(new LatLng(location.getLatitude(),location.getLongitude()));

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 18, 0, 0)));
            }
        });
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    EasyPermissions.requestPermissions(
                            MainActivity.this,
                            "此App必須擁有以下權限",
                            100,
                            new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});

                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    }
                });
            }
        });

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)),300, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        mMap.animateCamera(CameraUpdateFactory.scrollBy(0,-300),300,null);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                marker.showInfoWindow();

                if(btnDirect.getVisibility()==View.INVISIBLE) {
                    btnDirect.setVisibility(View.VISIBLE);
                    Animation animation_in = AnimationUtils.makeInAnimation(MainActivity.this,false);
                    btnDirect.startAnimation(animation_in);
                }
                btnDirect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+marker.getPosition().latitude+","+marker.getPosition().longitude+"&mode=w");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                });

                return true;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(btnDirect.getVisibility()==View.VISIBLE) {
                    Animation animation_out = AnimationUtils.makeOutAnimation(MainActivity.this, true);
                    btnDirect.startAnimation(animation_out);
                    btnDirect.setVisibility(View.INVISIBLE);
                }

                btnDirect.setOnClickListener(null);
            }
        });
        mMap.setInfoWindowAdapter(new HydrantInfoWindowAdapter(this));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(MainActivity.this,"欲編輯請長按",Toast.LENGTH_LONG).show();
            }
        });

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                final Hydrant hydrant = (Hydrant)marker.getTag();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("您確定要編輯 編號:"+hydrant.getId()+" 的消防栓狀態嗎?").setNegativeButton("取消",null)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cs = db.query(MyDBHelper.TABLE_TEMP,new String[] {MyDBHelper.COLUMN_ID},null,null,null,null,null);
                                if(cs.getCount()>0){

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                                    builder1.setMessage("發現有上次未儲存的資料，\n請問是否一起編輯?")
                                            .setNeutralButton("取消", null)
                                            .setNegativeButton("刪除後繼續", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    db.delete(MyDBHelper.TABLE_TEMP,null,null);
                                                    MyDBHelper.replaceTempHydrant(db,hydrant);
                                                    Intent intent = new Intent();
                                                    intent.setClass(MainActivity.this,InvestgationActivity.class);
                                                    startActivity(intent);
                                                }
                                            }).setPositiveButton("一併編輯", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            MyDBHelper.replaceTempHydrant(db,hydrant);
                                            Intent intent = new Intent();
                                            intent.setClass(MainActivity.this,InvestgationActivity.class);
                                            startActivity(intent);
                                        }
                                    }).show();
                                }else {
                                    MyDBHelper.replaceTempHydrant(db,hydrant);
                                    Intent intent = new Intent();
                                    intent.setClass(MainActivity.this,InvestgationActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }).show();

            }

        });

        //autocompleteFragment = (PlaceAutocompleteFragment)
                //getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        /*
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                if(targetPosition!=null){
                    targetPosition.remove();
                    targetPosition = null;
                    targetAnime.removeCallbacks(targetAnimeTask);
                }
                targetPosition = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(Hydrant.bitmapDescriptorFromVector(MainActivity.this,R.drawable.ic_target_icon)).anchor(0.5f,0.5f).alpha(0.2f));
                targetAnime.post(targetAnimeTask);
                Toast.makeText(MainActivity.this,place.getAddress(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.

            }
        });*/

        //autocompleteFragment.setHint("目標地點");


        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng currentTarget = mMap.getCameraPosition().target;
                resetCurrentBound(currentTarget);
            }
        });


        /*autocompleteFragment.getActivity().findViewById(R.id.place_autocomplete_search_button).setLayoutParams(new LinearLayout.LayoutParams(120,120));
        autocompleteFragment.getActivity().findViewById(R.id.place_autocomplete_clear_button).setLayoutParams(new LinearLayout.LayoutParams(120,120));
        autocompleteFragment.getActivity().findViewById(R.id.place_autocomplete_clear_button).setVisibility(View.INVISIBLE);
        ((EditText) autocompleteFragment.getActivity().findViewById(R.id.place_autocomplete_search_input)).setTextSize(18);*/


        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                    EasyPermissions.requestPermissions(
                            MainActivity.this,
                            "須要錄音權限才可以使用語音功能",
                            100,
                            new String[]{ Manifest.permission.RECORD_AUDIO});

                    return;
                }
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"zh-TW");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"請說出目的地");
                startActivityForResult(intent,REQUEST_SPEECH_RECOGNIZE);
            }
        });

        placeAutoInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(placeAutoInput.getText().toString()!="" && targetPosition != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(targetPosition.getPosition()));
                }else{
                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setBoundsBias(currentBound).build(MainActivity.this);
                        startActivityForResult(intent,REQUEST_PLACE_AUTOCOMPLETE);
                    }catch (Exception e){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("錯誤").setMessage(e.getMessage()).setPositiveButton("確認",null).show();
                    }
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(targetPosition!= null){
                    targetPosition.remove();
                    targetPosition = null;
                    targetAnime.removeCallbacks(targetAnimeTask);
                }
                placeAutoInput.setText("");
                //autocompleteFragment.getActivity().findViewById(R.id.place_autocomplete_clear_button).callOnClick();

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }
    int pressedTimes = 0;

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(pressedTimes ==0){
                Toast.makeText(this,"再按一次結束程式",Toast.LENGTH_SHORT).show();
                pressedTimes++;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        pressedTimes = 0;
                    }
                },2000);
            }else if(pressedTimes>0){
                finish();
                System.exit(0);
            }
        }
    }

    Handler targetAnime = new Handler();

    Runnable targetAnimeTask = new Runnable() {
        boolean isIncrease = true;
        @Override
        public void run() {
            if(targetPosition != null){
                if(isIncrease){
                    targetPosition.setAlpha(targetPosition.getAlpha()+0.05f);
                }else{
                    targetPosition.setAlpha(targetPosition.getAlpha()-0.05f);
                }
                if(targetPosition.getAlpha()>=0.9f || targetPosition.getAlpha()<=0.2)
                    isIncrease = !isIncrease;

                targetAnime.postDelayed(this,25);

            }
        }
    };

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
            intent.putExtra(ManualActivity.ARG_MANUAL_TYPE,ManualActivity.MANUAL_TYPE_MAIN);
            startActivity(intent);

            return true;
        }else if(id==R.id.action_update){
            checkDatabaseUpdate();

            return true;
        }else if(id==R.id.action_about){
            String messge;
            try {
                messge = "應用程式版本:" + getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }catch (PackageManager.NameNotFoundException e){
                messge = e.getMessage()+TextUtils.join("\n",e.getStackTrace());
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("關於").setMessage(messge).setPositiveButton("關閉",null).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_investigation) {
            Intent intent = new Intent();
            intent.setClass(this, QueryActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.nav_history) {

            Intent intent = new Intent();
            intent.setClass(this, HistoryActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.nav_search) {
            Intent intent = new Intent();
            intent.setClass(this, SearchActivity.class);
            startActivity(intent);

            return true;

        }else if(id==R.id.nav_add_hydrant){
            Intent intent = new Intent();
            intent.setClass(this, AddHydrantActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_main);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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
                        ContentValues cv =new ContentValues();
                        cv.put(MyDBHelper.COLUMN_ID,1);
                        cv.put(MyDBHelper.COLUMN_NAME,accountName);
                        db.insert(MyDBHelper.TABLE_ACCOUNT_NAME,null,cv);
                        mCredential.setSelectedAccountName(accountName);
                    }
                    checkDatabaseUpdate();

                }
                break;
            case REQUEST_PLACE_AUTOCOMPLETE:
                if(resultCode == RESULT_OK){
                    Place place = PlaceAutocomplete.getPlace(this,data);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    if(targetPosition!=null){
                        targetPosition.remove();
                        targetPosition = null;
                        targetAnime.removeCallbacks(targetAnimeTask);
                    }
                    targetPosition = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(Hydrant.bitmapDescriptorFromVector(MainActivity.this,R.drawable.ic_target_icon)).anchor(0.5f,0.5f).alpha(0.2f));
                    targetAnime.post(targetAnimeTask);
                    placeAutoInput.setText(place.getName().toString());
                    resetCurrentBound(place.getLatLng());
                    showNearHydrant();
                    Toast.makeText(MainActivity.this,place.getAddress(),Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_SPEECH_RECOGNIZE:
                if(resultCode == RESULT_OK && data!=null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(result.size()>0) {

                        /*autocompleteFragment.setText(result.get(0));
                        ((EditText) autocompleteFragment.getActivity().findViewById(R.id.place_autocomplete_search_input)).callOnClick();*/
                        try {
                            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .setBoundsBias(currentBound)
                                    .zzdB(result.get(0))
                                    .build(MainActivity.this);
                            startActivityForResult(intent,REQUEST_PLACE_AUTOCOMPLETE);
                        }catch (Exception e){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("錯誤").setMessage(e.getMessage()).setPositiveButton("確認",null).show();
                        }
                    }
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        if(requestCode ==100 ) {
            onMapReady(mMap);
            checkDatabaseUpdate();
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        android.os.Process.killProcess(android.os.Process.myPid());
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
                    checkDatabaseUpdate();
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
            checkDatabaseUpdate();
        }
    }


    private void checkDatabaseUpdate(){

        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("檢查更新");
            progressDialog.setMessage("正在更新資料庫...\n第一次更新可能會花比較多時間，請耐心等候");
            progressDialog.setCancelable(false);
        }

        if(hasPermission() && !progressDialog.isShowing()) {

            progressDialog.show();


            //new checkUpdate().execute();
            new checkServerMessge().execute();
        }

    }

    HashMap<Integer,Marker> mMarker;

    private void showNearHydrant(){

        resetCurrentBound(mMap.getCameraPosition().target);
        final ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
        final ArrayList<Hydrant> hydrants = new ArrayList<>();

        if(mMarker==null){
            mMarker = new HashMap<>();
        }

        HashMap<Integer,Integer> tempInt = new HashMap<>();

        Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{"*"},
                MyDBHelper.COLUMN_LATITUDE+" >= "+currentBound.southwest.latitude+
                " AND "+MyDBHelper.COLUMN_LATITUDE + " <= "+currentBound.northeast.latitude +
                " AND "+MyDBHelper.COLUMN_LONGITUDE + " <= " + currentBound.northeast.longitude+
                " AND " + MyDBHelper.COLUMN_LONGITUDE + " >= " + currentBound.southwest.longitude
                ,null,null,null,MyDBHelper.COLUMN_LATITUDE+" DESC , "+MyDBHelper.COLUMN_LONGITUDE +" ASC");

        if(cs.moveToFirst()&&cs.getCount()>0){
            do{

                Hydrant hydrant = new Hydrant(cs);
                if(!hydrant.getStates().contains(Hydrant.STATES.REMOVED)) {
                    tempInt.put(hydrant.getId(), null);

                    if (!mMarker.containsKey(hydrant.getId())) {
                        MarkerOptions tempOptions = new MarkerOptions();

                        tempOptions.position(hydrant.getLatlng()).draggable(false).visible(true).icon(hydrant.getBitmapDescriptor(this));

                        markerOptions.add(tempOptions);
                        hydrants.add(hydrant);

                    }
                }
            }while(cs.moveToNext());
        }

        if(markerOptions.size()>0) {
            final Handler showMarkerHandler = new Handler();

            showMarkerHandler.postDelayed(new Runnable() {
                int i = 0 ;
                int range = 0;
                @Override
                public void run() {
                    if(i==0){
                        range = markerOptions.size()/20;
                    }
                    for(int j =0;j<=range && i < markerOptions.size(); i++,j++) {
                        Marker m = mMap.addMarker(markerOptions.get(i));
                        m.setTag(hydrants.get(i));
                        m.setTitle(((hydrants.get(i).getType() == Hydrant.TYPE.UNDER) ? "地下式" :
                                ((hydrants.get(i).getType() == Hydrant.TYPE.GROUND) ? "地上式" :
                                        "地下式(新式開關)"))
                                + "消防栓 (編號:" + new DecimalFormat("0000").format(hydrants.get(i).getId()) + ")");
                        mMarker.put(hydrants.get(i).getId(), m);
                    }

                    if(i<markerOptions.size())
                        showMarkerHandler.postDelayed(this,20);
                }
            },20);
        }
        ArrayList<Integer> removedKey = new ArrayList<>();
        for(Integer key:mMarker.keySet()){
            if(!tempInt.containsKey(key)){
                mMarker.get(key).remove();
                removedKey.add(key);
            }
        }

        for (Integer key : removedKey)
            mMarker.remove(key);

    }

    private void checkUnSavedData(){
        Cursor cs = db.query(MyDBHelper.TABLE_TEMP,new String[]{MyDBHelper.COLUMN_ID},null,null,null,null,null);
        if(cs.getCount()>0){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setMessage("發現有上次未儲存的資料，\n請問是否繼續編輯?")
                    .setNeutralButton("取消", null)
                    .setNegativeButton("放棄資料", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.delete(MyDBHelper.TABLE_TEMP,null,null);
                        }
                    }).setPositiveButton("繼續編輯", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this,InvestgationActivity.class);
                            startActivity(intent);
                    }
            }).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class checkUpdate extends AsyncTask<Void,Void,Integer>{

        private Exception mError = null;

        @Override
        protected Integer doInBackground(Void... params) {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleSheetConnecter gsc = new GoogleSheetConnecter(mCredential,transport,jsonFactory);



            try{
                return GoogleSheetConnecter.updateDBFromSheet(db,gsc);
            }catch (Exception e){
                mError = e;
                cancel(true);

                return null;
            }


        }

        @Override
        protected void onPostExecute(Integer output) {
            progressDialog.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            if(output>=0) {
                Toast.makeText(MainActivity.this,"資料更新完成",Toast.LENGTH_SHORT).show();
                showNearHydrant();
                checkUnSavedData();
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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
                            +TextUtils.join("\n",mError.getStackTrace())+ mError.getMessage()).setTitle("取消").show();
                }
            } else {
                dialog.setMessage("Request cancelled.").setTitle("取消").show();
            }
        }


    }

    private class checkServerMessge extends AsyncTask<Void,Void,ValueRange>{

        private Exception mError = null;

        @Override
        protected ValueRange doInBackground(Void... params) {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleSheetConnecter gsc = new GoogleSheetConnecter(mCredential,transport,jsonFactory);

            try{


                return gsc.Get(GoogleSheetConnecter.RANGE_SERVER_MESSEGE,GoogleSheetConnecter.ValueRenderOption.FORMATTED_VALUE,GoogleSheetConnecter.DateTimeRenderOption.FORMATTED_STRING);
            }catch (Exception e){
                mError = e;
                cancel(true);

                return null;
            }


        }

        @Override
        protected void onPostExecute(ValueRange output) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            if(output.getValues()!=null) {
                String messege = output.getValues().get(0).get(0).toString();
                if (messege.compareTo("") != 0) {
                    progressDialog.cancel();
                    dialog.setTitle("公告").setMessage(messege).setCancelable(false).setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (progressDialog == null) {
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setTitle("檢查更新");
                                progressDialog.setMessage("正在更新資料庫...\n第一次更新可能會花比較多時間，請耐心等候");
                                progressDialog.setCancelable(false);
                            }

                            if (hasPermission() && !progressDialog.isShowing()) {

                                progressDialog.show();


                                new checkApplicationVersion().execute();
                                //new checkApplicationUpdate().execute();
                            }

                        }
                    }).show();
                } else {
                    new checkApplicationVersion().execute();
                }
            }else {
                new checkApplicationVersion().execute();
            }
        }

        @Override
        protected void onCancelled(){
            progressDialog.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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
                            +TextUtils.join("\n",mError.getStackTrace())+ mError.getMessage()).setTitle("取消").show();
                }
            } else {
                dialog.setMessage("Request cancelled.").setTitle("取消").show();
            }
        }


    }

    private class checkApplicationVersion extends AsyncTask<Void,Void,ValueRange>{

        private Exception mError = null;

        @Override
        protected ValueRange doInBackground(Void... params) {

            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleSheetConnecter gsc = new GoogleSheetConnecter(mCredential,transport,jsonFactory);
            try{
                ValueRange result = gsc.Get(GoogleSheetConnecter.RANGE_VERSION,GoogleSheetConnecter.ValueRenderOption.FORMATTED_VALUE,GoogleSheetConnecter.DateTimeRenderOption.FORMATTED_STRING);
                if(result.getValues()!=null) {
                    int newVersion = Integer.valueOf(result.getValues().get(0).get(0).toString());
                    int currVersion = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                    if (newVersion > currVersion) {
                        return gsc.Get(GoogleSheetConnecter.TABLE_LOG+"!"+GoogleSheetConnecter.COLUMN_VERSION_CODE+currVersion
                                +":"+GoogleSheetConnecter.COLUMN_VERSION_CHANGELOG+newVersion,GoogleSheetConnecter.ValueRenderOption.FORMATTED_VALUE,GoogleSheetConnecter.DateTimeRenderOption.FORMATTED_STRING);

                    }
                }
                return null;

            }catch (IOException e){
                mError = e;
                cancel(true);
                return null;
            } catch (PackageManager.NameNotFoundException e) {
                mError = e;
                cancel(true);
                return null;
            }


        }

        @Override
        protected void onPostExecute(ValueRange output) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            if(output != null) {
                progressDialog.cancel();
                List<List<Object>> values = output.getValues();
                String messege = "目前版本:"+values.get(0).get(1).toString()+"\n"
                        +"最新版本:"+values.get(values.size()-1).get(1).toString()+"\n"
                        +"更新日誌:\n";
                for(int i = 1 ; i<values.size();i++){
                    List<Object> row = values.get(i);
                    if(row.size()>2){
                        messege+="["+row.get(1).toString()+"]\n"+row.get(2).toString()+"\n";
                    }
                }

                dialog.setTitle("有新的更新").setMessage(messege).setCancelable(false).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setTitle("檢查更新");
                            progressDialog.setMessage("正在更新資料庫...\n第一次更新可能會花比較多時間，請耐心等候");
                            progressDialog.setCancelable(false);
                        }
                         if (hasPermission() && !progressDialog.isShowing()) {
                             progressDialog.show();

                             new checkUpdate().execute();
                             //new checkApplicationUpdate().execute();
                         }
                    }}).setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse(getResources().getString(R.string.apk_release_url));
                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        startActivity(intent);
                        MainActivity.this.finish();
                    }
                }).show();



                return;
            }
            new checkUpdate().execute();

        }

        @Override
        protected void onCancelled(){
            progressDialog.cancel();
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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
                            +TextUtils.join("\n",mError.getStackTrace())+ mError.getMessage()).setTitle("取消").show();
                }
            } else {
                dialog.setMessage("Request cancelled.").setTitle("取消").show();
            }
        }


    }


   public static class HydrantInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

       Activity contxt;

       public HydrantInfoWindowAdapter(Activity context){
           this.contxt = context;
       }

       @Override
       public View getInfoWindow(Marker marker) {

           return null;
       }

       @Override
       public View getInfoContents(final Marker marker) {
           View view = contxt.getLayoutInflater().inflate(R.layout.hydrant_info_window,null);

           final Hydrant hydrant = (Hydrant)marker.getTag();
           if(hydrant!=null) {
               TextView textViewStates = (TextView) view.findViewById(R.id.textView_States);
               TextView textViewHasMarked = (TextView) view.findViewById(R.id.textView_hasMarked);
               TextView textViewMarkedStates = (TextView) view.findViewById(R.id.textView_markedStates);
               TextView textViewDist = (TextView) view.findViewById(R.id.textView_dist);
               TextView textViewVil = (TextView) view.findViewById(R.id.textView_vil);
               TextView textViewAddress = (TextView) view.findViewById(R.id.textView_address);
               TextView textViewPs = (TextView) view.findViewById(R.id.textView_ps);
               TextView textViewTitle = (TextView) view.findViewById(R.id.textView_infoTitle);
               TextView textViewMarkedStatesTag = (TextView) view.findViewById(R.id.textView_MarkedStatesTag);
               TextView textViewDuty = (TextView) view.findViewById(R.id.textView_duty);
               ImageView trafficLevelSection1 = (ImageView) view.findViewById(R.id.trafficLevel_Section1);
               ImageView trafficLevelSection2 = (ImageView) view.findViewById(R.id.trafficLevel_Section2);
               ImageView trafficLevelSection3 = (ImageView) view.findViewById(R.id.trafficLevel_Section3);
               ImageView trafficLevel1 = (ImageView) view.findViewById(R.id.trafficLevel_level1);
               ImageView trafficLevel2 = (ImageView) view.findViewById(R.id.trafficLevel_level2);
               ImageView trafficLevel3 = (ImageView) view.findViewById(R.id.trafficLevel_level3);
               ImageView trafficLevel4 = (ImageView) view.findViewById(R.id.trafficLevel_level4);
               TextView trafficLevelText = (TextView) view.findViewById(R.id.trafficLevel_text);



               textViewTitle.setText(marker.getTitle());
               textViewStates.setText(hydrant.pharseStatesToString());
               textViewHasMarked.setText(hydrant.hasMark() ? "有設置" : "無設置");
               if (hydrant.hasMark()) {
                   textViewMarkedStates.setText(hydrant.getMark_States());
               } else {
                   textViewMarkedStates.setVisibility(View.INVISIBLE);
                   textViewMarkedStatesTag.setVisibility(View.INVISIBLE);
               }
               textViewDist.setText(hydrant.getChineseDist(hydrant.getDist()));
               textViewVil.setText(hydrant.getVil());
               textViewAddress.setText(hydrant.getAddress());
               textViewPs.setText(hydrant.getPs());
               textViewDuty.setText(Hydrant.DUTY.getDutyName(hydrant.getDutyCode()));

               Drawable lv1Drawable = trafficLevel1.getBackground();
               Drawable lv2Drawable = trafficLevel2.getBackground();
               Drawable lv3Drawable = trafficLevel3.getBackground();
               Drawable lv4Drawable = trafficLevel4.getBackground();
               Drawable section1Drawable = trafficLevelSection1.getDrawable();
               Drawable section2Drawable = trafficLevelSection2.getDrawable();
               Drawable section3Drawable = trafficLevelSection3.getDrawable();


               DrawableCompat.wrap(lv1Drawable).mutate();
               DrawableCompat.wrap(lv2Drawable).mutate();
               DrawableCompat.wrap(lv3Drawable).mutate();
               DrawableCompat.wrap(lv4Drawable).mutate();
               DrawableCompat.wrap(section1Drawable).mutate();
               DrawableCompat.wrap(section2Drawable).mutate();
               DrawableCompat.wrap(section3Drawable).mutate();

               switch (hydrant.getTrafficLevel()){
                   case 1:


                       DrawableCompat.setTint(lv1Drawable, contxt.getResources().getColor( R.color.colorPrimaryDark));
                       DrawableCompat.setTint(lv2Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                       DrawableCompat.setTint(lv3Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                       DrawableCompat.setTint(lv4Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                       DrawableCompat.setTint(section1Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                       DrawableCompat.setTint(section2Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                       DrawableCompat.setTint(section3Drawable, contxt.getResources().getColor( R.color.colorStateBroken));



                       trafficLevelText.setText("僅能移動式幫浦通行");
                       break;
                   case 2:

                           DrawableCompat.setTint(lv1Drawable, contxt.getResources().getColor( R.color.dangerOrange));
                           DrawableCompat.setTint(lv2Drawable, contxt.getResources().getColor( R.color.dangerOrange));
                           DrawableCompat.setTint(lv3Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                           DrawableCompat.setTint(lv4Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                           DrawableCompat.setTint(section1Drawable, contxt.getResources().getColor( R.color.dangerOrange));
                           DrawableCompat.setTint(section2Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                           DrawableCompat.setTint(section3Drawable, contxt.getResources().getColor( R.color.colorStateBroken));


                       trafficLevelText.setText("小型水泵車 可通行");

                       break;
                   case 3:
                           DrawableCompat.setTint(lv1Drawable, contxt.getResources().getColor( R.color.nomal_green));
                           DrawableCompat.setTint(lv2Drawable, contxt.getResources().getColor( R.color.nomal_green));
                           DrawableCompat.setTint(lv3Drawable, contxt.getResources().getColor( R.color.nomal_green));
                           DrawableCompat.setTint(lv4Drawable, contxt.getResources().getColor( R.color.colorStateBroken));
                           DrawableCompat.setTint(section1Drawable, contxt.getResources().getColor( R.color.nomal_green));
                           DrawableCompat.setTint(section2Drawable, contxt.getResources().getColor( R.color.nomal_green));
                           DrawableCompat.setTint(section3Drawable, contxt.getResources().getColor( R.color.colorStateBroken));

                           trafficLevelText.setText("水箱車 可通行");


                       break;
                   case 4:

                           DrawableCompat.setTint(lv1Drawable, contxt.getResources().getColor( R.color.good_green));
                           DrawableCompat.setTint(lv2Drawable, contxt.getResources().getColor( R.color.good_green));
                           DrawableCompat.setTint(lv3Drawable, contxt.getResources().getColor( R.color.good_green));
                           DrawableCompat.setTint(lv4Drawable, contxt.getResources().getColor( R.color.good_green));
                           DrawableCompat.setTint(section1Drawable, contxt.getResources().getColor( R.color.good_green));
                           DrawableCompat.setTint(section2Drawable, contxt.getResources().getColor( R.color.good_green));
                           DrawableCompat.setTint(section3Drawable, contxt.getResources().getColor( R.color.good_green));
                       trafficLevelText.setText("水庫車 可通行");

                       break;
               }


               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                   trafficLevel1.setBackground(lv1Drawable);
                   trafficLevel2.setBackground(lv2Drawable);
                   trafficLevel3.setBackground(lv3Drawable);
                   trafficLevel4.setBackground(lv4Drawable);
               }else{
                   trafficLevel1.setBackgroundDrawable(lv1Drawable);
                   trafficLevel2.setBackgroundDrawable(lv2Drawable);
                   trafficLevel3.setBackgroundDrawable(lv3Drawable);
                   trafficLevel4.setBackgroundDrawable(lv4Drawable);
               }
               trafficLevelSection1.setImageDrawable(section1Drawable);
               trafficLevelSection2.setImageDrawable(section2Drawable);
               trafficLevelSection3.setImageDrawable(section3Drawable);


           }
           return view;
       }
   }
}

