package com.example.group_project.controller;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_project.R;
import com.example.group_project.model.Review;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class RecentReviewsActivity extends BaseBottomNavActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_reviews);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation(bottomNavigationView, R.id.navigation_recent_reviews);

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data list
        reviewList = new ArrayList<>();

        // Set up the Adapter
        reviewAdapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(reviewAdapter);

        // Load dummy data to test the UI before Firebase is ready
        loadDummyData();
    }

    private void loadDummyData() {
        Review dummy1 = new Review();
        dummy1.setSpotName("McKeldin Library");
        dummy1.setStarRating(4.5f);
        dummy1.setDescription("Quiet in the upper floors, plenty of outlets, and usually easy to find a seat in the afternoon.");

        Review dummy2 = new Review();
        dummy2.setSpotName("ESJ Study Lounge");
        dummy2.setStarRating(3.0f);
        dummy2.setDescription("Bright open seating with moderate foot traffic and solid group study energy.");

        reviewList.add(dummy1);
        reviewList.add(dummy2);

        // Notify the adapter that the underlying dataset has changed
        reviewAdapter.notifyDataSetChanged();
    }
}