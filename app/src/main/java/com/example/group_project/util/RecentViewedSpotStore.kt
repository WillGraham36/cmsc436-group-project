package com.example.group_project.util

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

object RecentViewedSpotStore {
    private const val PREFS_NAME = "recent_viewed_spots"
    private const val KEY_SPOT_IDS = "spot_ids"
    private const val DELIMITER = ","
    private const val MAX_RECENT_SPOTS = 10

    fun recordViewedSpot(context: Context?, spotId: String?) {
        if (context == null || TextUtils.isEmpty(spotId)) {
            return
        }

        var spotIds = getRecentSpotIds(context)
        spotIds.removeAll { it == spotId }

        spotIds.add(0, spotId!!)
        if (spotIds.size > MAX_RECENT_SPOTS) {
            spotIds = ArrayList(spotIds.subList(0, MAX_RECENT_SPOTS))
        }

        getPreferences(context)
            .edit()
            .putString(KEY_SPOT_IDS, TextUtils.join(DELIMITER, spotIds))
            .apply()
    }

    fun getRecentSpotIds(context: Context?): MutableList<String> {
        if (context == null) {
            return ArrayList()
        }

        val storedValue = getPreferences(context).getString(KEY_SPOT_IDS, "")
        if (storedValue.isNullOrEmpty()) {
            return ArrayList()
        }

        return storedValue.split(DELIMITER).toMutableList()
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
