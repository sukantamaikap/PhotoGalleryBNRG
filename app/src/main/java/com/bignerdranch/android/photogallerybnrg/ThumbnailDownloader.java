package com.bignerdranch.android.photogallerybnrg;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by smaikap on 20/8/16.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private final ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public ThumbnailDownloader(final Handler handler) {
        super(TAG);
        this.mResponseHandler = handler;
    }

    @Override
    protected void onLooperPrepared() {
        this.mRequestHandler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    final T target = (T) msg.obj;
                    Log.i(TAG, "GOT REQUEST URL : " + ThumbnailDownloader.this.mRequestMap.get(target));
                    ThumbnailDownloader.this.handRequest(target);
                }
            }
        };
    }

    public void queueThumbnail(final T target, final String url) {
        Log.i(TAG, "GET CONTENT FROM : " + url);

        if (url == null) {
            this.mRequestMap.remove(target);
        } else {
            this.mRequestMap.put(target, url);
            this.mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    private void handRequest(final T target) {
        try {
            final String url = this.mRequestMap.get(target);
            if (url == null) {
                return;
            }

            final byte[] bitmapByte  = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
            Log.i(TAG, "BITMAP CREATED");

            this.mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (ThumbnailDownloader.this.mRequestMap.get(target) != url) {
                        return;
                    }

                    ThumbnailDownloader.this.mRequestMap.remove(target);
                    ThumbnailDownloader.this.mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "ERROR DOWNLOADING IMAGE : ", e);
        }
    }

    public void clearQueue() {
        this.mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(final T target, final Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(final ThumbnailDownloadListener<T> listener) {
        this.mThumbnailDownloadListener = listener;
    }
}
