package com.bignerdranch.android.photogallerybnrg;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smaikap on 15/8/16.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
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
        this.setupAdapter();
        return view;
    }

    private void setupAdapter() {
        if (this.isAdded()) {
            this.mPhotoRecycleView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemTsk extends AsyncTask<Void, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            return new FlickrFetcher().fetchItem();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            PhotoGalleryFragment.this.mItems = items;
            PhotoGalleryFragment.this.setupAdapter();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            this.mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(final GalleryItem item) {
            this.mTitleTextView.setText(item.getCaption());
        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(final List<GalleryItem> items) {
            this.mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final TextView textView = new TextView(PhotoGalleryFragment.this.getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            final GalleryItem galleryItem = this.mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return this.mGalleryItems.size();
        }
    }
}
