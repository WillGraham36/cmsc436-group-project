package com.example.group_project.controller;

import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseBottomNavActivity extends AppCompatActivity {

    protected void setupBottomNavigation(BottomNavigationView bottomNavigationView,
                                         @IdRes int selectedItemId) {
        bottomNavigationView.setSelectedItemId(selectedItemId);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedItemId) {
                return true;
            }

            if (itemId == R.id.navigation_home) {
                navigateTo(HomeActivity.class);
                return true;
            }

            if (itemId == R.id.navigation_recent_reviews) {
                navigateTo(RecentReviewsActivity.class);
                return true;
            }

            if (itemId == R.id.navigation_settings) {
                navigateTo(SettingsActivity.class);
                return true;
            }

            return false;
        });
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        startActivity(intent);
        finish();
    }
}
