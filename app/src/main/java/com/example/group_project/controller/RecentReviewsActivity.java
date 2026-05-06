package com.example.group_project.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_project.R;
import com.example.group_project.model.Review;
import com.example.group_project.util.RecentViewedSpotStore;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecentReviewsActivity extends BaseBottomNavActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter reviewAdapter;
    private TextView emptyStateTextView;
    private DatabaseReference reviewsReference;
    private final List<Query> reviewsQueries = new ArrayList<>();
    private final List<ValueEventListener> reviewsListeners = new ArrayList<>();
    private final Map<String, List<Review>> reviewsBySpotId = new HashMap<>();
    private final List<String> recentSpotIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_reviews);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation(bottomNavigationView, R.id.navigation_recent_reviews);

        recyclerView = findViewById(R.id.recyclerViewReviews);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        recyclerView.setAdapter(reviewAdapter);

        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews");
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachReviewsListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachReviewsListener();
    }

    private void attachReviewsListener() {
        detachReviewsListener();
        recentSpotIds.clear();
        recentSpotIds.addAll(RecentViewedSpotStore.getRecentSpotIds(this));

        if (recentSpotIds.isEmpty()) {
            reviewAdapter.setReviews(new ArrayList<>());
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText(R.string.recent_reviews_empty);
            return;
        }

        emptyStateTextView.setVisibility(View.GONE);

        for (String spotId : recentSpotIds) {
            Query reviewsQuery = reviewsReference.orderByChild("spotId").equalTo(spotId);
            ValueEventListener reviewsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Review> loadedReviews = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Review review = child.getValue(Review.class);
                        if (review != null) {
                            loadedReviews.add(review);
                        }
                    }

                    loadedReviews.sort(Comparator.comparingLong(Review::getTimestamp).reversed());
                    reviewsBySpotId.put(spotId, loadedReviews);
                    renderRecentReviews();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    emptyStateTextView.setText(R.string.recent_reviews_error);
                }
            };

            reviewsQueries.add(reviewsQuery);
            reviewsListeners.add(reviewsListener);
            reviewsQuery.addValueEventListener(reviewsListener);
        }
    }

    private void detachReviewsListener() {
        for (int i = 0; i < reviewsQueries.size(); i++) {
            reviewsQueries.get(i).removeEventListener(reviewsListeners.get(i));
        }
        reviewsQueries.clear();
        reviewsListeners.clear();
        reviewsBySpotId.clear();
    }

    private void renderRecentReviews() {
        List<Review> orderedReviews = new ArrayList<>();
        for (String spotId : recentSpotIds) {
            List<Review> spotReviews = reviewsBySpotId.get(spotId);
            if (spotReviews != null) {
                orderedReviews.addAll(spotReviews);
            }
        }

        reviewAdapter.setReviews(orderedReviews);
        emptyStateTextView.setVisibility(orderedReviews.isEmpty() ? View.VISIBLE : View.GONE);
        if (orderedReviews.isEmpty()) {
            emptyStateTextView.setText(R.string.recent_reviews_empty);
        }
    }
}
