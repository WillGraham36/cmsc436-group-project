package com.example.group_project.controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.group_project.R
import com.example.group_project.model.Review
import com.example.group_project.model.StudySpot
import com.example.group_project.util.RecentViewedSpotStore
import com.example.group_project.util.SpotUiFormatter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : BaseBottomNavActivity(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private lateinit var spotsReference: DatabaseReference
    private lateinit var reviewsReference: DatabaseReference
    private var spotsListener: ValueEventListener? = null
    private var reviewsListener: ValueEventListener? = null
    private val studySpots = ArrayList<StudySpot>()
    private val reviews = ArrayList<Review>()
    private val markerPayloads = HashMap<String, MarkerPayload>()
    private var draftLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        setupBottomNavigation(bottomNavigationView, R.id.navigation_home)

        spotsReference = FirebaseDatabase.getInstance().getReference("pins")
        reviewsReference = FirebaseDatabase.getInstance().getReference("reviews")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        attachDatabaseListeners()
    }

    override fun onStop() {
        super.onStop()
        detachDatabaseListeners()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val umd = LatLng(38.9869, -76.9426)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(umd, 15f))
        googleMap.setInfoWindowAdapter(StudySpotInfoWindowAdapter())
        googleMap.setOnInfoWindowClickListener(::handleInfoWindowClick)
        googleMap.setOnMapLongClickListener(::handleMapLongPress)
        renderMarkers()
        enableUserLocation()
    }

    private fun attachDatabaseListeners() {
        if (spotsListener == null) {
            spotsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studySpots.clear()
                    for (child in snapshot.children) {
                        child.getValue(StudySpot::class.java)?.let(studySpots::add)
                    }
                    renderMarkers()
                }

                override fun onCancelled(error: DatabaseError) = Unit
            }
            spotsReference.addValueEventListener(spotsListener!!)
        }

        if (reviewsListener == null) {
            reviewsListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviews.clear()
                    for (child in snapshot.children) {
                        child.getValue(Review::class.java)?.let(reviews::add)
                    }
                    renderMarkers()
                }

                override fun onCancelled(error: DatabaseError) = Unit
            }
            reviewsReference.addValueEventListener(reviewsListener!!)
        }
    }

    private fun detachDatabaseListeners() {
        spotsListener?.let {
            spotsReference.removeEventListener(it)
            spotsListener = null
        }
        reviewsListener?.let {
            reviewsReference.removeEventListener(it)
            reviewsListener = null
        }
    }

    private fun renderMarkers() {
        val googleMap = map ?: return

        googleMap.clear()
        markerPayloads.clear()

        val summaries = buildSpotSummaries()
        for (spot in studySpots) {
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(spot.latitude, spot.longitude))
                    .title(spot.spotName)
            ) ?: continue

            val payload = MarkerPayload.forExistingSpot(spot, summaries[spot.spotId])
            marker.tag = payload
            markerPayloads[marker.id] = payload
        }

        draftLatLng?.let { latLng ->
            val draftMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.new_spot_marker_title))
            )
            if (draftMarker != null) {
                val payload = MarkerPayload.forDraft(latLng)
                draftMarker.tag = payload
                markerPayloads[draftMarker.id] = payload
                draftMarker.showInfoWindow()
            }
        }
    }

    private fun buildSpotSummaries(): Map<String, SpotSummary> {
        val reviewsBySpotId = HashMap<String, MutableList<Review>>()
        for (review in reviews) {
            val spotId = review.spotId
            if (spotId.isBlank()) {
                continue
            }

            val spotReviews = reviewsBySpotId.getOrPut(spotId) { ArrayList() }
            spotReviews.add(review)
        }

        val summaries = HashMap<String, SpotSummary>()
        for ((spotId, spotReviews) in reviewsBySpotId) {
            var averageRating = 0.0
            for (review in spotReviews) {
                averageRating += review.starRating
            }
            if (spotReviews.isNotEmpty()) {
                averageRating /= spotReviews.size
            }
            summaries[spotId] = SpotSummary(
                averageRating,
                spotReviews.size,
                SpotUiFormatter.collectTopTraits(spotReviews, 3)
            )
        }
        return summaries
    }

    private fun handleMapLongPress(latLng: LatLng) {
        draftLatLng = latLng
        renderMarkers()
    }

    private fun handleInfoWindowClick(marker: Marker) {
        var payload = markerPayloads[marker.id]
        if (payload == null) {
            payload = marker.tag as? MarkerPayload
        }
        payload ?: return

        if (payload.isDraft()) {
            val intent = Intent(this, CreateReviewActivity::class.java)
            intent.putExtra(CreateReviewActivity.EXTRA_LATITUDE, payload.latitude)
            intent.putExtra(CreateReviewActivity.EXTRA_LONGITUDE, payload.longitude)
            draftLatLng = null
            renderMarkers()
            startActivity(intent)
            return
        }

        val spot = payload.spot ?: return
        RecentViewedSpotStore.recordViewedSpot(this, spot.spotId)
        val intent = Intent(this, SpotDetailsActivity::class.java)
        intent.putExtra(SpotDetailsActivity.EXTRA_SPOT_ID, spot.spotId)
        startActivity(intent)
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            return
        }

        map?.isMyLocationEnabled = true
    }

    private fun buildSpotSubtitle(spot: StudySpot, summary: SpotSummary?): String {
        val builder = StringBuilder()
        builder.append(spot.buildingName)
        if (!TextUtils.isEmpty(spot.roomNumber)) {
            builder.append(" | ").append(spot.roomNumber)
        }
        builder.append('\n')
        if (summary == null) {
            builder.append(getString(R.string.map_marker_no_reviews))
        } else {
            builder.append(SpotUiFormatter.formatRatingSummary(summary.averageRating, summary.reviewCount))
        }
        return builder.toString()
    }

    private inner class StudySpotInfoWindowAdapter : GoogleMap.InfoWindowAdapter {
        private val layoutInflater = LayoutInflater.from(this@HomeActivity)

        override fun getInfoWindow(marker: Marker): View? {
            var payload = markerPayloads[marker.id]
            if (payload == null) {
                payload = marker.tag as? MarkerPayload
            }
            payload ?: return null

            if (payload.isDraft()) {
                val draftInfoView = layoutInflater.inflate(R.layout.map_info_window_new_spot, null, false)
                val titleTextView: TextView = draftInfoView.findViewById(R.id.infoTitleTextView)
                val actionTextView: TextView = draftInfoView.findViewById(R.id.infoActionTextView)
                titleTextView.setText(R.string.new_spot_marker_title)
                actionTextView.setTextColor(getColor(android.R.color.white))
                actionTextView.setText(R.string.create_review_button)
                return draftInfoView
            }

            val spot = payload.spot ?: return null
            val existingInfoView = layoutInflater.inflate(R.layout.map_info_window, null, false)
            val titleTextView: TextView = existingInfoView.findViewById(R.id.infoTitleTextView)
            val subtitleTextView: TextView = existingInfoView.findViewById(R.id.infoSubtitleTextView)
            val tagsTextView: TextView = existingInfoView.findViewById(R.id.infoTagsTextView)
            val actionTextView: TextView = existingInfoView.findViewById(R.id.infoActionTextView)
            actionTextView.visibility = View.VISIBLE
            actionTextView.setTextColor(getColor(android.R.color.white))
            titleTextView.text = spot.spotName
            subtitleTextView.text = buildSpotSubtitle(spot, payload.summary)
            if (payload.summary == null || payload.summary.tags.isEmpty()) {
                tagsTextView.setText(R.string.map_marker_no_tags)
            } else {
                tagsTextView.text = SpotUiFormatter.formatTraitList(payload.summary.tags)
            }
            actionTextView.setText(R.string.view_reviews_button)
            return existingInfoView
        }

        override fun getInfoContents(marker: Marker): View? = null
    }

    private class MarkerPayload(
        val spot: StudySpot?,
        val summary: SpotSummary?,
        val latitude: Double?,
        val longitude: Double?
    ) {
        fun isDraft(): Boolean = spot == null

        companion object {
            fun forExistingSpot(spot: StudySpot, summary: SpotSummary?): MarkerPayload {
                return MarkerPayload(spot, summary, null, null)
            }

            fun forDraft(latLng: LatLng): MarkerPayload {
                return MarkerPayload(null, null, latLng.latitude, latLng.longitude)
            }
        }
    }

    private class SpotSummary(
        val averageRating: Double,
        val reviewCount: Int,
        val tags: List<String>
    )
}
