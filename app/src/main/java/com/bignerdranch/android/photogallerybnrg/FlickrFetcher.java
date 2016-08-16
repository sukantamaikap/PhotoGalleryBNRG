package com.bignerdranch.android.photogallerybnrg;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smaikap on 15/8/16.
 */
public class FlickrFetcher {
    private static final String TAG = "FlickrFetcher";
    private static final String API_KEY = "96de86408a603386edbb592ce40f033e";
    private static final String API_SECRET = "2aaa42562c8e2883";

    public byte[] getUrlBytes(final String urlSpec) throws IOException {
        final URL url = new URL(urlSpec);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        ByteArrayOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " : with " + urlSpec);
            }

            int byteRead = 0;
            final byte[] buffer = new byte[1024];
            while ((byteRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, byteRead);
            }
            return outputStream.toByteArray();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public String getUrlString(final String urlSpec) throws IOException {
        return new String(this.getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItem() {
        final List<GalleryItem> items = new ArrayList<>();
        try {
            final String uri = Uri.parse("https://api.flickr.com/services/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build()
                    .toString();
            final String jsonString = this.getUrlString(uri);
            Log.i(TAG, "Received JSON : " + jsonString);
            final JSONObject jsonBody = new JSONObject(jsonString);
            this.parseItems(items, jsonBody);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse jSon", e);
        }
        return items;
    }

    private void parseItems(final List<GalleryItem> items, final JSONObject jsonBody) throws JSONException {
        final JSONObject photoJSonObject = jsonBody.getJSONObject("photos");
        final JSONArray photoJsonArray = photoJSonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            final JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            final GalleryItem galleryItem = new GalleryItem();
            galleryItem.setId(photoJsonObject.getString("id"));
            galleryItem.setCaption(photoJsonObject.getString("title"));

            if (photoJSonObject.has("url_s")) {
                galleryItem.setUrl(photoJsonObject.getString("url_s"));
            }

            items.add(galleryItem);
        }
    }
}
