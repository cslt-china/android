package com.google.android.apps.signalong.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private final static String TAG = "DownloadImageTask";

    private DownloadImageCallbacks callbacks;
    public DownloadImageTask(DownloadImageCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap bmp = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            bmp = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return bmp;
    }
    protected void onPostExecute(Bitmap result) {
      callbacks.onImageDownloaded(result);
    }

    public interface DownloadImageCallbacks {
      void onImageDownloaded(Bitmap result);
    }
}
