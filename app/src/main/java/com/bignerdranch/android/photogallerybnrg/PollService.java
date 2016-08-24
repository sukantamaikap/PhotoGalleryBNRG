package com.bignerdranch.android.photogallerybnrg;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

/**
 * Created by smaikap on 24/8/16.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL = 1000 * 60;

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(final Context context) {
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        final String query = QueryPreferences.getStoedQuery(this);
        final String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if (query == null) {
            items = new FlickrFetcher().fetchRecentPhotos(String.valueOf(PhotoGalleryFragment.PAGE_COUNT));
        } else {
            items = new FlickrFetcher().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        final String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result : " + resultId);
        } else {
            Log.i(TAG, "Got a new result : " + resultId);
        }

        QueryPreferences.setLastResultId(this, resultId);
    }

    private boolean isNetworkAvailableAndConnected() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        final boolean isNetworkConnected = isNetworkAvailable && connectivityManager.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
