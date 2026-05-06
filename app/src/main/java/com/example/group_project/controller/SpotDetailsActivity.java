package com.example.group_project.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_project.R;
import com.example.group_project.model.Review;
import com.example.group_project.model.StudySpot;
import com.example.group_project.util.SpotUiFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SpotDetailsActivity extends BaseBottomNavActivity {

    public static final String EXTRA_SPOT_ID = "extra_spot_id";

    private TextView spotNameTextView;
    private TextView buildingTextView;
    private TextView summaryTextView;
    private TextView tagsTextView;
    private TextView coordinatesTextView;
    private TextView emptyStateTextView;
    private ReviewAdapter reviewAdapter;

    private DatabaseReference spotsReference;
    private Query reviewsQuery;
    private ValueEventListener spotListener;
    private ValueEventListener reviewsListener;
    private String spotId;
    private StudySpot currentSpot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_details);

        spotId = getIntent().getStringExtra(EXTRA_SPOT_ID);
        if (TextUtils.isEmpty(spotId)) {
            Toast.makeText(this, R.string.spot_details_missing_spot, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton addReviewButton = findViewById(R.id.addReviewButton);
        RecyclerView reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);

        spotNameTextView = findViewById(R.id.spotNameTextView);
        buildingTextView = findViewById(R.id.buildingTextView);
        summaryTextView = findViewById(R.id.summaryTextView);
        tagsTextView = findViewById(R.id.tagsTextView);
        coordinatesTextView = findViewById(R.id.coordinatesTextView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        spotsReference = FirebaseDatabase.getInstance().getReference("pins");
        reviewsQuery = FirebaseDatabase.getInstance()
                .getReference("reviews")
                .orderByChild("spotId")
                .equalTo(spotId);

        backButton.setOnClickListener(view -> finish());
        addReviewButton.setOnClickListener(view -> launchCreateReview());
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachListeners();
    }

    private void attachListeners() {
        if (spotListener == null) {
            spotListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentSpot = snapshot.getValue(StudySpot.class);
                    if (currentSpot == null) {
                        Toast.makeText(
                                SpotDetailsActivity.this,
                                R.string.spot_details_missing_spot,
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                        return;
                    }
                    bindSpot(currentSpot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(
                            SpotDetailsActivity.this,
                            R.string.spot_details_load_error,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };
            spotsReference.child(spotId).addValueEventListener(spotListener);
        }

        if (reviewsListener == null) {
            reviewsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Review> reviews = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Review review = child.getValue(Review.class);
                        if (review != null) {
                            reviews.add(review);
                        }
                    }

                    reviews.sort(Comparator.comparingLong(Review::getTimestamp).reversed());
                    reviewAdapter.setReviews(reviews);
                    emptyStateTextView.setVisibility(reviews.isEmpty()
                            ? android.view.View.VISIBLE
                            : android.view.View.GONE);
                    if (reviews.isEmpty()) {
                        emptyStateTextView.setText(R.string.spot_details_no_reviews);
                    }

                    updateSummary(reviews);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(
                            SpotDetailsActivity.this,
                            R.string.spot_details_load_error,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            };
            reviewsQuery.addValueEventListener(reviewsListener);
        }
    }

    private void detachListeners() {
        if (spotListener != null) {
            spotsReference.child(spotId).removeEventListener(spotListener);
            spotListener = null;
        }
        if (reviewsListener != null) {
            reviewsQuery.removeEventListener(reviewsListener);
            reviewsListener = null;
        }
    }

    private void bindSpot(StudySpot spot) {
        spotNameTextView.setText(spot.getSpotName());
        buildingTextView.setText(buildBuildingLine(spot));
        coordinatesTextView.setText(String.format(
                Locale.US,
                "%.5f, %.5f",
                spot.getLatitude(),
                spot.getLongitude()
        ));
    }

    private void updateSummary(List<Review> reviews) {
        int reviewCount = reviews.size();
        double averageRating = 0.0;
        for (Review review : reviews) {
            averageRating += review.getStarRating();
        }
        if (reviewCount > 0) {
            averageRating /= reviewCount;
        }

        summaryTextView.setText(SpotUiFormatter.formatRatingSummary(averageRating, reviewCount));
        tagsTextView.setText(SpotUiFormatter.formatTraitList(SpotUiFormatter.uniqueTraits(reviews)));
    }

    private String buildBuildingLine(StudySpot spot) {
        if (TextUtils.isEmpty(spot.getRoomNumber())) {
            return spot.getBuildingName();
        }
        return spot.getBuildingName() + " | " + spot.getRoomNumber();
    }

    private void launchCreateReview() {
        if (currentSpot == null) {
            Toast.makeText(this, R.string.spot_details_missing_spot, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CreateReviewActivity.class);
        intent.putExtra(CreateReviewActivity.EXTRA_SPOT_ID, currentSpot.getSpotId());
        intent.putExtra(CreateReviewActivity.EXTRA_SPOT_NAME, currentSpot.getSpotName());
        intent.putExtra(CreateReviewActivity.EXTRA_BUILDING_NAME, currentSpot.getBuildingName());
        intent.putExtra(CreateReviewActivity.EXTRA_ROOM_NUMBER, currentSpot.getRoomNumber());
        intent.putExtra(CreateReviewActivity.EXTRA_LATITUDE, currentSpot.getLatitude());
        intent.putExtra(CreateReviewActivity.EXTRA_LONGITUDE, currentSpot.getLongitude());
        startActivity(intent);
    }
}
