package com.bignerdranch.android.photogallerybnrg;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by smaikap on 24/8/16.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;

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
            Log.d(TAG, "Got an old result : " + resultId);
        } else {
            Log.d(TAG, "Got a new result : " + resultId);
            Log.d(TAG, "Build a new notification");
            final Resources resources = this.getResources();
            final Intent photoGalleryIntent = PhotoGalleryActivity.newIntent(this);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, photoGalleryIntent, 0);
            final Notification notification = new NotificationCompat
                    .Builder(this)
                    .setTicker(resources.getString(R.string.new_picture_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_picture_title))
                    .setContentTitle(resources.getString(R.string.new_picture_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(Boolean.TRUE)
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, notification);
        }

        QueryPreferences.setLastResultId(this, resultId);
    }

    public static void setServiceAlarm(final Context context, final boolean isOn) {
        final Intent intent = PollService.newIntent(context);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            Log.d(TAG, "STARTING THE ALARM");
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL, pendingIntent);
        } else {
            Log.d(TAG, "STOPPING THE ALARM");
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static boolean isServiceAlarmOn(final Context context) {
        final Intent intent = PollService.newIntent(context);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    private boolean isNetworkAvailableAndConnected() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean isNetworkAvailable = connectivityManager.getActiveNetworkInfo() != null;
        final boolean isNetworkConnected = isNetworkAvailable && connectivityManager.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
