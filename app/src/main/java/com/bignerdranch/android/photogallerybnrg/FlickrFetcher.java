package com.bignerdranch.android.photogallerybnrg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by smaikap on 15/8/16.
 */
public class FlickrFetcher {

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
}
