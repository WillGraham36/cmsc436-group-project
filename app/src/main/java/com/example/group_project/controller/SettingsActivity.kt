package com.example.group_project.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_project.R
import com.example.group_project.model.Review
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class SettingsActivity : BaseBottomNavActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var emptyStateTextView: TextView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var reviewsQuery: Query
    private var reviewsListener: ValueEventListener? = null
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        googleSignInClient = buildGoogleSignInClient()
        usernameTextView = findViewById(R.id.usernameTextView)
        emptyStateTextView = findViewById(R.id.myReviewsEmptyTextView)
        val darkModeSwitch: MaterialSwitch = findViewById(R.id.darkModeSwitch)
        val logoutButton: MaterialButton = findViewById(R.id.logoutButton)
        val reviewsRecyclerView: RecyclerView = findViewById(R.id.myReviewsRecyclerView)
        val adContainer: FrameLayout = findViewById(R.id.settingsAdContainer)

        // Reuse the same review card layout used elsewhere
        // requirement: 2 instances of views not reviewed in class - recycler view for My Reviews
        reviewAdapter = ReviewAdapter(ArrayList())
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsRecyclerView.adapter = reviewAdapter

        // Only pull reviews written by the logged in account
        // requirement: meaningful remote data (getting firebase data) - Settings loads reviews by author id
        reviewsQuery = FirebaseDatabase.getInstance()
            .getReference("reviews")
            .orderByChild("authorId")
            .equalTo(auth.currentUser?.uid)

        updateUsername()
        setupDarkModeSwitch(darkModeSwitch)
        logoutButton.setOnClickListener { logout() }
        loadBannerAd(adContainer)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        setupBottomNavigation(bottomNavigationView, R.id.navigation_settings)
    }

    override fun onStart() {
        super.onStart()
        attachMyReviewsListener()
    }

    override fun onStop() {
        super.onStop()
        detachMyReviewsListener()
    }

    override fun onDestroy() {
        adView?.destroy()
        adView = null
        super.onDestroy()
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

    private fun setupDarkModeSwitch(darkModeSwitch: MaterialSwitch) {
        // Keep the switch matched to the saved app setting
        // requirement: meaningful local persistent data (2/2) - dark mode choice is stored in SharedPreferences
        val preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        darkModeSwitch.isChecked = preferences.getBoolean(PREFERENCE_DARK_MODE, false)
        // requirement: where the listeners are on one of these new gui elements - dark mode switch listener
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(PREFERENCE_DARK_MODE, isChecked).apply()
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun loadBannerAd(adContainer: FrameLayout) {
        // Initialize ads once before loading the banner
        // requirement: advertising - banner ad loads on the Settings page
        Thread {
            MobileAds.initialize(this) {}
        }.start()

        adContainer.post {
            val bannerAdView = AdView(this)
            bannerAdView.adUnitId = BANNER_AD_UNIT_ID
            bannerAdView.setAdSize(AdSize.BANNER)
            adView = bannerAdView
            adContainer.removeAllViews()
            adContainer.addView(bannerAdView)
            bannerAdView.loadAd(AdRequest.Builder().build())
        }
    }

    private fun attachMyReviewsListener() {
        if (auth.currentUser == null) {
            reviewAdapter.setReviews(ArrayList())
            showEmptyState(R.string.my_reviews_empty)
            return
        }

        if (reviewsListener != null) {
            return
        }

        reviewsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Firebase gives children in query order, then we show newest first
                val reviews = ArrayList<Review>()
                for (child in snapshot.children) {
                    child.getValue(Review::class.java)?.let(reviews::add)
                }
                reviews.sortByDescending { it.timestamp }
                reviewAdapter.setReviews(reviews)
                emptyStateTextView.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
                if (reviews.isEmpty()) {
                    emptyStateTextView.setText(R.string.my_reviews_empty)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                reviewAdapter.setReviews(ArrayList())
                showEmptyState(R.string.my_reviews_error)
            }
        }
        reviewsQuery.addValueEventListener(reviewsListener!!)
    }

    private fun detachMyReviewsListener() {
        reviewsListener?.let {
            reviewsQuery.removeEventListener(it)
            reviewsListener = null
        }
    }

    private fun showEmptyState(messageResId: Int) {
        emptyStateTextView.setText(messageResId)
        emptyStateTextView.visibility = View.VISIBLE
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, options)
    }

    private fun logout() {
        detachMyReviewsListener()
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()
            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "settings"
        private const val PREFERENCE_DARK_MODE = "dark_mode"
        private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    }
}
