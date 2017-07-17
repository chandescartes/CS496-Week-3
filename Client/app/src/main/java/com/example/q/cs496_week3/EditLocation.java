package com.example.q.cs496_week3;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by q on 2017-07-14.
 */

public class EditLocation extends AppCompatActivity implements OnMapReadyCallback {

    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    Marker selected_location;
    MarkerOptions markerOptions = new MarkerOptions();
    Location myLocation;
    double myLat;
    double myLng;
    String nickname;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            myLat = location.getLatitude();
            myLng = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_location);
        nickname = getIntent().getStringExtra("nickname");

        if (checkLocationPermission()) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
            myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            myLat = myLocation.getLatitude();
            myLng = myLocation.getLongitude();
        }

        final OnMapReadyCallback context = this;
        Button currBtn = (Button) findViewById(R.id.curr_location);
        currBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View mview) {
                selected_location.remove();
                FragmentManager fragmentManager = getFragmentManager();
                MapFragment mapFragment = (MapFragment) fragmentManager
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(context);
            }
        });

        Button submitBtn = (Button) findViewById(R.id.submit_location);
        submitBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View mview) {
                double lat = selected_location.getPosition().latitude;
                double lng = selected_location.getPosition().longitude;

                HttpCall.setMethodtext("userPUT");
                HttpCall.setUrltext("/api/user/"+UserInfo.getIdStr()+"/location");
                HttpCall.setLatvalue(lat);
                HttpCall.setLngvalue(lng);
                HttpCall.getResponse();

                UserInfo.setLatv(lat);
                UserInfo.setLngv(lng);

                Intent intent = new Intent(EditLocation.this, MainActivity.class);
                startActivity(intent);
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        LatLng default_latlng = new LatLng(myLat, myLng);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(default_latlng, 15);

        markerOptions.position(default_latlng);
        markerOptions.title("선택된 장소");

        selected_location = map.addMarker(markerOptions);
        selected_location.setDraggable(true);
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d("Drag_STARTED", "--DRAGGING--");
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d("Drag_ENDED", String.valueOf(marker.getPosition().latitude));
            }
        });
        map.animateCamera(cameraUpdate);
    }

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission. ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission. ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(EditLocation.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission. ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}
