package com.example.group_project.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class RecentViewedSpotStore {

    private static final String PREFS_NAME = "recent_viewed_spots";
    private static final String KEY_SPOT_IDS = "spot_ids";
    private static final String DELIMITER = ",";
    private static final int MAX_RECENT_SPOTS = 10;

    private RecentViewedSpotStore() {
    }

    public static void recordViewedSpot(Context context, String spotId) {
        if (context == null || TextUtils.isEmpty(spotId)) {
            return;
        }

        List<String> spotIds = getRecentSpotIds(context);
        Iterator<String> iterator = spotIds.iterator();
        while (iterator.hasNext()) {
            if (spotId.equals(iterator.next())) {
                iterator.remove();
            }
        }

        spotIds.add(0, spotId);
        if (spotIds.size() > MAX_RECENT_SPOTS) {
            spotIds = new ArrayList<>(spotIds.subList(0, MAX_RECENT_SPOTS));
        }

        getPreferences(context)
                .edit()
                .putString(KEY_SPOT_IDS, TextUtils.join(DELIMITER, spotIds))
                .apply();
    }

    public static List<String> getRecentSpotIds(Context context) {
        if (context == null) {
            return new ArrayList<>();
        }

        String storedValue = getPreferences(context).getString(KEY_SPOT_IDS, "");
        if (TextUtils.isEmpty(storedValue)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(storedValue.split(DELIMITER)));
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
