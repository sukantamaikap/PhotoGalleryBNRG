package com.bignerdranch.android.photogallerybnrg;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by smaikap on 29/8/16.
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    public static Intent newIntent(final Context context, final Uri photoPageUri) {
        final Intent intent = new Intent(context, PhotoPageActivity.class);
        intent.setData(photoPageUri);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(this.getIntent().getData());
    }
}
