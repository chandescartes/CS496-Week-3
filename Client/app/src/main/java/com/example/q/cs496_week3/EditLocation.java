package com.example.q.cs496_week3;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_location);
        nickname = getIntent().getStringExtra("nickname");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        myLat = myLocation.getLatitude();
        myLng = myLocation.getLongitude();

        final OnMapReadyCallback context = this;
        Button currBtn = (Button) findViewById(R.id.curr_location);
        currBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View mview) {
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
                Intent intent = new Intent(EditLocation.this, MainActivity.class);
                intent.putExtra("nickname", nickname);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
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
}
