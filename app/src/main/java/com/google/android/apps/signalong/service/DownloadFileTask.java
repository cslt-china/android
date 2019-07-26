package com.google.android.apps.signalong.service;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.apps.signalong.api.VideoApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * DownloadFileServiceImpl implements download file and save file.
 */
public class DownloadFileTask extends AsyncTask<String, Integer, String> {

    private static final String TAG = "DownloadFileTask";

    private static final int FILE_CHUNCK_SIZE = 4096;

    private final VideoApi videoApi;

    public enum DownloadStatus {
        // Not executing any download task.
        IDLE,
        // Download is finished in failure.
        FAILED,
        // Download is finished successfully.
        SUCCESS,
        // Download is in progress.
        LOADING;
    }

    ;

    private DownloadStatus status;
    private int progress;
    private String errorMessage;

    private DownloadFileCallbacks callbacks;

    public DownloadFileTask(VideoApi videoApi, DownloadFileCallbacks callbacks) {
        this.videoApi = videoApi;
        this.callbacks = callbacks;
    }

    public String toString() {
        return TAG + "[status]" + status + " [progress]" + progress + " [errorMessage]" + errorMessage;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progress = 0;
        errorMessage = "";
        status = DownloadStatus.IDLE;
        Log.i(TAG, this.toString());
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        callbacks.onProgressUpdate(progress[0]);
    }

    @Override
    protected String doInBackground(String... downloadUrlAndOutputFilePath) {
        if (downloadUrlAndOutputFilePath.length != 2) {
            throw new IllegalArgumentException("Parameters must contain two strings, "
                    + "downloadUrl as the first item and outputFilePath as the second!!!");
        }
        downloadFile(downloadUrlAndOutputFilePath[0], downloadUrlAndOutputFilePath[1]);
        return null;
    }

    private void downloadFile(String downloadUrl, String outputFilepath) {
        videoApi
                .downloadFile(downloadUrl)
                .enqueue(
                        new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response == null || response.body() == null) {
                                    callbacks.onDownloadFailure(DownloadStatus.FAILED +
                                            "videoApi response is null or its body is null!!!");
                                    return;
                                }

                                try {
                                    // Prepare the IO streams.
                                    InputStream inputStream = response.body().byteStream();
                                    OutputStream outputStream = new FileOutputStream(new File(outputFilepath));

                                    byte[] fileReader = new byte[FILE_CHUNCK_SIZE];

                                    long downloadedSize = 0;

                                    // Get the total byte size to be downloaded.
                                    long contentLength = response.body().contentLength();

                                    // Initialize the internal fields
                                    progress = 0;

                                    // Start downloading
                                    int read = -1;
                                    while ((read = inputStream.read(fileReader)) != -1) {
                                        outputStream.write(fileReader, 0, read);
                                        downloadedSize += read;
                                        progress = (int) (downloadedSize * 100 / contentLength);
                                        publishProgress(progress);
                                        if (isCancelled())
                                            break;
                                    }

                                    outputStream.flush();
                                    // Close streams.
                                    inputStream.close();
                                    outputStream.close();
                                    callbacks.onDownloadSuccess(outputFilepath);
                                } catch (IOException e) {
                                    status = DownloadStatus.FAILED;
                                    callbacks.onDownloadFailure(e.toString());
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                callbacks.onDownloadFailure(DownloadStatus.FAILED + t.toString());
                            }
                        });
    }

    public interface DownloadFileCallbacks {
        void onDownloadFailure(String errorMessage);

        void onProgressUpdate(int progress);

        void onDownloadSuccess(String downloadedFilePath);
    }
}
