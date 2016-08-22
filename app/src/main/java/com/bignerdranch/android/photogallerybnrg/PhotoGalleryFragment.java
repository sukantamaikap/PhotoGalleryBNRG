package com.bignerdranch.android.photogallerybnrg;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

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
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private int mPastVisibleItems, mVisibleItemCount, mTotalItemCount;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        new FetchItemTask(PAGE_COUNT++).execute();

        final Handler responseHandler = new Handler();
        this.mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        this.mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {

            @Override
            public void onThumbnailDownloaded(final PhotoHolder target, final Bitmap thumbnail) {
                final Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });

        this.mThumbnailDownloader.start();
        this.mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
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
            final String queryString = "dog";
            if (queryString == null) {
                return new FlickrFetcher().fetchRecentPhotos(String.valueOf(this.pageCount));
            } else {
                return new FlickrFetcher().searchPhotos(queryString);
            }
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
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            this.mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(final Drawable drawable) {
            this.mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(final List<GalleryItem> items) {
            this.mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = LayoutInflater.from(PhotoGalleryFragment.this.getActivity());
            final View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(final PhotoHolder holder, final int position) {
            final Drawable placeHolder = PhotoGalleryFragment.this.getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeHolder);

            final GalleryItem galleryItem = this.mGalleryItems.get(position);
            PhotoGalleryFragment.this.mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl_S());
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
                PhotoGalleryFragment.this.mTotalItemCount = PhotoGalleryFragment.this.mLayoutManager.getItemCount();
                PhotoGalleryFragment.this.mPastVisibleItems = PhotoGalleryFragment.this.mLayoutManager.findFirstVisibleItemPosition();

                if ( (mVisibleItemCount + mPastVisibleItems) >= mTotalItemCount) {
                    Log.d(TAG, "END OF ITEMS REACHED, TRIGGER FETCH..");
                    new FetchItemTask(PAGE_COUNT++).execute();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mThumbnailDownloader.quit();
        Log.i(TAG, "BACKGROUND THREAD DESTROYED");
    }
}
