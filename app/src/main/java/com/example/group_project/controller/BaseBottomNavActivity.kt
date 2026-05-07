package com.example.group_project.controller

import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.group_project.R
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseBottomNavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before the activity draws
        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val mode = if (preferences.getBoolean(PREFERENCE_DARK_MODE, false)) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
        super.onCreate(savedInstanceState)
    }

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

    companion object {
        private const val PREFERENCES_NAME = "settings"
        private const val PREFERENCE_DARK_MODE = "dark_mode"
    }
}
