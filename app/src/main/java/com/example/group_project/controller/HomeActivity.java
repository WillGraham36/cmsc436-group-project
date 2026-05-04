package com.example.group_project.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.example.group_project.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends BaseBottomNavActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialButton createReviewButton =
                findViewById(R.id.createReviewButton);

        BottomNavigationView bottomNavigationView =
                findViewById(R.id.bottomNavigation);

        createReviewButton.setOnClickListener(view ->
                startActivity(new Intent(
                        HomeActivity.this,
                        CreateReviewActivity.class)));

        setupBottomNavigation(
                bottomNavigationView,
                R.id.navigation_home
        );

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        LatLng umd = new LatLng(38.9869, -76.9426);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(umd, 15));

        addStudySpotMarkers();

        enableUserLocation();
    }

    private void addStudySpotMarkers() {

        LatLng mckeldin =
                new LatLng(38.985970, -76.945088);

        LatLng iribe =
                new LatLng(38.989078, -76.936685);

        LatLng esj =
                new LatLng(38.986179, -76.942777);

        mMap.addMarker(new MarkerOptions()
                .position(mckeldin)
                .title("McKeldin Library")
                .snippet("Quiet individual study"));

        mMap.addMarker(new MarkerOptions()
                .position(iribe)
                .title("Iribe Center")
                .snippet("Great for CS students"));

        mMap.addMarker(new MarkerOptions()
                .position(esj)
                .title("ESJ")
                .snippet("Good group study space"));
    }

    private void enableUserLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );

            return;
        }

        mMap.setMyLocationEnabled(true);
    }
}
