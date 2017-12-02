package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.style.LocaleSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SQLiteDatabase db;


    public static final String STRINGARRAY_ARG_RANGES = "ranges";
    ArrayList<String> ranges;
    String whereString;
    public static final String BOOLEAN_ARG_ISINVESTGATION = "investigation";
    public static final String WHERE_STRING = "whereString";
    boolean isInvestgation;

    public static final String ARG_TARGET_HYDRANT_ID = "hydrantId";
    int targetId;
    int targetIndex = -1;
    ArrayList<Hydrant> hydrants;
    ArrayList<Marker> mMarker;
    boolean isSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        db = new MyDBHelper(this).getReadableDatabase();

        ranges = getIntent().getStringArrayListExtra(STRINGARRAY_ARG_RANGES);
        hydrants = new ArrayList<>();
        isInvestgation = getIntent().getBooleanExtra(BOOLEAN_ARG_ISINVESTGATION,false);
        isSearch = getIntent().getBooleanExtra(InvestgationResultActivity.BOOLEAN_IS_SEARCH,false);

        targetId = getIntent().getIntExtra(ARG_TARGET_HYDRANT_ID,-1);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       /* // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
            dialog.setTitle("Error");
            dialog.setMessage("沒有權限");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            dialog.show();
        }*/
        mMarker = new ArrayList<>();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        ArrayList<Double> latList = new ArrayList<>();
        ArrayList<Double> lngList = new ArrayList<>();


        if(isInvestgation){

            Cursor cs = db.query(MyDBHelper.TABLE_TEMP,new String[]{"*"},null,null,null,null,null);
            if (cs.getCount()>0&&cs.moveToFirst()) {
                do {
                    Hydrant hydrant = new Hydrant(cs);
                    hydrants.add(hydrant);
                    latList.add(hydrant.getLatlng().latitude);
                    lngList.add(hydrant.getLatlng().longitude);

                    MarkerOptions markerOptions = new MarkerOptions();

                    markerOptions.position(hydrant.getLatlng()).draggable(false).visible(true).icon(hydrant.getBitmapDescriptor(this));
                    Marker m = mMap.addMarker(markerOptions);
                    m.setTag(hydrant);
                    m.setTitle(((hydrant.getType() == Hydrant.TYPE.UNDER) ? "地下式" :
                            ((hydrant.getType() == Hydrant.TYPE.GROUND) ? "地上式" :
                                    "地下式(新式開關)"))
                            + "消防栓 (編號:" + hydrant.getId() + ")");
                    mMarker.add(m);
                    if (hydrant.getId() == targetId)
                        targetIndex = hydrants.size() - 1;

                } while (cs.moveToNext());
            }
        }else if (!isSearch && ranges != null && ranges.size() > 0) {

            if (ranges.get(0).compareTo("all") == 0) {
                Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT, new String[]{"*"}, null, null, null, null, null);
                if (cs.moveToFirst()) {
                    do {
                        Hydrant hydrant = new Hydrant(cs);
                        hydrants.add(hydrant);
                        latList.add(hydrant.getLatlng().latitude);
                        lngList.add(hydrant.getLatlng().longitude);

                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(hydrant.getLatlng()).draggable(false).visible(true).icon(hydrant.getBitmapDescriptor(this));
                        Marker m = mMap.addMarker(markerOptions);
                        m.setTag(hydrant);
                        m.setTitle(((hydrant.getType() == Hydrant.TYPE.UNDER) ? "地下式" :
                                ((hydrant.getType() == Hydrant.TYPE.GROUND) ? "地上式" :
                                        "地下式(新式開關)"))
                                + "消防栓 (編號:" + hydrant.getId() + ")");
                        mMarker.add(m);
                        if (hydrant.getId() == targetId)
                            targetIndex = hydrants.size() - 1;

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
                                hydrants.add(hydrant);
                                latList.add(hydrant.getLatlng().latitude);
                                lngList.add(hydrant.getLatlng().longitude);

                                MarkerOptions markerOptions = new MarkerOptions();

                                markerOptions.position(hydrant.getLatlng()).draggable(false).visible(true).icon(hydrant.getBitmapDescriptor(this));
                                Marker m = mMap.addMarker(markerOptions);
                                m.setTag(hydrant);
                                m.setTitle(((hydrant.getType() == Hydrant.TYPE.UNDER) ? "地下式" :
                                        ((hydrant.getType() == Hydrant.TYPE.GROUND) ? "地上式" :
                                                "地下式(新式開關)"))
                                        + "消防栓 (編號:" + hydrant.getId() + ")");
                                mMarker.add(m);
                                if (hydrant.getId() == targetId)
                                    targetIndex = hydrants.size() - 1;

                            } while (cs.moveToNext());
                        }
                    }

                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("發生錯誤").setMessage("訊息:" + e.getMessage()).setPositiveButton("確定", null).show();
                    return;
                }
            }

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)), 500, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            mMap.animateCamera(CameraUpdateFactory.scrollBy(0, -280), 300, null);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    marker.showInfoWindow();

                    return false;
                }
            });


        }else if(isSearch){

            whereString = getIntent().getStringExtra(WHERE_STRING);

                Cursor cs = db.query(MyDBHelper.TABLE_HYDRANT, new String[]{"*"}, whereString, null, null, null, null);
                if (cs.getCount()>0&&cs.moveToFirst()) {
                    do {
                        Hydrant hydrant = new Hydrant(cs);
                        hydrants.add(hydrant);
                        latList.add(hydrant.getLatlng().latitude);
                        lngList.add(hydrant.getLatlng().longitude);

                        MarkerOptions markerOptions = new MarkerOptions();

                        markerOptions.position(hydrant.getLatlng()).draggable(false).visible(true).icon(hydrant.getBitmapDescriptor(this));
                        Marker m = mMap.addMarker(markerOptions);
                        m.setTag(hydrant);
                        m.setTitle(((hydrant.getType() == Hydrant.TYPE.UNDER) ? "地下式" :
                                ((hydrant.getType() == Hydrant.TYPE.GROUND) ? "地上式" :
                                        "地下式(新式開關)"))
                                + "消防栓 (編號:" + hydrant.getId() + ")");
                        mMarker.add(m);
                        if (hydrant.getId() == targetId)
                            targetIndex = hydrants.size() - 1;

                    } while (cs.moveToNext());
                }


            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(final Marker marker) {

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)), 500, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            mMap.animateCamera(CameraUpdateFactory.scrollBy(0, -280), 300, null);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    marker.showInfoWindow();

                    return false;
                }
            });
        } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("查無結果!").setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
        }

        mMap.setInfoWindowAdapter(new MainActivity.HydrantInfoWindowAdapter(this));

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(Collections.min(latList), Collections.min(lngList)), new LatLng(Collections.max(latList), Collections.max(lngList)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, dm.widthPixels, dm.heightPixels, 100));

        setTitle("查詢結果 (共 " + hydrants.size() + " 筆)");

       if(targetIndex != -1){
           mMap.moveCamera(CameraUpdateFactory.newLatLng(mMarker.get(targetIndex).getPosition()));
           mMarker.get(targetIndex).showInfoWindow();

       }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.result_map_manu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_list) {

            Intent intent = new Intent();
            intent.putExtra(InvestgationResultActivity.BOOLEAN_ARG_ISINVESTGATION, isInvestgation);
            if (isSearch)
                intent.putExtra(InvestgationResultActivity.WHERE_STRING,whereString);
            else
                intent.putExtra(InvestgationResultActivity.STRINGARRAY_ARG_RANGES, ranges);

            intent.putExtra(InvestgationResultActivity.BOOLEAN_IS_SEARCH,isSearch);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setClass(this,InvestgationResultActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

}
