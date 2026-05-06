package com.example.group_project.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RatingBar;
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

    private TextInputEditText spotNameEditText;
    private TextInputEditText buildingNameEditText;
    private TextInputEditText roomNumberEditText;
    private TextInputEditText descriptionEditText;
    private TextInputEditText latitudeEditText;
    private TextInputEditText longitudeEditText;
    private RatingBar ratingBar;
    private CheckBox quietCheckBox;
    private CheckBox moderatelyLoudCheckBox;
    private CheckBox loudCheckBox;
    private CheckBox visibleCheckBox;
    private CheckBox secludedCheckBox;
    private MaterialButton submitReviewButton;
    private DatabaseReference reviewsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton uploadImagesButton = findViewById(R.id.uploadImagesButton);
        submitReviewButton = findViewById(R.id.submitReviewButton);
        spotNameEditText = findViewById(R.id.spotNameEditText);
        buildingNameEditText = findViewById(R.id.buildingNameEditText);
        roomNumberEditText = findViewById(R.id.roomNumberEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        latitudeEditText = findViewById(R.id.latitudeEditText);
        longitudeEditText = findViewById(R.id.longitudeEditText);
        ratingBar = findViewById(R.id.ratingBar);
        quietCheckBox = findViewById(R.id.quietCheckBox);
        moderatelyLoudCheckBox = findViewById(R.id.moderatelyLoudCheckBox);
        loudCheckBox = findViewById(R.id.loudCheckBox);
        visibleCheckBox = findViewById(R.id.visibleCheckBox);
        secludedCheckBox = findViewById(R.id.secludedCheckBox);
        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews");

        backButton.setOnClickListener(view -> finish());
        uploadImagesButton.setOnClickListener(view ->
                Toast.makeText(this, R.string.prototype_upload_toast, Toast.LENGTH_SHORT).show());
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
        String description = getText(descriptionEditText);
        int rating = Math.round(ratingBar.getRating());
        if (TextUtils.isEmpty(spotName) || TextUtils.isEmpty(buildingName)
                || TextUtils.isEmpty(description) || rating == 0) {
            Toast.makeText(this, R.string.review_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        submitReviewButton.setEnabled(false);

        Map<String, Object> review = new HashMap<>();
        String reviewId = reviewsReference.push().getKey();
        long timestamp = System.currentTimeMillis();
        Double latitude = parseOptionalDouble(latitudeEditText);
        Double longitude = parseOptionalDouble(longitudeEditText);

        review.put("reviewId", reviewId);
        review.put("spotName", spotName);
        review.put("buildingName", buildingName);
        review.put("roomNumber", getText(roomNumberEditText));
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
        if (latitude != null) {
            review.put("latitude", latitude);
        }
        if (longitude != null) {
            review.put("longitude", longitude);
        }

        if (reviewId == null) {
            submitReviewButton.setEnabled(true);
            Toast.makeText(this, R.string.review_submit_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            reviewsReference.child(reviewId)
                    .setValue(review)
                    .addOnCompleteListener(task -> {
                        submitReviewButton.setEnabled(true);
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Review submit failed", task.getException());
                            Toast.makeText(
                                    this,
                                    buildSubmitFailureMessage(task.getException()),
                                    Toast.LENGTH_LONG).show();
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

    private Double parseOptionalDouble(TextInputEditText editText) {
        String value = getText(editText);
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getUsername(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = user.getEmail();
        }
        return displayName == null ? getString(R.string.anonymous_user) : displayName;
    }
}
