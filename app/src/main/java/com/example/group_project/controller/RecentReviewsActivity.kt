package com.example.group_project.controller

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_project.R
import com.example.group_project.model.Review
import com.example.group_project.util.RecentViewedSpotStore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class RecentReviewsActivity : BaseBottomNavActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var reviewsReference: DatabaseReference
    private val reviewsQueries = ArrayList<Query>()
    private val reviewsListeners = ArrayList<ValueEventListener>()
    private val reviewsBySpotId = HashMap<String, List<Review>>()
    private val recentSpotIds = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_reviews)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        setupBottomNavigation(bottomNavigationView, R.id.navigation_recent_reviews)

        recyclerView = findViewById(R.id.recyclerViewReviews)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        reviewAdapter = ReviewAdapter(ArrayList())
        recyclerView.adapter = reviewAdapter

        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews")
    }

    override fun onStart() {
        super.onStart()
        attachReviewsListener()
    }

    override fun onStop() {
        super.onStop()
        detachReviewsListener()
    }

    private fun attachReviewsListener() {
        detachReviewsListener()
        recentSpotIds.clear()
        recentSpotIds.addAll(RecentViewedSpotStore.getRecentSpotIds(this))

        if (recentSpotIds.isEmpty()) {
            reviewAdapter.setReviews(ArrayList())
            emptyStateTextView.visibility = View.VISIBLE
            emptyStateTextView.setText(R.string.recent_reviews_empty)
            return
        }

        emptyStateTextView.visibility = View.GONE

        for (spotId in recentSpotIds) {
            val reviewsQuery = reviewsReference.orderByChild("spotId").equalTo(spotId)
            val reviewsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loadedReviews = ArrayList<Review>()
                    for (child in snapshot.children) {
                        child.getValue(Review::class.java)?.let(loadedReviews::add)
                    }

                    loadedReviews.sortByDescending { it.timestamp }
                    reviewsBySpotId[spotId] = loadedReviews
                    renderRecentReviews()
                }

                override fun onCancelled(error: DatabaseError) {
                    emptyStateTextView.visibility = View.VISIBLE
                    emptyStateTextView.setText(R.string.recent_reviews_error)
                }
            }

            reviewsQueries.add(reviewsQuery)
            reviewsListeners.add(reviewsListener)
            reviewsQuery.addValueEventListener(reviewsListener)
        }
    }

    private fun detachReviewsListener() {
        for (i in reviewsQueries.indices) {
            reviewsQueries[i].removeEventListener(reviewsListeners[i])
        }
        reviewsQueries.clear()
        reviewsListeners.clear()
        reviewsBySpotId.clear()
    }

    private fun renderRecentReviews() {
        val orderedReviews = ArrayList<Review>()
        for (spotId in recentSpotIds) {
            reviewsBySpotId[spotId]?.let(orderedReviews::addAll)
        }

        reviewAdapter.setReviews(orderedReviews)
        emptyStateTextView.visibility = if (orderedReviews.isEmpty()) View.VISIBLE else View.GONE
        if (orderedReviews.isEmpty()) {
            emptyStateTextView.setText(R.string.recent_reviews_empty)
        }
    }
}
