package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class InvestgationActivity extends AppCompatActivity implements OnMapReadyCallback, ViewPager.OnPageChangeListener, GoogleMap.OnMarkerClickListener,EasyPermissions.PermissionCallbacks {

    SQLiteDatabase db;
    ViewPager viewPager;
    protected GoogleMap mMap;
    View include;
    ImageView zoomInButton;
    ImageView zoomOutButton;
    ImageView markerOutline;


    private FusedLocationProviderClient mFusedLocationClient;
    public ArrayList<Hydrant> mHydrants;
    protected ArrayList<Marker> mMarker = new ArrayList<>();
    InvestgationPagerAdapter pagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investgation);
        viewPager = (ViewPager) findViewById(R.id.investgationViewPager);
        include = findViewById(R.id.investgationInclude);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageView locationTargetMarker = (ImageView) findViewById(R.id.investgationTargetMarker);
        locationTargetMarker.setVisibility(View.INVISIBLE);
        markerOutline = (ImageView) findViewById(R.id.investgationMarkerOutline);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

    public void setNewMarkerPosition(int index, LatLng newPosition) {
        mMarker.get(index).setPosition(newPosition);
    }

    public void setNewMarkerIcon(int index, BitmapDescriptor bitmapDescriptor) {
        mMarker.get(index).setIcon(bitmapDescriptor);
    }


    private void pulseMarker(final Bitmap markerIcon, final Marker marker, final long onePulseDuration) {
        final Handler handler = new Handler();
        final long startTime = System.currentTimeMillis();
        final Interpolator interpolator = new CycleInterpolator(1f);
        handler.postDelayed(new Runnable() {
            int times = 0;
            float t = 0;
            boolean up = true;

            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(scaleBitmap(markerIcon, 1f + 0.02f * t)));
                if (up)
                    t++;
                else
                    t--;

                if (t >= 5 && up)
                    up = !up;
                if (t < 1 && !up) {
                    up = !up;
                    times++;
                }

                if (times < 2)
                    handler.postDelayed(this, 20);
                else
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerIcon));


            }
        }, 300);
    }

    public Bitmap scaleBitmap(Bitmap bitmap, float scaleFactor) {
        final int sizeX = Math.round(bitmap.getWidth() * scaleFactor);
        final int sizeY = Math.round(bitmap.getHeight() * scaleFactor);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false);
        return bitmapResized;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       /* if (!EasyPermissions.hasPermissions(getApplicationContext(), new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})) {

            return;
        }*/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            EasyPermissions.requestPermissions(
                    this,
                    "此App必須擁有以下權限\n獲取位置\n獲取聯絡人",
                    100,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);

        zoomInButton = (ImageView)findViewById(R.id.investgationZoomIn);
        zoomOutButton = (ImageView)findViewById(R.id.investgationZoomOut);
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });


        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ActivityCompat.checkSelfPermission(InvestgationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(InvestgationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(),location.getLongitude()),mMap.getCameraPosition().zoom,mMap.getCameraPosition().tilt,mMap.getCameraPosition().bearing)));
                    }
                });

                return true;
            }
        });

        db = new MyDBHelper(this).getWritableDatabase();


        Cursor cs = db.query(MyDBHelper.TABLE_TEMP,
                new String[]{"*"}
                ,null,null,null,null,MyDBHelper.COLUMN_ID+MyDBHelper.ASC);


        try {
            mHydrants = new ArrayList<>();
            if(cs.getCount()>0&&cs.moveToFirst()){
                do{

                    Hydrant hydrant = new Hydrant(cs);
                    mHydrants.add(hydrant);
                }while (cs.moveToNext());
            }

            pagerAdapter = new InvestgationPagerAdapter(getSupportFragmentManager(),mHydrants);
            viewPager.setAdapter(pagerAdapter);
            viewPager.addOnPageChangeListener(this);
        }catch (Exception e){
            return;
        }

        int tmpIndex=0;
        for(Hydrant hydrant:mHydrants){

            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(hydrant.getLatlng()).icon(hydrant.getBitmapDescriptor(this));

            Marker m = mMap.addMarker(markerOptions);

            m.setTag(tmpIndex);


            mMarker.add(m);
            tmpIndex++;
        }

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(mMarker.get(0).getPosition(),18,0,0)));
        mMap.setOnMarkerClickListener(this);
        //viewPager.adOnPageChangeListener();

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        if(position != pagerAdapter.getCount()-1) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mMarker.get(position).getPosition()), 300, null);
            Bitmap markIcon = mHydrants.get(position).getIconBitmap(this);
            pulseMarker(markIcon,mMarker.get(position),300);
        }
    }



    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        viewPager.setCurrentItem((int)marker.getTag(),true);
        return false;
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        if(requestCode ==100 ) {

            onMapReady(mMap);
        }

    }


    private class InvestgationPagerAdapter extends FragmentStatePagerAdapter{

        List<Hydrant> hydrant;
        List<Fragment> fragmentsList;
        public InvestgationPagerAdapter(FragmentManager fm,List<Hydrant> hydrant) {
            super(fm);
            this.hydrant = hydrant;
            fragmentsList = new ArrayList<>();
            int i =0;
            for(Hydrant page :hydrant) {
                EditPageViewFragment pageFragment = new EditPageViewFragment();
                Bundle argument = new Bundle();
                argument.putInt(EditPageViewFragment.ARG_HYDRANT_ID,page.getId());
                argument.putInt(EditPageViewFragment.ARG_PAGE_INDEX,i);
                argument.putInt(EditPageViewFragment.ARG_PAGE_COUNT,hydrant.size());

                pageFragment.setArguments(argument);
                fragmentsList.add(pageFragment);
                i++;
            }
            EditCompletePageFragment completePageFragment = new EditCompletePageFragment();
            fragmentsList.add(completePageFragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }
    }

    protected int displayHeight;
    protected int originalHeight;
    protected int fullScreenHeight;
    protected boolean isFullScreen = false;
    protected boolean onWindowSliding= false;

    public static class EditCompletePageFragment extends Fragment {


        ImageView btnConcern;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.edit_complete_page, container, false);

            btnConcern = (ImageView)rootView.findViewById(R.id.investgation_result_btnConcern);
            btnConcern.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(),InvestgationResultActivity.class);
                    intent.putExtra(InvestgationResultActivity.BOOLEAN_ARG_ISINVESTGATION,true);
                    startActivity(intent);

                }
            });

            return rootView;
        }
    }

    public static class EditPageViewFragment extends Fragment implements OnMapReadyCallback {

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
        ImageView buttonLocationConcern;
        ImageView buttonLocationCancel;
        ImageView locationTargetMarker;
        ImageView buttonRevert;
        ImageView buttonWindowControl;
        ImageView buttonNextPage;
        ImageView buttonPrevPage;

        ImageView seekBarImage;
        TextView seekBarText;

        TextView trafficText;
        DiscreteSeekBar trafficSeekBar;

        ViewPager parentViewPager;
        View parentActivityView;
        View investgationInclude;

        InvestgationActivity parentActivity;

        SQLiteDatabase db;


        public static final String ARG_HYDRANT_ID = "hydrantId";
        public static final String ARG_PAGE_INDEX = "pageIndex";
        public static final String ARG_PAGE_COUNT = "pageCount";

        int mPageIndex;
        int pageCount;

        List<Integer> selectedStatesIndex;
        List<String>  statesStringList;

        GoogleMap mMap;
        LatLng hydrantPosition;

        Hydrant mHydrant;
        android.os.Handler handler;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.hydrant_edit_page, container,false);

            SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
            Bundle argument = getArguments();

            db = new MyDBHelper(getContext()).getWritableDatabase();

            Cursor cs = db.query(MyDBHelper.TABLE_TEMP,new String[]{"*"},MyDBHelper.COLUMN_ID+" = "+argument.getInt(ARG_HYDRANT_ID),null,null,null,null);

            if(cs.getCount()>0&&cs.moveToFirst())
                mHydrant = new Hydrant(cs);

            parentActivity = (InvestgationActivity)getActivity();


            buttonLocationCancel = (ImageView) getActivity().findViewById(R.id.investgationCancelButton);
            buttonLocationConcern = (ImageView)getActivity().findViewById(R.id.investgationConcernButton);
            buttonRevert = (ImageView)rootView.findViewById(R.id.editPageRevertButton);

            locationTargetMarker = (ImageView)getActivity().findViewById(R.id.investgationTargetMarker);

            parentViewPager = (ViewPager)getActivity().findViewById(R.id.investgationViewPager);
            parentActivityView = getActivity().findViewById(R.id.investgationTopView);
            investgationInclude = getActivity().findViewById(R.id.investgationInclude);

            textViewTitle = (TextView) rootView.findViewById(R.id.editPageTitle);
            spinnerType= (Spinner) rootView.findViewById(R.id.editPageType);
            editTextPress= (EditText) rootView.findViewById(R.id.editPagePressInputText);
            buttonPressMinus= (ImageButton) rootView.findViewById(R.id.editPagePressMinusButtom);
            buttonPressPlus= (ImageButton) rootView.findViewById(R.id.editPagerPressPlusButtom);
            buttonStates= (Button) rootView.findViewById(R.id.editPageStates);
            spinnerHasMarked= (Spinner) rootView.findViewById(R.id.editPageHasMarked);
            spinnerMakedStates= (Spinner) rootView.findViewById(R.id.editPageMarkedStates);
            editTextMarkedStatesEtc= (EditText) rootView.findViewById(R.id.editPageMarkedStatesEtcInput);
            spinnerDist= (Spinner) rootView.findViewById(R.id.editPageDist);
            spinnerVil= (Spinner) rootView.findViewById(R.id.editPageVil);
            editTextAddress= (EditText) rootView.findViewById(R.id.editPageAddress);
            imageViewLocationButtom= (ImageView) rootView.findViewById(R.id.editPageLocation);
            editTextPs= (EditText) rootView.findViewById(R.id.editPagePs);

            seekBarImage=(ImageView)getActivity().findViewById(R.id.investgetionSeekImage);
            seekBarText = (TextView) getActivity().findViewById(R.id.investgationSeekText);

            trafficText = (TextView)rootView.findViewById(R.id.editPageTraficText);
            trafficSeekBar = (DiscreteSeekBar)rootView.findViewById(R.id.editPageTrafficSeek);

            buttonWindowControl =(ImageView)rootView.findViewById(R.id.editPageBtnWindowControl);
            buttonWindowControl.setImageResource((parentActivity.isFullScreen)?android.R.drawable.arrow_up_float:android.R.drawable.arrow_down_float);

            buttonNextPage = (ImageView)rootView.findViewById(R.id.editPageNextButton);
            buttonPrevPage = (ImageView)rootView.findViewById(R.id.editPagePrevButton);





            buttonWindowControl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(parentActivity.originalHeight==0)
                        parentActivity.originalHeight = investgationInclude.getHeight();
                    if(parentActivity.fullScreenHeight==0){
                        int[] screenLoacation = new int[2];
                        investgationInclude.getLocationOnScreen(screenLoacation);
                        final int idHeight = getActivity().findViewById(R.id.editPageConstrantLayout).getHeight()-getActivity().findViewById(R.id.editPageScrollView).getHeight();
                        parentActivity.fullScreenHeight = parentActivity.displayHeight-screenLoacation[1]-idHeight;
                    }

                    if(!parentActivity.onWindowSliding){
                        slideWindow(!parentActivity.isFullScreen);
                    }
                }
            });



            mPageIndex = argument.getInt(ARG_PAGE_INDEX);
            pageCount = argument.getInt(ARG_PAGE_COUNT);

            textViewTitle.setText(new DecimalFormat("0000").format(mHydrant.getId()));
            textViewTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.onPageSelected(mPageIndex);
                }
            });


            initialViewUi();
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

            parentActivity.displayHeight = dm.heightPixels;


            buttonLocationCancel.setVisibility(View.INVISIBLE);
            buttonLocationConcern.setVisibility(View.INVISIBLE);
            imageViewLocationButtom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int[] screenLoacation = new int[2];
                    investgationInclude.getLocationOnScreen(screenLoacation);
                    final int idHeight = getActivity().findViewById(R.id.editPageConstrantLayout).getHeight()-getActivity().findViewById(R.id.editPageScrollView).getHeight();
                    final int originalHeight = investgationInclude.getHeight();
                    final int fullScreenHeight = parentActivity.displayHeight-screenLoacation[1]-idHeight;
                    final int width = investgationInclude.getWidth();


                    buttonLocationConcern.setVisibility(View.VISIBLE);
                    buttonLocationCancel.setVisibility(View.VISIBLE);

                    buttonWindowControl.setEnabled(false);
                    parentActivity.mMap.setOnMarkerClickListener(null);

                    String s = "";
                    for (int i =0;i<screenLoacation.length;i++)
                            s+="  i:"+i+"="+screenLoacation[i];
                    final ConstraintLayout.LayoutParams originLayoutParams = new ConstraintLayout.LayoutParams((ConstraintLayout.LayoutParams) parentViewPager.getLayoutParams());

                    //investgationInclude.setLayoutParams(new ConstraintLayout.LayoutParams(investgationInclude.getLayoutParams().width,displayHeight-screenLoacation[1]-idHeight));
                    Timer timer = new Timer();
                    handler = new android.os.Handler(){
                        @Override
                        public void handleMessage(Message msg){
                            int currentHeight = msg.arg1;
                            investgationInclude.setLayoutParams(new ConstraintLayout.LayoutParams(width,currentHeight));

                        }
                    };
                    AnimateToChangeHeight animator = new AnimateToChangeHeight(fullScreenHeight,investgationInclude.getHeight(),width,timer);
                    timer.schedule(animator,0,10);

                    Animation fadeIn = AnimationUtils.loadAnimation(getContext(),R.anim.alpha);
                    fadeIn.setDuration(AnimateToChangeHeight.totalTimes*10);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                                locationTargetMarker.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    locationTargetMarker.startAnimation(fadeIn);

                    buttonLocationConcern.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setNewLatLng();
                            mHydrant.setLatlng(mMap.getCameraPosition().target);
                            ContentValues cv = new ContentValues();
                            cv.put(MyDBHelper.COLUMN_LATITUDE,mHydrant.getLatlng().latitude);
                            cv.put(MyDBHelper.COLUMN_LONGITUDE,mHydrant.getLatlng().longitude);
                            db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);

                            buttonWindowControl.setEnabled(true);
                            parentActivity.mMap.setOnMarkerClickListener(parentActivity);


                            Timer timer = new Timer();
                            AnimateToChangeHeight animator = new AnimateToChangeHeight(originalHeight,investgationInclude.getHeight(),width,timer);
                            timer.schedule(animator,0,10);
                            Animation fadeOut = AnimationUtils.loadAnimation(getContext(),R.anim.alpha_reverse);
                            fadeOut.setDuration(AnimateToChangeHeight.totalTimes*10);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    locationTargetMarker.setVisibility(View.INVISIBLE);
                                    buttonLocationCancel.setVisibility(View.INVISIBLE);
                                    buttonLocationConcern.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            locationTargetMarker.startAnimation(fadeOut);
                        }
                    });

                    buttonLocationCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            buttonWindowControl.setEnabled(true);
                            parentActivity.mMap.setOnMarkerClickListener(parentActivity);



                            Timer timer = new Timer();
                            AnimateToChangeHeight animator = new AnimateToChangeHeight(originalHeight,investgationInclude.getHeight(),width,timer);
                            timer.schedule(animator,0,10);
                            Animation fadeOut = AnimationUtils.loadAnimation(getContext(),R.anim.alpha_reverse);
                            fadeOut.setDuration(AnimateToChangeHeight.totalTimes*10);
                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    locationTargetMarker.setVisibility(View.INVISIBLE);
                                    buttonLocationCancel.setVisibility(View.INVISIBLE);
                                    buttonLocationConcern.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            locationTargetMarker.startAnimation(fadeOut);
                        }
                    });

                }
            });


            buttonRevert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("重設此消防栓狀態的變更?\n(您剛才的變更將不會保留)").setNegativeButton("取消",null)
                            .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{"*"},MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null,null,null,null);
                                    if(cs.moveToFirst()){
                                        mHydrant = new Hydrant(cs);
                                    }
                                    initialViewUi();
                                    parentActivity.setNewMarkerPosition(mPageIndex,hydrantPosition);

                                }
                            }).show();
                }
            });

            parentActivity.mHydrants.set(mPageIndex,mHydrant);


            return rootView;
        }

        private void initialViewUi() {

            buttonPrevPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentViewPager.setCurrentItem(mPageIndex-1,true);
                }
            });

            buttonNextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentViewPager.setCurrentItem(mPageIndex+1,true);
                }
            });
            if(mPageIndex==0){
                buttonPrevPage.setVisibility(View.INVISIBLE);
            }else{
                buttonPrevPage.setVisibility(View.VISIBLE);
            }
            final int oriPadding = buttonNextPage.getPaddingBottom();
            if(mPageIndex==pageCount-1){
                buttonNextPage.setImageResource(R.drawable.concern_button_style);
                buttonNextPage.setPadding(0,0,0,0);
                buttonNextPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(getActivity(),InvestgationResultActivity.class);
                        intent.putExtra(InvestgationResultActivity.BOOLEAN_ARG_ISINVESTGATION,true);
                        startActivity(intent);
                    }
                });
            }else{
                buttonNextPage.setImageResource(R.drawable.next_image);
                buttonNextPage.setPadding(oriPadding,oriPadding,oriPadding,oriPadding);
                buttonNextPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentViewPager.setCurrentItem(mPageIndex+1,true);
                    }
                });

            }

            setSpinnerType();


            editTextPress.setText(mHydrant.getPress());
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
                        ContentValues cv = new ContentValues();
                        cv.put(MyDBHelper.COLUMN_PRESS,mHydrant.getPress());
                        db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);

                    }
                }
            });


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



            setButtonStates();


            setSpinnerMarkState();



            List<String> distList = new ArrayList<>();
            final List<Integer> allDist = Hydrant.DIST.getAllDist();

            for(int i  :allDist )
                distList.add(Hydrant.getChineseDist(i));
            spinnerDist.setAdapter(new ArrayAdapter<String>(this.getContext(),R.layout.simple_list_item_1,distList));
            spinnerDist.setSelection( allDist.indexOf(mHydrant.getDist()));
            spinnerDist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    spinnerVil.setAdapter(new ArrayAdapter<String>(EditPageViewFragment.this.getActivity(),R.layout.simple_list_item_1,Hydrant.VIL.getVil(allDist.get(position))));

                    int currentVilIndex=0;
                    List<String> vilList = Hydrant.VIL.getVil(mHydrant.getDist());
                    for(int i =0;i<vilList.size();i++){
                        if(vilList.get(i).compareTo(mHydrant.getVil())==0){
                            currentVilIndex = i;
                            break;
                        }
                    }
                    spinnerVil.setSelection(currentVilIndex);
                    mHydrant.setDist(allDist.get(position));
                    ContentValues cv = new ContentValues();
                    cv.put(MyDBHelper.COLUMN_DIST,mHydrant.getDist());
                    db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spinnerVil.setAdapter(null);
                }
            });


            spinnerVil.setAdapter(new ArrayAdapter<String>(this.getActivity(),R.layout.simple_list_item_1,Hydrant.VIL.getVil(mHydrant.getDist())));
            int currentVilIndex=0;
            List<String> vilList = Hydrant.VIL.getVil(mHydrant.getDist());
            for(int i =0;i<vilList.size();i++){
                if(vilList.get(i).compareTo(mHydrant.getVil())==0){
                    currentVilIndex = i;
                    break;
                }
            }
            spinnerVil.setSelection(currentVilIndex);
            spinnerVil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    mHydrant.setVil(Hydrant.VIL.getVil(mHydrant.getDist()).get(position));
                    ContentValues cv = new ContentValues();
                    cv.put(MyDBHelper.COLUMN_VIL,mHydrant.getVil());
                    db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });



            editTextAddress.setText(mHydrant.getAddress());

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
                        ContentValues cv = new ContentValues();
                        cv.put(MyDBHelper.COLUMN_ADDRESS,mHydrant.getAddress());
                        db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                    }
                }
            });

            setTrafficLevelUi();


            editTextPs.setText(mHydrant.getPs());
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
                        ContentValues cv = new ContentValues();
                        cv.put(MyDBHelper.COLUMN_PS,mHydrant.getPs());
                        db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                    }else{
                        String none = "NONE";
                        mHydrant.setPs(none);
                        ContentValues cv = new ContentValues();
                        cv.put(MyDBHelper.COLUMN_PS,mHydrant.getPs());
                        db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                    }
                }
            });



            hydrantPosition = mHydrant.getLatlng();

        }

        private void setSpinnerMarkState() {

            final List<String> MarkStatesList = Hydrant.MARK_STATES.getAllStates();
            final ArrayAdapter markedStatesAdapter = new ArrayAdapter<String>(this.getActivity(),R.layout.simple_list_item_1,MarkStatesList);
            final AdapterView.OnItemSelectedListener markedStatesSelectedListener = new AdapterView.OnItemSelectedListener() {
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
                                    ContentValues cv = new ContentValues();
                                    cv.put(MyDBHelper.COLUMN_MARKED_STATE,mHydrant.getMark_States());
                                    db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                                }
                            }
                        });
                    }else {
                        editTextMarkedStatesEtc.setVisibility(View.INVISIBLE);
                        mHydrant.setMark_States(MarkStatesList.get(position));
                        ContentValues cv = new ContentValues();
                        cv.put(MyDBHelper.COLUMN_MARKED_STATE,mHydrant.getMark_States());
                        db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    spinnerMakedStates.setSelection(Hydrant.MARK_STATES.getAllStates().indexOf(Hydrant.MARK_STATES.GOOD));
                }
            };

            spinnerHasMarked.setAdapter(new ArrayAdapter<String>(this.getActivity(),R.layout.simple_list_item_1,new String[]{"無設","有設"}));
            spinnerHasMarked.setSelection(mHydrant.hasMark()?1:0);



            spinnerHasMarked.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position ==0){
                        spinnerMakedStates.setSelection(0);
                        editTextMarkedStatesEtc.setVisibility(View.INVISIBLE);
                        spinnerMakedStates.setOnItemSelectedListener(null);
                        spinnerMakedStates.setAdapter(new ArrayAdapter<String>(EditPageViewFragment.this.getActivity(),R.layout.simple_list_item_1,new String[]{"無"}));
                        mHydrant.setMark_States(Hydrant.MARK_STATES.NULL);
                        spinnerMakedStates.setEnabled(false);

                    }else{
                        spinnerMakedStates.setEnabled(true);
                        Cursor cc = db.query(MyDBHelper.TABLE_HYDRANT,new String[]{MyDBHelper.COLUMN_MARKED_STATE},MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null,null,null,null);
                        if(cc.getCount()>0&&cc.moveToFirst()){
                            String currentMarkState = cc.getString(0);
                            mHydrant.setMark_States((currentMarkState.compareTo(Hydrant.MARK_STATES.NULL)==0)?Hydrant.MARK_STATES.GOOD:currentMarkState);
                            spinnerMakedStates.setAdapter(markedStatesAdapter);
                            spinnerMakedStates.setOnItemSelectedListener(markedStatesSelectedListener);
                        }

                    }
                    mHydrant.setMarkExist(position==1);
                    ContentValues cv = new ContentValues();
                    cv.put(MyDBHelper.COLUMN_MARKED,mHydrant.hasMark()?"1":"0");
                    cv.put(MyDBHelper.COLUMN_MARKED_STATE,mHydrant.getMark_States());
                    db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });



            spinnerMakedStates.setAdapter(markedStatesAdapter);
            int markedStatesIndex = Hydrant.MARK_STATES.getAllStates().indexOf(mHydrant.getMark_States());
            if(markedStatesIndex == -1 && mHydrant.getMark_States().compareTo(Hydrant.MARK_STATES.NULL) !=0 ){
                spinnerMakedStates.setSelection(Hydrant.MARK_STATES.getAllStates().indexOf(Hydrant.MARK_STATES.ETC));
                editTextMarkedStatesEtc.setVisibility(View.VISIBLE);
                editTextMarkedStatesEtc.setText(mHydrant.getMark_States());
            }else if(mHydrant.getMark_States().compareTo(Hydrant.MARK_STATES.NULL) ==0){
                spinnerMakedStates.setEnabled(false);
                editTextMarkedStatesEtc.setVisibility(View.INVISIBLE);
                editTextMarkedStatesEtc.setText("");
            }else {
                spinnerMakedStates.setSelection(Hydrant.MARK_STATES.getAllStates().indexOf(mHydrant.getMark_States()));
                editTextMarkedStatesEtc.setVisibility(View.INVISIBLE);
                editTextMarkedStatesEtc.setText("");
            }


            spinnerMakedStates.setOnItemSelectedListener(markedStatesSelectedListener);
        }

        private void setButtonStates() {


            statesStringList = new ArrayList<>();
            for(int code:Hydrant.STATES.getAllStates()){
                if(code != Hydrant.STATES.REMOVED && code!=Hydrant.STATES.GOOD)
                    statesStringList.add(Hydrant.getChineseState(code));
            }



            selectedStatesIndex = new ArrayList<>();
            String states = "";
            ArrayList<Integer> selectedStatesCodes = mHydrant.getStates();

            if(selectedStatesCodes.contains(Hydrant.STATES.GOOD)) {
                states = Hydrant.getChineseState(Hydrant.STATES.GOOD);
            }else {
                for (int code : selectedStatesCodes) {
                    if (code != selectedStatesCodes.get(0)) {
                        states += "," + Hydrant.getChineseState(code);
                    } else
                        states = Hydrant.getChineseState(code);
                    selectedStatesIndex.add(Hydrant.STATES.getAllStates().indexOf(code)-1);
                }
            }
            buttonStates.setText(states);

            buttonStates.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    final AlertDialog.Builder builder =new AlertDialog.Builder(EditPageViewFragment.this.getContext());

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
                            ContentValues cv = new ContentValues();
                            cv.put(MyDBHelper.COLUMN_STATE,mHydrant.getStatesForString());
                            db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);

                            parentActivity.setNewMarkerIcon(mPageIndex,mHydrant.getBitmapDescriptor(getContext()));
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

            spinnerType.setAdapter(new ArrayAdapter<String>(this.getActivity(),R.layout.simple_list_item_1,types));
            spinnerType.setSelection(Hydrant.TYPE.getAllType().indexOf(mHydrant.getType()));
            spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mHydrant.setType(Hydrant.TYPE.getAllType().get(position));
                    ContentValues cv = new ContentValues();
                    cv.put(MyDBHelper.COLUMN_TYPE,mHydrant.getType());
                    db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);

                    parentActivity.setNewMarkerIcon(mPageIndex,mHydrant.getBitmapDescriptor(getContext()));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }


        public void setNewLatLng(){
            hydrantPosition = mMap.getCameraPosition().target;
            parentActivity.setNewMarkerPosition(mPageIndex,hydrantPosition);

        }


        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

        }

        public void slideWindow(boolean isDown){

            final int width = investgationInclude.getWidth();
            parentActivity.onWindowSliding = true;

            final ConstraintLayout.LayoutParams originLayoutParams = new ConstraintLayout.LayoutParams((ConstraintLayout.LayoutParams) parentViewPager.getLayoutParams());

            handler = new android.os.Handler(){
                @Override
                public void handleMessage(Message msg){
                    int currentHeight = msg.arg1;
                    investgationInclude.setLayoutParams(new ConstraintLayout.LayoutParams(width,currentHeight));
                    if(msg.arg2 ==AnimateToChangeHeight.ARG_COMPLETE){
                        parentActivity.onWindowSliding = false;
                        parentActivity.isFullScreen = !parentActivity.isFullScreen;
                        if(parentActivity.isFullScreen)
                            buttonWindowControl.setImageResource(android.R.drawable.arrow_up_float);
                        else
                            buttonWindowControl.setImageResource(android.R.drawable.arrow_down_float);
                    }
                }
            };


            if(isDown) {
                Timer timer = new Timer();
                AnimateToChangeHeight animator;
                animator = new AnimateToChangeHeight(parentActivity.fullScreenHeight, investgationInclude.getHeight(), width, timer);
                timer.schedule(animator, 0, 10);
            }else {
                Timer timer = new Timer();
                AnimateToChangeHeight animator;
                animator = new AnimateToChangeHeight(parentActivity.originalHeight,investgationInclude.getHeight(),width,timer);
                timer.schedule(animator, 0, 10);
            }


        }

        private void setTrafficLevelUi(){


            trafficText.setText(Hydrant.TRAFFIC.LevelName().get(mHydrant.getTrafficLevel())+" 可通行");
            trafficSeekBar.setMax(Hydrant.TRAFFIC.LevelName().size()-1);
            trafficSeekBar.setMin(1);
            trafficSeekBar.setProgress(mHydrant.getTrafficLevel());

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

                    ContentValues cv = new ContentValues();
                    cv.put(MyDBHelper.COLUMN_TRAFFIC_LEVEL,mHydrant.getTrafficLevel());
                    db.update(MyDBHelper.TABLE_TEMP,cv,MyDBHelper.COLUMN_ID+" = "+mHydrant.getId(),null);
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
                    Animation fadeIn = AnimationUtils.loadAnimation(getContext(),R.anim.alpha);
                    fadeIn.setDuration(200);
                    seekBarImage.startAnimation(fadeIn);
                    seekBarText.startAnimation(fadeIn);
                    seekBarImage.setVisibility(View.VISIBLE);
                    seekBarText.setVisibility(View.VISIBLE);
                }

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                    Animation fadeOut = AnimationUtils.loadAnimation(getContext(),R.anim.alpha_reverse);
                    fadeOut.setDuration(200);
                    seekBarImage.startAnimation(fadeOut);
                    seekBarText.startAnimation(fadeOut);
                    seekBarImage.setVisibility(View.INVISIBLE);
                    seekBarText.setVisibility(View.INVISIBLE);
                }
            });


        }

        private class AnimateToChangeHeight extends TimerTask{

            final int targetHeight;
            final int width;
            Double currentHeight;
            Double periodHeight;
            Double a;
            int times;
            Timer timer;
            public static final int totalTimes = 30;
            boolean isReturn;
            public static final int ARG_COMPLETE = 1001;

            public AnimateToChangeHeight(int targetHeight , int currentHeight,int currentWidth ,Timer timer){
                super();
                this.targetHeight = targetHeight;
                this.currentHeight = Double.valueOf(String.valueOf(currentHeight));
                width = currentWidth;
                times = 1;
                a=0.0;
                periodHeight = ((targetHeight - currentHeight)*0.85)/(totalTimes/2);

                this.timer = timer;

                isReturn = (targetHeight-currentHeight)<0;
            }

            @Override
            public void run() {

                if(times==(totalTimes/2)+1){
                    double x = targetHeight-currentHeight;
                    a = (2*(x-periodHeight*(totalTimes/2)))/Math.pow(totalTimes/2,2);
                }


                if(times >=totalTimes && isReturn){
                    Message msg = new Message();
                    msg.arg1 = targetHeight;
                    msg.arg2 = ARG_COMPLETE;
                    handler.sendMessage(msg);
                    timer.cancel();
                    return;
                }else if(times >= totalTimes){
                    Message msg = new Message();
                    msg.arg1 = targetHeight;
                    msg.arg2 = ARG_COMPLETE;
                    handler.sendMessage(msg);
                    timer.cancel();
                    return;

                }else{
                    periodHeight+=a;
                    currentHeight += periodHeight;
                    Message msg = new Message();
                    msg.arg1 = currentHeight.intValue();
                    handler.sendMessage(msg);
                }
                times++;
            }
        }
    }



}
