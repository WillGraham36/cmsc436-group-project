package com.example.group_project.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_project.R
import com.example.group_project.model.Review
import com.example.group_project.util.SpotUiFormatter
import java.util.Locale

class ReviewAdapter(reviewList: List<Review>?) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val reviewList: MutableList<Review> = reviewList?.toMutableList() ?: ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val currentReview = reviewList[position]
        holder.spotNameText.text = currentReview.spotName
        // Keep rating display consistent across all review lists
        holder.ratingText.text = String.format(
            Locale.US,
            "Rating: %.1f/5",
            currentReview.starRating
        )
        holder.descriptionText.text = currentReview.description
        holder.timestampText.text = SpotUiFormatter.formatRelativeTime(currentReview.timestamp)
    }

    override fun getItemCount(): Int = reviewList.size

    fun setReviews(reviews: List<Review>?) {
        // Simple full refresh is fine for these small class-project lists
        reviewList.clear()
        if (reviews != null) {
            reviewList.addAll(reviews)
        }
        notifyDataSetChanged()
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val spotNameText: TextView = itemView.findViewById(R.id.textSpotName)
        val ratingText: TextView = itemView.findViewById(R.id.textRatingSummary)
        val timestampText: TextView = itemView.findViewById(R.id.textTimestamp)
        val descriptionText: TextView = itemView.findViewById(R.id.textReviewBody)
    }
}
