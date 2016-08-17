package com.bignerdranch.android.photogallerybnrg;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smaikap on 15/8/16.
 */
public class GalleryItem {
    @SerializedName("title")
    private String mTitle;

    @SerializedName("id")
    private String mId;

    @SerializedName("url_s")
    private String mUrl_S;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(final String title) {
        mTitle = title;
    }

    public String getId() {
        return mId;
    }

    public void setId(final String id) {
        mId = id;
    }

    public String getUrl_S() {
        return mUrl_S;
    }

    public void setUrl_S(final String url_S) {
        mUrl_S = url_S;
    }
}
