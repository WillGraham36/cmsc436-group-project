package com.example.group_project.controller

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_project.R
import com.example.group_project.model.Review
import com.example.group_project.model.StudySpot
import com.example.group_project.util.SpotUiFormatter
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class SpotDetailsActivity : BaseBottomNavActivity() {

    private lateinit var spotNameTextView: TextView
    private lateinit var buildingTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var tagsTextView: TextView
    private lateinit var coordinatesTextView: TextView
    private lateinit var emptyStateTextView: TextView
    private lateinit var reviewAdapter: ReviewAdapter

    private lateinit var spotsReference: DatabaseReference
    private lateinit var reviewsQuery: Query
    private var spotListener: ValueEventListener? = null
    private var reviewsListener: ValueEventListener? = null
    private var spotId: String? = null
    private var currentSpot: StudySpot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_details)

        spotId = intent.getStringExtra(EXTRA_SPOT_ID)
        if (spotId.isNullOrEmpty()) {
            Toast.makeText(this, R.string.spot_details_missing_spot, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val backButton: ImageButton = findViewById(R.id.backButton)
        val addReviewButton: MaterialButton = findViewById(R.id.addReviewButton)
        val reviewsRecyclerView: RecyclerView = findViewById(R.id.reviewsRecyclerView)

        spotNameTextView = findViewById(R.id.spotNameTextView)
        buildingTextView = findViewById(R.id.buildingTextView)
        summaryTextView = findViewById(R.id.summaryTextView)
        tagsTextView = findViewById(R.id.tagsTextView)
        coordinatesTextView = findViewById(R.id.coordinatesTextView)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        // Reviews on this page use the same card as recent and profile reviews
        // requirement: 2 instances of views not reviewed in class - recycler view for spot reviews
        reviewAdapter = ReviewAdapter(ArrayList())
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsRecyclerView.adapter = reviewAdapter

        spotsReference = FirebaseDatabase.getInstance().getReference("pins")
        // Only listen to reviews for the selected spot
        // requirement: meaningful remote data (getting firebase data) - SpotDetails loads Firebase reviews for this spot
        reviewsQuery = FirebaseDatabase.getInstance()
            .getReference("reviews")
            .orderByChild("spotId")
            .equalTo(spotId)

        backButton.setOnClickListener { finish() }
        addReviewButton.setOnClickListener { launchCreateReview() }
    }

    override fun onStart() {
        super.onStart()
        attachListeners()
    }

    override fun onStop() {
        super.onStop()
        detachListeners()
    }

    private fun attachListeners() {
        // Keep the spot info and its reviews live while this screen is open
        if (spotListener == null) {
            spotListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentSpot = snapshot.getValue(StudySpot::class.java)
                    if (currentSpot == null) {
                        Toast.makeText(
                            this@SpotDetailsActivity,
                            R.string.spot_details_missing_spot,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        return
                    }
                    bindSpot(currentSpot!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SpotDetailsActivity,
                        R.string.spot_details_load_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            spotsReference.child(spotId!!).addValueEventListener(spotListener!!)
        }

        if (reviewsListener == null) {
            reviewsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Newest reviews should appear at the top
                    val reviews = ArrayList<Review>()
                    for (child in snapshot.children) {
                        child.getValue(Review::class.java)?.let(reviews::add)
                    }

                    reviews.sortByDescending { it.timestamp }
                    reviewAdapter.setReviews(reviews)
                    emptyStateTextView.visibility = if (reviews.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    if (reviews.isEmpty()) {
                        emptyStateTextView.setText(R.string.spot_details_no_reviews)
                    }

                    updateSummary(reviews)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SpotDetailsActivity,
                        R.string.spot_details_load_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            reviewsQuery.addValueEventListener(reviewsListener!!)
        }
    }

    private fun detachListeners() {
        spotListener?.let {
            spotsReference.child(spotId!!).removeEventListener(it)
            spotListener = null
        }
        reviewsListener?.let {
            reviewsQuery.removeEventListener(it)
            reviewsListener = null
        }
    }

    private fun bindSpot(spot: StudySpot) {
        spotNameTextView.text = spot.spotName
        buildingTextView.text = buildBuildingLine(spot)
        coordinatesTextView.text = String.format(
            Locale.US,
            "%.5f, %.5f",
            spot.latitude,
            spot.longitude
        )
    }

    private fun updateSummary(reviews: List<Review>) {
        // Recalculate the header every time Firebase sends a review update
        val reviewCount = reviews.size
        var averageRating = 0.0
        for (review in reviews) {
            averageRating += review.starRating
        }
        if (reviewCount > 0) {
            averageRating /= reviewCount
        }

        summaryTextView.text = SpotUiFormatter.formatRatingSummary(averageRating, reviewCount)
        tagsTextView.text = SpotUiFormatter.formatTraitList(SpotUiFormatter.uniqueTraits(reviews))
    }

    private fun buildBuildingLine(spot: StudySpot): String {
        if (TextUtils.isEmpty(spot.roomNumber)) {
            return spot.buildingName
        }
        return "${spot.buildingName} | ${spot.roomNumber}"
    }

    private fun launchCreateReview() {
        // Pass spot data forward so the form can lock the location fields
        val spot = currentSpot
        if (spot == null) {
            Toast.makeText(this, R.string.spot_details_missing_spot, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CreateReviewActivity::class.java)
        // requirement: 2 views sharing data between each other - SpotDetailsActivity passes spot info to CreateReviewActivity
        intent.putExtra(CreateReviewActivity.EXTRA_SPOT_ID, spot.spotId)
        intent.putExtra(CreateReviewActivity.EXTRA_SPOT_NAME, spot.spotName)
        intent.putExtra(CreateReviewActivity.EXTRA_BUILDING_NAME, spot.buildingName)
        intent.putExtra(CreateReviewActivity.EXTRA_ROOM_NUMBER, spot.roomNumber)
        intent.putExtra(CreateReviewActivity.EXTRA_LATITUDE, spot.latitude)
        intent.putExtra(CreateReviewActivity.EXTRA_LONGITUDE, spot.longitude)
        startActivity(intent)
    }

    companion object {
        const val EXTRA_SPOT_ID = "extra_spot_id"
    }
}
