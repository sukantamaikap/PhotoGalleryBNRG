package com.bignerdranch.android.photogallerybnrg;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by smaikap on 28/8/16.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "RECEIVED RESULTS : " + this.getResultCode());

        if (this.getResultCode() != Activity.RESULT_OK) {
            Log.i(TAG, "APP IN FOCUS, NOT NOTIFICATION TO BE DISPLAYED");
            return;
        }

        final int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);
        final Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, notification);
    }
}
