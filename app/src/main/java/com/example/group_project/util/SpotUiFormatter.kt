package com.example.group_project.util

import android.text.TextUtils
import android.text.format.DateUtils
import com.example.group_project.model.Review
import java.util.Locale

object SpotUiFormatter {

    fun formatAverageRating(rating: Double): String {
        return String.format(Locale.US, "%.1f", rating)
    }

    fun formatReviewCount(reviewCount: Int): String {
        return if (reviewCount == 1) "1 review" else "$reviewCount reviews"
    }

    fun formatRatingSummary(rating: Double, reviewCount: Int): String {
        if (reviewCount == 0) {
            return "No reviews yet"
        }
        return "${formatAverageRating(rating)}/5 | ${formatReviewCount(reviewCount)}"
    }

    fun formatRelativeTime(timestamp: Long): String {
        // Some older records may not have a timestamp yet
        if (timestamp <= 0L) {
            return "Recently"
        }
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    fun formatTraitList(traits: Collection<String>?): String {
        if (traits.isNullOrEmpty()) {
            return "No tags yet"
        }
        return TextUtils.join(", ", traits)
    }

    fun collectTopTraits(reviews: List<Review>?, limit: Int): List<String> {
        // Count repeated tags so marker summaries show the most common ones
        if (reviews.isNullOrEmpty() || limit <= 0) {
            return emptyList()
        }

        val frequencies = HashMap<String, Int>()
        for (review in reviews) {
            for (trait in review.traits) {
                if (trait.isBlank()) {
                    continue
                }
                frequencies[trait] = (frequencies[trait] ?: 0) + 1
            }
        }

        return frequencies.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.key }
            )
            .take(limit)
            .map { it.key }
    }

    fun uniqueTraits(reviews: List<Review>?): List<String> {
        // Newer reviews get first chance at ordering the tag list
        if (reviews.isNullOrEmpty()) {
            return emptyList()
        }

        val traits = LinkedHashSet<String>()
        val sorted = reviews.sortedByDescending { it.timestamp }
        for (review in sorted) {
            for (trait in review.traits) {
                if (trait.isNotBlank()) {
                    traits.add(trait)
                }
            }
        }
        return traits.toList()
    }
}
