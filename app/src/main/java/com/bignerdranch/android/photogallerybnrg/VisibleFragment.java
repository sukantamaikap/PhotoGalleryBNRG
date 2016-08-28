package com.bignerdranch.android.photogallerybnrg;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by smaikap on 27/8/16.
 */
public class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "CANCELLING NOTIFICATION");
            this.setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        this.getActivity().registerReceiver(this.mBroadcastReceiver, filter, PollService.PREM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.getActivity().unregisterReceiver(this.mBroadcastReceiver);
    }
}
