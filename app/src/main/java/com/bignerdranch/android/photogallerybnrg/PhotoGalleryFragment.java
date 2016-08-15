package com.bignerdranch.android.photogallerybnrg;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by smaikap on 15/8/16.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecycleView;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        new FetchItemTsk().execute();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        this.mPhotoRecycleView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycle_view);
        this.mPhotoRecycleView.setLayoutManager(new GridLayoutManager(this.getActivity(), 3));
        return view;
    }

    private class FetchItemTsk extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                final String result = new FlickrFetcher().getUrlString("https://www.flickr.com/");
                Log.i(TAG, "Fetched content of URL : " + result);
            } catch (IOException e) {
                Log.e(TAG, "Failed to fetch URL : ", e);
            }
            return null;
        }
    }
}
