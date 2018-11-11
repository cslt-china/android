package com.google.android.apps.signalong.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.apps.signalong.api.VideoApi;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadImageTask extends AsyncTask<String, Void, String> {

    private final static String TAG = "DownloadImageTask";

    private final VideoApi videoApi;

    private DownloadImageCallbacks callbacks;
    public DownloadImageTask(VideoApi videoApi, DownloadImageCallbacks callbacks) {
        this.videoApi = videoApi;
        this.callbacks = callbacks;
    }

    protected String doInBackground(String... urls) {
      String urldisplay = urls[0];
      videoApi
        .downloadFile(urldisplay)
        .enqueue(
          new Callback<ResponseBody>() {
              @Override
              public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                  if (response == null || response.body() == null) {
                      callbacks.onDownloadFailure(DownloadFileTask.DownloadStatus.FAILED +
                        "videoApi response is null or its body is null!!!");
                      return;
                  }
                  Bitmap bmp = null;
                  try {
                      InputStream inputStream = response.body().byteStream();
                      bmp = BitmapFactory.decodeStream(inputStream);
                      inputStream.close();
                      callbacks.onImageDownloaded(bmp);
                  } catch (Exception e) {
                      callbacks.onDownloadFailure(e.toString());
                  }
              }

              @Override
              public void onFailure(Call<ResponseBody> call, Throwable t) {
                  callbacks.onDownloadFailure(DownloadFileTask.DownloadStatus.FAILED + t.toString());
              }
          });
      return null;
    }

    public interface DownloadImageCallbacks {
      void onImageDownloaded(Bitmap result);
      void onDownloadFailure(String error);
    }
}
