package com.example.group_project.controller

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.group_project.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseBottomNavActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = buildGoogleSignInClient()
        usernameTextView = findViewById(R.id.usernameTextView)
        val logoutButton: MaterialButton = findViewById(R.id.logoutButton)

        updateUsername()
        logoutButton.setOnClickListener { logout() }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        setupBottomNavigation(bottomNavigationView, R.id.navigation_settings)
    }

    private fun updateUsername() {
        val user = auth.currentUser
        if (user == null) {
            usernameTextView.setText(R.string.anonymous_user)
            return
        }

        var displayName = user.displayName
        if (displayName.isNullOrBlank()) {
            displayName = user.email
        }

        usernameTextView.text = displayName ?: getString(R.string.anonymous_user)
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, options)
    }

    private fun logout() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}
