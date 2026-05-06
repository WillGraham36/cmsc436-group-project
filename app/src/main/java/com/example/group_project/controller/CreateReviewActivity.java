package com.example.group_project.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateReviewActivity extends AppCompatActivity {
    private static final String TAG = "CreateReviewActivity";

    public static final String EXTRA_SPOT_ID = "extra_spot_id";
    public static final String EXTRA_SPOT_NAME = "extra_spot_name";
    public static final String EXTRA_BUILDING_NAME = "extra_building_name";
    public static final String EXTRA_ROOM_NUMBER = "extra_room_number";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    private TextInputEditText spotNameEditText;
    private TextInputEditText buildingNameEditText;
    private TextInputEditText roomNumberEditText;
    private TextInputEditText descriptionEditText;
    private TextView selectedSpotTextView;
    private RatingBar ratingBar;
    private CheckBox quietCheckBox;
    private CheckBox moderatelyLoudCheckBox;
    private CheckBox loudCheckBox;
    private CheckBox visibleCheckBox;
    private CheckBox secludedCheckBox;
    private MaterialButton submitReviewButton;
    private DatabaseReference reviewsReference;
    private DatabaseReference spotsReference;
    private DatabaseReference rootReference;
    private String spotId;
    private double latitude;
    private double longitude;
    private boolean hasCoordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        ImageButton backButton = findViewById(R.id.backButton);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        selectedSpotTextView = findViewById(R.id.selectedSpotTextView);
        spotNameEditText = findViewById(R.id.spotNameEditText);
        buildingNameEditText = findViewById(R.id.buildingNameEditText);
        roomNumberEditText = findViewById(R.id.roomNumberEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        ratingBar = findViewById(R.id.ratingBar);
        quietCheckBox = findViewById(R.id.quietCheckBox);
        moderatelyLoudCheckBox = findViewById(R.id.moderatelyLoudCheckBox);
        loudCheckBox = findViewById(R.id.loudCheckBox);
        visibleCheckBox = findViewById(R.id.visibleCheckBox);
        secludedCheckBox = findViewById(R.id.secludedCheckBox);

        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews");
        spotsReference = FirebaseDatabase.getInstance().getReference("pins");
        rootReference = FirebaseDatabase.getInstance().getReference();

        spotId = getIntent().getStringExtra(EXTRA_SPOT_ID);
        hasCoordinates = getIntent().hasExtra(EXTRA_LATITUDE) && getIntent().hasExtra(EXTRA_LONGITUDE);
        latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0.0);
        longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0.0);

        bindSelectedSpot();

        backButton.setOnClickListener(view -> finish());
        submitReviewButton.setOnClickListener(view -> submitReview());
    }

    private void submitReview() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.review_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String spotName = getText(spotNameEditText);
        String buildingName = getText(buildingNameEditText);
        String roomNumber = getText(roomNumberEditText);
        String description = getText(descriptionEditText);
        int rating = Math.round(ratingBar.getRating());
        if (TextUtils.isEmpty(spotName) || TextUtils.isEmpty(buildingName)
                || TextUtils.isEmpty(description) || rating == 0) {
            Toast.makeText(this, R.string.review_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(spotId) && !hasCoordinates) {
            Toast.makeText(this, R.string.review_missing_map_context, Toast.LENGTH_SHORT).show();
            return;
        }

        submitReviewButton.setEnabled(false);

        String reviewId = reviewsReference.push().getKey();
        String resolvedSpotId = TextUtils.isEmpty(spotId) ? spotsReference.push().getKey() : spotId;
        long timestamp = System.currentTimeMillis();
        if (reviewId == null || resolvedSpotId == null) {
            submitReviewButton.setEnabled(true);
            Toast.makeText(this, R.string.review_submit_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> review = new HashMap<>();
        review.put("reviewId", reviewId);
        review.put("spotId", resolvedSpotId);
        review.put("spotName", spotName);
        review.put("buildingName", buildingName);
        review.put("roomNumber", roomNumber);
        review.put("description", description);
        review.put("starRating", (double) rating);
        review.put("traits", getSelectedTraits());
        review.put("authorId", user.getUid());
        review.put("userId", user.getUid());
        review.put("username", getUsername(user));
        review.put("userEmail", user.getEmail());
        review.put("timestamp", timestamp);
        review.put("createdAt", ServerValue.TIMESTAMP);
        review.put("clientCreatedAt", timestamp);
        review.put("quiet", quietCheckBox.isChecked());
        review.put("moderatelyLoud", moderatelyLoudCheckBox.isChecked());
        review.put("loud", loudCheckBox.isChecked());
        review.put("visible", visibleCheckBox.isChecked());
        review.put("secluded", secludedCheckBox.isChecked());

        Map<String, Object> updates = new HashMap<>();
        updates.put("reviews/" + reviewId, review);

        if (TextUtils.isEmpty(spotId)) {
            Map<String, Object> spot = new HashMap<>();
            spot.put("spotId", resolvedSpotId);
            spot.put("spotName", spotName);
            spot.put("buildingName", buildingName);
            spot.put("roomNumber", roomNumber);
            spot.put("latitude", latitude);
            spot.put("longitude", longitude);
            spot.put("createdAt", ServerValue.TIMESTAMP);
            spot.put("createdBy", user.getUid());
            updates.put("pins/" + resolvedSpotId, spot);
        }

        try {
            rootReference.updateChildren(updates).addOnCompleteListener(task -> {
                submitReviewButton.setEnabled(true);
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Review submit failed", task.getException());
                    Toast.makeText(
                            this,
                            buildSubmitFailureMessage(task.getException()),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Toast.makeText(this, R.string.review_submit_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        } catch (RuntimeException e) {
            submitReviewButton.setEnabled(true);
            Log.w(TAG, "Review submit could not be started", e);
            Toast.makeText(this, buildSubmitFailureMessage(e), Toast.LENGTH_LONG).show();
        }
    }

    private String buildSubmitFailureMessage(Exception exception) {
        String message = getString(R.string.review_submit_failure);
        if (exception == null || exception.getMessage() == null || exception.getMessage().trim().isEmpty()) {
            return message;
        }
        return message + " " + exception.getMessage();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private List<String> getSelectedTraits() {
        List<String> traits = new ArrayList<>();
        addTraitIfChecked(traits, quietCheckBox);
        addTraitIfChecked(traits, moderatelyLoudCheckBox);
        addTraitIfChecked(traits, loudCheckBox);
        addTraitIfChecked(traits, visibleCheckBox);
        addTraitIfChecked(traits, secludedCheckBox);
        return traits;
    }

    private void addTraitIfChecked(List<String> traits, CheckBox checkBox) {
        if (checkBox.isChecked()) {
            traits.add(checkBox.getText().toString());
        }
    }

    private String getUsername(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getEmail();
        }
        return displayName == null ? getString(R.string.anonymous_user) : displayName;
    }

    private void bindSelectedSpot() {
        String initialSpotName = getIntent().getStringExtra(EXTRA_SPOT_NAME);
        String initialBuildingName = getIntent().getStringExtra(EXTRA_BUILDING_NAME);
        String initialRoomNumber = getIntent().getStringExtra(EXTRA_ROOM_NUMBER);

        if (!TextUtils.isEmpty(initialSpotName)) {
            spotNameEditText.setText(initialSpotName);
        }
        if (!TextUtils.isEmpty(initialBuildingName)) {
            buildingNameEditText.setText(initialBuildingName);
        }
        if (!TextUtils.isEmpty(initialRoomNumber)) {
            roomNumberEditText.setText(initialRoomNumber);
        }

        if (TextUtils.isEmpty(spotId)) {
            selectedSpotTextView.setText(R.string.create_review_new_spot_message);
            return;
        }

        if (TextUtils.isEmpty(initialSpotName)) {
            initialSpotName = getString(R.string.study_spot_name);
        }
        selectedSpotTextView.setText(getString(
                R.string.create_review_existing_spot_message,
                initialSpotName
        ));
        spotNameEditText.setEnabled(false);
        buildingNameEditText.setEnabled(false);
        roomNumberEditText.setEnabled(false);
    }
}
