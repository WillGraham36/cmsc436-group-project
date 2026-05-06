package com.example.group_project.controller

import android.content.Intent
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.example.group_project.R
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseBottomNavActivity : AppCompatActivity() {

    protected fun setupBottomNavigation(
        bottomNavigationView: BottomNavigationView,
        @IdRes selectedItemId: Int
    ) {
        bottomNavigationView.selectedItemId = selectedItemId
        bottomNavigationView.setOnItemSelectedListener { item ->
            val itemId = item.itemId
            if (itemId == selectedItemId) {
                return@setOnItemSelectedListener true
            }

            if (itemId == R.id.navigation_home) {
                navigateTo(HomeActivity::class.java)
                return@setOnItemSelectedListener true
            }

            if (itemId == R.id.navigation_recent_reviews) {
                navigateTo(RecentReviewsActivity::class.java)
                return@setOnItemSelectedListener true
            }

            if (itemId == R.id.navigation_settings) {
                navigateTo(SettingsActivity::class.java)
                return@setOnItemSelectedListener true
            }

            false
        }
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
        finish()
    }
}
