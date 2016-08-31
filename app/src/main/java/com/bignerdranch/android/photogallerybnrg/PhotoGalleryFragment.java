package com.bignerdranch.android.photogallerybnrg;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smaikap on 15/8/16.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";
    public static int PAGE_COUNT = 0;
    private List<GalleryItem> mItems = new ArrayList<>();
    private RecyclerView mPhotoRecyclerView;
    private GridLayoutManager mLayoutManager;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private ProgressBar mProgressBar;
    private int mPastVisibleItems, mVisibleItemCount, mTotalItemCount;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
        this.setHasOptionsMenu(Boolean.TRUE);
        this.updateItem();

        PollService.setServiceAlarm(this.getContext(), Boolean.TRUE);

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
                if (PhotoGalleryFragment.this.mPhotoRecyclerView.getVisibility() == View.VISIBLE) {
                    final int numColumns = PhotoGalleryFragment.this.mPhotoRecyclerView.getWidth() / COL_WIDTH;
                    PhotoGalleryFragment.this.mLayoutManager.setSpanCount(numColumns);
                }
            }
        });

        this.mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_progress_bar);
        showProgressBar(Boolean.TRUE);
        this.setupAdapter();
        return view;
    }

    public void showProgressBar(Boolean show) {
        if (show) {
            this.mProgressBar.setVisibility(View.VISIBLE);
            this.mPhotoRecyclerView.setVisibility(View.GONE);
        } else {
            this.mProgressBar.setVisibility(View.GONE);
            this.mPhotoRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                Log.d(TAG, "Query text submitted : " + query);
                QueryPreferences.setStoredQuery(PhotoGalleryFragment.this.getActivity(), query);
                PhotoGalleryFragment.this.hideSoftKeyboard();
                searchView.onActionViewCollapsed();
                PhotoGalleryFragment.this.updateItem();
                PhotoGalleryFragment.PAGE_COUNT = 0;
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                Log.d(TAG, "Query text change : " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String query = QueryPreferences.getStoedQuery(PhotoGalleryFragment.this.getActivity());
                searchView.setQuery(query, false);
            }
        });

        final MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(this.getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear :
                QueryPreferences.setStoredQuery(PhotoGalleryFragment.this.getActivity(), null);
                PhotoGalleryFragment.this.updateItem();
                return true;

            case R.id.menu_item_toggle_polling :
                final boolean shouldStartAlarm = !PollService.isServiceAlarmOn(this.getActivity());
                PollService.setServiceAlarm(this.getActivity(), shouldStartAlarm);
                this.getActivity().invalidateOptionsMenu();
                return true;

            default :
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItem() {
        final String searchQueryParam = QueryPreferences.getStoedQuery(this.getActivity());
        new FetchItemTask(searchQueryParam, PAGE_COUNT++).execute();
    }

    private void setupAdapter() {
        if (this.isAdded()) {
            this.mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        private int pageCount;
        private String mQuery;

        public FetchItemTask(final String query, final int count) {
            this.pageCount = count;
            this.mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            if (this.mQuery == null) {
                return new FlickrFetcher().fetchRecentPhotos(String.valueOf(this.pageCount));
            } else {
                return new FlickrFetcher().searchPhotos(this.mQuery);
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
            PhotoGalleryFragment.this.showProgressBar(Boolean.FALSE);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            this.mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(PhotoHolder.this);
        }

        public void bindDrawable(final Drawable drawable) {
            this.mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(final GalleryItem galleryItem) {
            this.mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View view) {
            final Intent intent = PhotoPageActivity.newIntent(PhotoGalleryFragment.this.getActivity(), this.mGalleryItem.getPhotoPageUri());
            PhotoGalleryFragment.this.startActivity(intent);
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
            holder.bindGalleryItem(galleryItem);
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
            if (dy > 0) {
                PhotoGalleryFragment.this.mVisibleItemCount = PhotoGalleryFragment.this.mLayoutManager.getChildCount();
                PhotoGalleryFragment.this.mTotalItemCount = PhotoGalleryFragment.this.mLayoutManager.getItemCount();
                PhotoGalleryFragment.this.mPastVisibleItems = PhotoGalleryFragment.this.mLayoutManager.findFirstVisibleItemPosition();

                if ( (mVisibleItemCount + mPastVisibleItems) >= mTotalItemCount) {
                    Log.d(TAG, "END OF ITEMS REACHED, TRIGGER FETCH..");
                    PhotoGalleryFragment.this.updateItem();
                }
            }
        }
    }

    private void hideSoftKeyboard() {
        // Check if no view has focus:
        final View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            final InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
