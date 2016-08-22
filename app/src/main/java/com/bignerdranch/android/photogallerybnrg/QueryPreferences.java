package com.bignerdranch.android.photogallerybnrg;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by smaikap on 22/8/16.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoedQuery(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(final Context context, final String query) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_SEARCH_QUERY, query).apply();
    }
}
