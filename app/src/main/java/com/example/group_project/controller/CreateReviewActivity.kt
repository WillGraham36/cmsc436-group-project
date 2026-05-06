package com.example.group_project.controller

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.group_project.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlin.math.roundToInt

class CreateReviewActivity : AppCompatActivity() {

    private lateinit var spotNameEditText: TextInputEditText
    private lateinit var buildingNameEditText: TextInputEditText
    private lateinit var roomNumberEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var selectedSpotTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var quietCheckBox: CheckBox
    private lateinit var moderatelyLoudCheckBox: CheckBox
    private lateinit var loudCheckBox: CheckBox
    private lateinit var visibleCheckBox: CheckBox
    private lateinit var secludedCheckBox: CheckBox
    private lateinit var submitReviewButton: MaterialButton
    private lateinit var reviewsReference: DatabaseReference
    private lateinit var spotsReference: DatabaseReference
    private lateinit var rootReference: DatabaseReference
    private var spotId: String? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var hasCoordinates = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_review)

        val backButton: ImageButton = findViewById(R.id.backButton)
        submitReviewButton = findViewById(R.id.submitReviewButton)
        selectedSpotTextView = findViewById(R.id.selectedSpotTextView)
        spotNameEditText = findViewById(R.id.spotNameEditText)
        buildingNameEditText = findViewById(R.id.buildingNameEditText)
        roomNumberEditText = findViewById(R.id.roomNumberEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        ratingBar = findViewById(R.id.ratingBar)
        quietCheckBox = findViewById(R.id.quietCheckBox)
        moderatelyLoudCheckBox = findViewById(R.id.moderatelyLoudCheckBox)
        loudCheckBox = findViewById(R.id.loudCheckBox)
        visibleCheckBox = findViewById(R.id.visibleCheckBox)
        secludedCheckBox = findViewById(R.id.secludedCheckBox)

        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews")
        spotsReference = FirebaseDatabase.getInstance().getReference("pins")
        rootReference = FirebaseDatabase.getInstance().reference

        spotId = intent.getStringExtra(EXTRA_SPOT_ID)
        hasCoordinates = intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LONGITUDE)
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)

        bindSelectedSpot()

        backButton.setOnClickListener { finish() }
        submitReviewButton.setOnClickListener { submitReview() }
    }

    private fun submitReview() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, R.string.review_login_required, Toast.LENGTH_SHORT).show()
            return
        }

        val spotName = getText(spotNameEditText)
        val buildingName = getText(buildingNameEditText)
        val roomNumber = getText(roomNumberEditText)
        val description = getText(descriptionEditText)
        val rating = ratingBar.rating.roundToInt()
        if (TextUtils.isEmpty(spotName) || TextUtils.isEmpty(buildingName)
            || TextUtils.isEmpty(description) || rating == 0
        ) {
            Toast.makeText(this, R.string.review_required_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(spotId) && !hasCoordinates) {
            Toast.makeText(this, R.string.review_missing_map_context, Toast.LENGTH_SHORT).show()
            return
        }

        submitReviewButton.isEnabled = false

        val reviewId = reviewsReference.push().key
        val resolvedSpotId = if (TextUtils.isEmpty(spotId)) spotsReference.push().key else spotId
        val timestamp = System.currentTimeMillis()
        if (reviewId == null || resolvedSpotId == null) {
            submitReviewButton.isEnabled = true
            Toast.makeText(this, R.string.review_submit_failure, Toast.LENGTH_SHORT).show()
            return
        }

        val review = hashMapOf<String, Any?>(
            "reviewId" to reviewId,
            "spotId" to resolvedSpotId,
            "spotName" to spotName,
            "buildingName" to buildingName,
            "roomNumber" to roomNumber,
            "description" to description,
            "starRating" to rating.toDouble(),
            "traits" to getSelectedTraits(),
            "authorId" to user.uid,
            "userId" to user.uid,
            "username" to getUsername(user),
            "userEmail" to user.email,
            "timestamp" to timestamp,
            "createdAt" to ServerValue.TIMESTAMP,
            "clientCreatedAt" to timestamp,
            "quiet" to quietCheckBox.isChecked,
            "moderatelyLoud" to moderatelyLoudCheckBox.isChecked,
            "loud" to loudCheckBox.isChecked,
            "visible" to visibleCheckBox.isChecked,
            "secluded" to secludedCheckBox.isChecked
        )

        val updates = hashMapOf<String, Any?>(
            "reviews/$reviewId" to review
        )

        if (TextUtils.isEmpty(spotId)) {
            val spot = hashMapOf<String, Any?>(
                "spotId" to resolvedSpotId,
                "spotName" to spotName,
                "buildingName" to buildingName,
                "roomNumber" to roomNumber,
                "latitude" to latitude,
                "longitude" to longitude,
                "createdAt" to ServerValue.TIMESTAMP,
                "createdBy" to user.uid
            )
            updates["pins/$resolvedSpotId"] = spot
        }

        try {
            rootReference.updateChildren(updates).addOnCompleteListener { task ->
                submitReviewButton.isEnabled = true
                if (!task.isSuccessful) {
                    Log.w(TAG, "Review submit failed", task.exception)
                    Toast.makeText(
                        this,
                        buildSubmitFailureMessage(task.exception),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnCompleteListener
                }

                Toast.makeText(this, R.string.review_submit_success, Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: RuntimeException) {
            submitReviewButton.isEnabled = true
            Log.w(TAG, "Review submit could not be started", e)
            Toast.makeText(this, buildSubmitFailureMessage(e), Toast.LENGTH_LONG).show()
        }
    }

    private fun buildSubmitFailureMessage(exception: Exception?): String {
        val message = getString(R.string.review_submit_failure)
        val exceptionMessage = exception?.message
        if (exceptionMessage.isNullOrBlank()) {
            return message
        }
        return "$message $exceptionMessage"
    }

    private fun getText(editText: TextInputEditText): String {
        return editText.text?.toString()?.trim().orEmpty()
    }

    private fun getSelectedTraits(): List<String> {
        val traits = ArrayList<String>()
        addTraitIfChecked(traits, quietCheckBox)
        addTraitIfChecked(traits, moderatelyLoudCheckBox)
        addTraitIfChecked(traits, loudCheckBox)
        addTraitIfChecked(traits, visibleCheckBox)
        addTraitIfChecked(traits, secludedCheckBox)
        return traits
    }

    private fun addTraitIfChecked(traits: MutableList<String>, checkBox: CheckBox) {
        if (checkBox.isChecked) {
            traits.add(checkBox.text.toString())
        }
    }

    private fun getUsername(user: FirebaseUser): String {
        var displayName = user.displayName
        if (displayName.isNullOrBlank()) {
            displayName = user.email
        }
        return displayName ?: getString(R.string.anonymous_user)
    }

    private fun bindSelectedSpot() {
        var initialSpotName = intent.getStringExtra(EXTRA_SPOT_NAME)
        val initialBuildingName = intent.getStringExtra(EXTRA_BUILDING_NAME)
        val initialRoomNumber = intent.getStringExtra(EXTRA_ROOM_NUMBER)

        if (!initialSpotName.isNullOrEmpty()) {
            spotNameEditText.setText(initialSpotName)
        }
        if (!initialBuildingName.isNullOrEmpty()) {
            buildingNameEditText.setText(initialBuildingName)
        }
        if (!initialRoomNumber.isNullOrEmpty()) {
            roomNumberEditText.setText(initialRoomNumber)
        }

        if (TextUtils.isEmpty(spotId)) {
            selectedSpotTextView.setText(R.string.create_review_new_spot_message)
            return
        }

        if (initialSpotName.isNullOrEmpty()) {
            initialSpotName = getString(R.string.study_spot_name)
        }
        selectedSpotTextView.text = getString(
            R.string.create_review_existing_spot_message,
            initialSpotName
        )
        spotNameEditText.isEnabled = false
        buildingNameEditText.isEnabled = false
        roomNumberEditText.isEnabled = false
    }

    companion object {
        private const val TAG = "CreateReviewActivity"

        const val EXTRA_SPOT_ID = "extra_spot_id"
        const val EXTRA_SPOT_NAME = "extra_spot_name"
        const val EXTRA_BUILDING_NAME = "extra_building_name"
        const val EXTRA_ROOM_NUMBER = "extra_room_number"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }
}
