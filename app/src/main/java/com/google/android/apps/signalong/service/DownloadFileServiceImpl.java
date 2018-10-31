package com.google.android.apps.signalong.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.utils.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** DownloadFileServiceImpl implements download file and save file. */
public class DownloadFileServiceImpl implements DownloadFileService {

  private static final String TAG = "DownloadFileServiceImpl";

  private final VideoApi videoApi;

  public DownloadFileServiceImpl(VideoApi videoApi) {
    this.videoApi = videoApi;
  }

  @Override
  public LiveData<DownloadStatus> downloadFile(String downloadUrl, String outputFilepath) {
    MutableLiveData<DownloadStatus> downloadStatusLiveData = new MutableLiveData<>();

    if (FileUtils.isFileExist(outputFilepath)) {
      downloadStatusLiveData.setValue(DownloadStatus.sucessStatus());
    } else {
      downloadStatusLiveData.setValue(DownloadStatus.loadingStatus(0));
      videoApi
          .downloadFile(downloadUrl)
          .enqueue(
              new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                  File videoFile = new File(outputFilepath);
                  InputStream inputStream = null;
                  OutputStream outputStream = null;
                  int percent = 0;
                  try {
                    byte[] fileReader = new byte[4096];
                    long contentLength = response.body().contentLength();
                    long downloadedSize = 0;
                    inputStream = response.body().byteStream();
                    outputStream = new FileOutputStream(videoFile);
                    int read = -1;
                    while ((read = inputStream.read(fileReader)) != -1) {
                      outputStream.write(fileReader, 0, read);
                      //downloadStatusLiveData.
                      downloadedSize += read;
                      percent = (int)(downloadedSize * 100 / contentLength);
                      downloadStatusLiveData.setValue(
                          DownloadStatus.loadingStatus(percent));
                    }
                    outputStream.flush();
                    downloadStatusLiveData.setValue(DownloadStatus.sucessStatus());
                  } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                    downloadStatusLiveData.setValue(
                        DownloadStatus.failedStatus(e.getMessage(), percent));
                  } finally {
                    try {
                      if (inputStream != null) {
                        inputStream.close();
                      }
                      if (outputStream != null) {
                        outputStream.close();
                      }
                    } catch (IOException e) {
                      Log.d(TAG, e.getMessage());
                    }
                  }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                  downloadStatusLiveData.setValue(
                      DownloadStatus.failedStatus(t.getMessage()));
                }
              });
    }
    return downloadStatusLiveData;
  }
}
