package com.example.group_project.controller;

import android.os.Bundle;

import com.example.group_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends BaseBottomNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation(bottomNavigationView, R.id.navigation_settings);
    }
}
