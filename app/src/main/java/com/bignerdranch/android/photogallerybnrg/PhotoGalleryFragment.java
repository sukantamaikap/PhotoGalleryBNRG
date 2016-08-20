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
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smaikap on 15/8/16.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private static int PAGE_COUNT = 0;
    private List<GalleryItem> mItems = new ArrayList<>();
    private RecyclerView mPhotoRecyclerView;
    private GridLayoutManager mLayoutManager;
    private int mPastVisibleItems, mVisibleItemCount, mTotalItemCount;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        new FetchItemTask(PAGE_COUNT++).execute();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        this.mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycle_view);
        this.mLayoutManager = new GridLayoutManager(this.getActivity(), 3);
        this.mPhotoRecyclerView.setLayoutManager(mLayoutManager);
        this.mPhotoRecyclerView.addOnScrollListener(new PhotoGalleryOnScrollListener());
        this.mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private static final int COL_WIDTH = 300;

            @Override
            public void onGlobalLayout() {
                final int numColumns = PhotoGalleryFragment.this.mPhotoRecyclerView.getWidth() / COL_WIDTH;
                PhotoGalleryFragment.this.mLayoutManager.setSpanCount(numColumns);
            }
        });
        this.setupAdapter();
        return view;
    }

    private void setupAdapter() {
        if (this.isAdded()) {
            this.mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        private int pageCount;

        public FetchItemTask(final int count) {
            this.pageCount = count;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            return new FlickrFetcher().fetchItem(this.pageCount);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if (pageCount > 0) {
                Log.i(TAG, "NEW PAGE, ADD TO THE EXISTING LIST OF ITEMS");
                PhotoGalleryFragment.this.mItems.addAll(items);
                PhotoGalleryFragment.this.mPhotoRecyclerView.getAdapter().notifyDataSetChanged();

            } else {
                PhotoGalleryFragment.this.mItems = items;
                PhotoGalleryFragment.this.setupAdapter();
            }
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);
            this.mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(final GalleryItem item) {
            this.mTitleTextView.setText(item.getTitle());
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

    private class PhotoGalleryOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState){
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(final RecyclerView recyclerView,
                               final int dx,
                               final int dy){
            super.onScrolled(recyclerView, dx, dy);
            Log.d(TAG, "SCROLL dx : " + dx);
            Log.d(TAG, "SCROLL dy : " + dy);
            if (dy > 0) {
                PhotoGalleryFragment.this.mVisibleItemCount = PhotoGalleryFragment.this.mLayoutManager.getChildCount();
                Log.d(TAG, "visibleItemCount -> " + PhotoGalleryFragment.this.mVisibleItemCount);

                PhotoGalleryFragment.this.mTotalItemCount = PhotoGalleryFragment.this.mLayoutManager.getItemCount();
                Log.d(TAG, "totalItemCount -> " + PhotoGalleryFragment.this.mTotalItemCount);

                PhotoGalleryFragment.this.mPastVisibleItems = PhotoGalleryFragment.this.mLayoutManager.findFirstVisibleItemPosition();
                Log.d(TAG, "pastVisibleItems -> " + PhotoGalleryFragment.this.mPastVisibleItems);

                if ( (mVisibleItemCount + mPastVisibleItems) >= mTotalItemCount) {
                    Log.d(TAG, "END OF ITEMS REACHED, TRIGGER FETCH..");
                    new FetchItemTask(PAGE_COUNT++).execute();
                }
            }
        }
    }
}
