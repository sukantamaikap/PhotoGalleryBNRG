package com.bignerdranch.android.photogallerybnrg;

import android.app.Activity;
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
import android.util.Log;

import java.util.List;

/**
 * Created by smaikap on 24/8/16.
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
    public static final String ACTION_SHOW_NOTIFICATION = "com.bignardranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PREM_PRIVATE = "com.bignardranch.android.photogallery.PRIVATE";
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String REQUEST_CODE = "REQUEST_CODE";

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(final Context context) {
        return new Intent(context, PollService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            Log.d(TAG, "NO NETWORK FOUND, WILL NOT EXECUTE #onHandleIntent");
            return;
        }

        final String query = QueryPreferences.getStoedQuery(this);
        final String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if (query == null) {
            items = new FlickrFetcher().fetchRecentPhotos(String.valueOf(1));
        } else {
            items = new FlickrFetcher().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        final String resultId = items.get(0).getId();
        if (!resultId.equals(lastResultId)) {
            Log.d(TAG, "GOT A NEW RESULT : " + resultId);
            Log.d(TAG, "BUILD A NEW NOTIFICATION");
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

            this.showBackgroundNotification(0, notification);
        }

        QueryPreferences.setLastResultId(this, resultId);
    }

    private void showBackgroundNotification(final int requestCode, final Notification notification) {
        final Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(PollService.REQUEST_CODE, requestCode);
        intent.putExtra(PollService.NOTIFICATION, notification);
        this.sendOrderedBroadcast(intent, PollService.PREM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    public static void setServiceAlarm(final Context context, final boolean isOn) {
        final Intent intent = PollService.newIntent(context);
        //this call packages up the call to Activity.startService(intent)
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    POLL_INTERVAL,
                    pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        QueryPreferences.setAlarmOn(context, isOn);
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
