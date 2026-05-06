package com.example.group_project.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.group_project.R;
import com.example.group_project.model.Review;
import com.example.group_project.model.StudySpot;
import com.example.group_project.util.RecentViewedSpotStore;
import com.example.group_project.util.SpotUiFormatter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends BaseBottomNavActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference spotsReference;
    private DatabaseReference reviewsReference;
    private ValueEventListener spotsListener;
    private ValueEventListener reviewsListener;
    private final List<StudySpot> studySpots = new ArrayList<>();
    private final List<Review> reviews = new ArrayList<>();
    private final Map<String, MarkerPayload> markerPayloads = new HashMap<>();
    private LatLng draftLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation(bottomNavigationView, R.id.navigation_home);

        spotsReference = FirebaseDatabase.getInstance().getReference("pins");
        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachDatabaseListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachDatabaseListeners();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng umd = new LatLng(38.9869, -76.9426);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(umd, 15));
        mMap.setInfoWindowAdapter(new StudySpotInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this::handleInfoWindowClick);
        mMap.setOnMapLongClickListener(this::handleMapLongPress);
        renderMarkers();
        enableUserLocation();
    }

    private void attachDatabaseListeners() {
        if (spotsListener == null) {
            spotsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    studySpots.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        StudySpot spot = child.getValue(StudySpot.class);
                        if (spot != null) {
                            studySpots.add(spot);
                        }
                    }
                    renderMarkers();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            spotsReference.addValueEventListener(spotsListener);
        }

        if (reviewsListener == null) {
            reviewsListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    reviews.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Review review = child.getValue(Review.class);
                        if (review != null) {
                            reviews.add(review);
                        }
                    }
                    renderMarkers();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            reviewsReference.addValueEventListener(reviewsListener);
        }
    }

    private void detachDatabaseListeners() {
        if (spotsListener != null) {
            spotsReference.removeEventListener(spotsListener);
            spotsListener = null;
        }
        if (reviewsListener != null) {
            reviewsReference.removeEventListener(reviewsListener);
            reviewsListener = null;
        }
    }

    private void renderMarkers() {
        if (mMap == null) {
            return;
        }

        mMap.clear();
        markerPayloads.clear();

        Map<String, SpotSummary> summaries = buildSpotSummaries();
        for (StudySpot spot : studySpots) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(spot.getLatitude(), spot.getLongitude()))
                    .title(spot.getSpotName()));
            if (marker == null) {
                continue;
            }
            MarkerPayload payload = MarkerPayload.forExistingSpot(spot, summaries.get(spot.getSpotId()));
            marker.setTag(payload);
            markerPayloads.put(marker.getId(), payload);
        }

        if (draftLatLng != null) {
            Marker draftMarker = mMap.addMarker(new MarkerOptions()
                    .position(draftLatLng)
                    .title(getString(R.string.new_spot_marker_title)));
            if (draftMarker != null) {
                MarkerPayload payload = MarkerPayload.forDraft(draftLatLng);
                draftMarker.setTag(payload);
                markerPayloads.put(draftMarker.getId(), payload);
                draftMarker.showInfoWindow();
            }
        }
    }

    private Map<String, SpotSummary> buildSpotSummaries() {
        Map<String, List<Review>> reviewsBySpotId = new HashMap<>();
        for (Review review : reviews) {
            if (TextUtils.isEmpty(review.getSpotId())) {
                continue;
            }
            List<Review> spotReviews = reviewsBySpotId.get(review.getSpotId());
            if (spotReviews == null) {
                spotReviews = new ArrayList<>();
                reviewsBySpotId.put(review.getSpotId(), spotReviews);
            }
            spotReviews.add(review);
        }

        Map<String, SpotSummary> summaries = new HashMap<>();
        for (Map.Entry<String, List<Review>> entry : reviewsBySpotId.entrySet()) {
            List<Review> spotReviews = entry.getValue();
            double averageRating = 0.0;
            for (Review review : spotReviews) {
                averageRating += review.getStarRating();
            }
            if (!spotReviews.isEmpty()) {
                averageRating /= spotReviews.size();
            }
            summaries.put(entry.getKey(), new SpotSummary(
                    averageRating,
                    spotReviews.size(),
                    SpotUiFormatter.collectTopTraits(spotReviews, 3)
            ));
        }
        return summaries;
    }

    private void handleMapLongPress(LatLng latLng) {
        draftLatLng = latLng;
        renderMarkers();
    }

    private void handleInfoWindowClick(Marker marker) {
        MarkerPayload payload = markerPayloads.get(marker.getId());
        if (payload == null) {
            payload = (MarkerPayload) marker.getTag();
        }
        if (payload == null) {
            return;
        }

        if (payload.isDraft()) {
            Intent intent = new Intent(this, CreateReviewActivity.class);
            intent.putExtra(CreateReviewActivity.EXTRA_LATITUDE, payload.latitude);
            intent.putExtra(CreateReviewActivity.EXTRA_LONGITUDE, payload.longitude);
            draftLatLng = null;
            renderMarkers();
            startActivity(intent);
            return;
        }

        RecentViewedSpotStore.recordViewedSpot(this, payload.spot.getSpotId());
        Intent intent = new Intent(this, SpotDetailsActivity.class);
        intent.putExtra(SpotDetailsActivity.EXTRA_SPOT_ID, payload.spot.getSpotId());
        startActivity(intent);
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1
            );
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    private String buildSpotSubtitle(StudySpot spot, SpotSummary summary) {
        StringBuilder builder = new StringBuilder();
        builder.append(spot.getBuildingName());
        if (!TextUtils.isEmpty(spot.getRoomNumber())) {
            builder.append(" | ").append(spot.getRoomNumber());
        }
        builder.append('\n');
        if (summary == null) {
            builder.append(getString(R.string.map_marker_no_reviews));
        } else {
            builder.append(SpotUiFormatter.formatRatingSummary(
                    summary.averageRating,
                    summary.reviewCount
            ));
        }
        return builder.toString();
    }

    private final class StudySpotInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final LayoutInflater layoutInflater = LayoutInflater.from(HomeActivity.this);

        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            MarkerPayload payload = markerPayloads.get(marker.getId());
            if (payload == null) {
                payload = (MarkerPayload) marker.getTag();
            }
            if (payload == null) {
                return null;
            }

            if (payload.isDraft()) {
                View draftInfoView = layoutInflater.inflate(R.layout.map_info_window_new_spot, null, false);
                TextView titleTextView = draftInfoView.findViewById(R.id.infoTitleTextView);
                TextView actionTextView = draftInfoView.findViewById(R.id.infoActionTextView);
                titleTextView.setText(R.string.new_spot_marker_title);
                actionTextView.setTextColor(getColor(android.R.color.white));
                actionTextView.setText(R.string.create_review_button);
                return draftInfoView;
            }

            View existingInfoView = layoutInflater.inflate(R.layout.map_info_window, null, false);
            TextView titleTextView = existingInfoView.findViewById(R.id.infoTitleTextView);
            TextView subtitleTextView = existingInfoView.findViewById(R.id.infoSubtitleTextView);
            TextView tagsTextView = existingInfoView.findViewById(R.id.infoTagsTextView);
            TextView actionTextView = existingInfoView.findViewById(R.id.infoActionTextView);
            actionTextView.setVisibility(View.VISIBLE);
            actionTextView.setTextColor(getColor(android.R.color.white));
            titleTextView.setText(payload.spot.getSpotName());
            subtitleTextView.setText(buildSpotSubtitle(payload.spot, payload.summary));
            if (payload.summary == null || payload.summary.tags.isEmpty()) {
                tagsTextView.setText(R.string.map_marker_no_tags);
            } else {
                tagsTextView.setText(SpotUiFormatter.formatTraitList(payload.summary.tags));
            }
            actionTextView.setText(R.string.view_reviews_button);
            return existingInfoView;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null;
        }
    }

    private static final class MarkerPayload {
        private final StudySpot spot;
        private final SpotSummary summary;
        private final Double latitude;
        private final Double longitude;

        private MarkerPayload(StudySpot spot, SpotSummary summary, Double latitude, Double longitude) {
            this.spot = spot;
            this.summary = summary;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        static MarkerPayload forExistingSpot(StudySpot spot, SpotSummary summary) {
            return new MarkerPayload(spot, summary, null, null);
        }

        static MarkerPayload forDraft(LatLng latLng) {
            return new MarkerPayload(null, null, latLng.latitude, latLng.longitude);
        }

        boolean isDraft() {
            return spot == null;
        }
    }

    private static final class SpotSummary {
        private final double averageRating;
        private final int reviewCount;
        private final List<String> tags;

        private SpotSummary(double averageRating, int reviewCount, List<String> tags) {
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
            this.tags = tags;
        }
    }
}
