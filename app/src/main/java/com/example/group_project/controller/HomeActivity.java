package com.example.group_project.controller;

import android.content.Intent;
import android.os.Bundle;

import com.example.group_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class HomeActivity extends BaseBottomNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialButton createReviewButton = findViewById(R.id.createReviewButton);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        createReviewButton.setOnClickListener(view ->
                startActivity(new Intent(HomeActivity.this, CreateReviewActivity.class)));

        setupBottomNavigation(bottomNavigationView, R.id.navigation_home);
    }
}
