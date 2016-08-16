package com.bignerdranch.android.photogallerybnrg;

/**
 * Created by smaikap on 15/8/16.
 */
public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(final String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(final String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(final String url) {
        mUrl = url;
    }
}
