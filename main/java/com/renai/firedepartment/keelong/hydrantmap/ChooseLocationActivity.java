package com.renai.firedepartment.keelong.hydrantmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class ChooseLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    public final static String EXTRA_LATITUDE = "latitude";
    public final static String EXTRA_LONGITUDE = "longitude";
    public final static String STATE_FIND_LOCATION_SUSSES = "find_location_susses";
    public final static String STATE_ALREADY_HAS_LOCATION = "already_has_location";

    public final static String EXTRA_HYDRANT_TYPE = "hydrant_type";
    public final static String EXTRA_HYDRANT_STATE = "hydrant_state";

    private FusedLocationProviderClient mFusedLocationClient;

    ImageView btnConcern;
    ImageView btnCancerl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnCancerl = (ImageView) findViewById(R.id.cancel_button);
        btnConcern = (ImageView) findViewById(R.id.concern_button);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if(getIntent().getBooleanExtra(STATE_FIND_LOCATION_SUSSES,false)){
            LatLng currentPosition = new LatLng(getIntent().getDoubleExtra(EXTRA_LATITUDE,0),
                                                    getIntent().getDoubleExtra(EXTRA_LONGITUDE,0));
            mMap.addMarker(new MarkerOptions().position(currentPosition));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,17));
        }else{
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(location.getLatitude(), location.getLongitude()), 17, 0, 0)));
                }
            });
        }

        btnCancerl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                setResult(RESULT_CANCELED, intent);
                ChooseLocationActivity.this.finish();
            }
        });

        btnConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng selectedPosition = mMap.getCameraPosition().target;
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putDouble(EXTRA_LATITUDE,selectedPosition.latitude);
                bundle.putDouble(EXTRA_LONGITUDE,selectedPosition.longitude);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent); //requestCode需跟A.class的一樣
                ChooseLocationActivity.this.finish();
            }
        });
    }
}
