package com.example.group_project.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group_project.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends BaseBottomNavActivity {
    private TextView usernameTextView;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        googleSignInClient = buildGoogleSignInClient();
        usernameTextView = findViewById(R.id.usernameTextView);
        MaterialButton logoutButton = findViewById(R.id.logoutButton);

        updateUsername();
        logoutButton.setOnClickListener(view -> logout());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation(bottomNavigationView, R.id.navigation_settings);
    }

    private void updateUsername() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            usernameTextView.setText(R.string.anonymous_user);
            return;
        }

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getEmail();
        }

        usernameTextView.setText(displayName == null ? getString(R.string.anonymous_user) : displayName);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        return GoogleSignIn.getClient(this, options);
    }

    private void logout() {
        auth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
