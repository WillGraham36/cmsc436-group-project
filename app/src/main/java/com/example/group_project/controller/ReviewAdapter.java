package com.example.group_project.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_project.R;
import com.example.group_project.model.Review;
import com.example.group_project.util.SpotUiFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList == null ? new ArrayList<>() : reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review currentReview = reviewList.get(position);
        holder.spotNameText.setText(currentReview.getSpotName());
        holder.ratingText.setText(String.format(
                Locale.US,
                "Rating: %.1f/5",
                currentReview.getStarRating()
        ));
        holder.descriptionText.setText(currentReview.getDescription());
        holder.timestampText.setText(SpotUiFormatter.formatRelativeTime(currentReview.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void setReviews(List<Review> reviews) {
        reviewList.clear();
        if (reviews != null) {
            reviewList.addAll(reviews);
        }
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView spotNameText;
        TextView ratingText;
        TextView timestampText;
        TextView descriptionText;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            spotNameText = itemView.findViewById(R.id.textSpotName);
            ratingText = itemView.findViewById(R.id.textRatingSummary);
            timestampText = itemView.findViewById(R.id.textTimestamp);
            descriptionText = itemView.findViewById(R.id.textReviewBody);
        }
    }
}
