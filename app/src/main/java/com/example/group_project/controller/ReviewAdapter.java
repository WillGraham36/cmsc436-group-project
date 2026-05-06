package com.example.group_project.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group_project.R;
import com.example.group_project.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
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
        String ratingDisplay = "Rating: " + currentReview.getStarRating() + "/5";
        holder.ratingText.setText(ratingDisplay);
        holder.descriptionText.setText(currentReview.getDescription());
        holder.timestampText.setText("Just now");
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
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