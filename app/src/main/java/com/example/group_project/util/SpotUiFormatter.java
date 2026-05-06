package com.example.group_project.util;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.example.group_project.model.Review;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpotUiFormatter {

    private SpotUiFormatter() {
    }

    public static String formatAverageRating(double rating) {
        return String.format(Locale.US, "%.1f", rating);
    }

    public static String formatReviewCount(int reviewCount) {
        return reviewCount == 1 ? "1 review" : reviewCount + " reviews";
    }

    public static String formatRatingSummary(double rating, int reviewCount) {
        if (reviewCount == 0) {
            return "No reviews yet";
        }
        return formatAverageRating(rating) + "/5 | " + formatReviewCount(reviewCount);
    }

    public static String formatRelativeTime(long timestamp) {
        if (timestamp <= 0L) {
            return "Recently";
        }
        return DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }

    public static String formatTraitList(Collection<String> traits) {
        if (traits == null || traits.isEmpty()) {
            return "No tags yet";
        }
        return TextUtils.join(", ", traits);
    }

    public static List<String> collectTopTraits(List<Review> reviews, int limit) {
        if (reviews == null || reviews.isEmpty() || limit <= 0) {
            return Collections.emptyList();
        }

        Map<String, Integer> frequencies = new HashMap<>();
        for (Review review : reviews) {
            List<String> traits = review.getTraits();
            if (traits == null) {
                continue;
            }
            for (String trait : traits) {
                if (trait == null || trait.trim().isEmpty()) {
                    continue;
                }
                frequencies.put(trait, frequencies.getOrDefault(trait, 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(frequencies.entrySet());
        entries.sort((left, right) -> {
            int compareFrequency = Integer.compare(right.getValue(), left.getValue());
            if (compareFrequency != 0) {
                return compareFrequency;
            }
            return left.getKey().compareToIgnoreCase(right.getKey());
        });

        List<String> topTraits = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : entries) {
            topTraits.add(entry.getKey());
            if (topTraits.size() == limit) {
                break;
            }
        }
        return topTraits;
    }

    public static List<String> uniqueTraits(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> traits = new LinkedHashSet<>();
        List<Review> sorted = new ArrayList<>(reviews);
        sorted.sort(Comparator.comparingLong(Review::getTimestamp).reversed());
        for (Review review : sorted) {
            List<String> reviewTraits = review.getTraits();
            if (reviewTraits == null) {
                continue;
            }
            for (String trait : reviewTraits) {
                if (trait != null && !trait.trim().isEmpty()) {
                    traits.add(trait);
                }
            }
        }
        return new ArrayList<>(traits);
    }
}
