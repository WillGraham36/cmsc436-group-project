package com.example.group_project.controller;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group_project.R;
import com.google.android.material.button.MaterialButton;

public class CreateReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton uploadImagesButton = findViewById(R.id.uploadImagesButton);
        MaterialButton submitReviewButton = findViewById(R.id.submitReviewButton);

        backButton.setOnClickListener(view -> finish());
        uploadImagesButton.setOnClickListener(view ->
                Toast.makeText(this, R.string.prototype_upload_toast, Toast.LENGTH_SHORT).show());
        submitReviewButton.setOnClickListener(view -> {
            Toast.makeText(this, R.string.prototype_submit_toast, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
