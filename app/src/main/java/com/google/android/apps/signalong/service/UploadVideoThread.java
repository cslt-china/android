package com.google.android.apps.signalong.service;

import android.util.Log;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;
import com.google.android.apps.signalong.jsonentities.CreateVideoResponse;
import com.google.android.apps.signalong.jsonentities.UploadVideoResponse;
import com.google.android.apps.signalong.jsonentities.VideoId;
import com.google.android.apps.signalong.utils.FileUtils;
import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** UploadVideoThread used to build threads to run the upload background. */
public class UploadVideoThread extends Thread {
  private static final String TAG = "UploadVideoThread";
  /* Internet video file type.*/
  private static final String MEDIA_VIDEO = "video/mp4";
  /* Internet image file type.*/
  private static final String MEDIA_IMAGE = "image/png";
  /* Key name of the uploaded video file.*/
  private static final String KEY_VIDEO = "video";
  /* Key name of the uploaded image file.*/
  private static final String KEY_IMAGE = "thumbnail";
  /* Take out the wait time for LinkedBlockingDeque method poll(),
   * in order to be able to detect if the thread wants to exit.
   */
  private static final int TAKE_OUT_WAIT_TIME_SECONDS = 5;
  private final VideoApi videoApi;
  private final DeleteCallBack deleteCallBack;
  private final LinkedBlockingDeque<VideoUploadTask> videoUploadTaskLinkedBlockingDeque;
  private final String token;
  private boolean isExit;
  private VideoUploadTask firstVideoUploadTask;
  private VideoUploadTask secondVideoUploadTask;

  /** Call onDelete method when the upload is successful. */
  public interface DeleteCallBack {
    void onDelete(VideoUploadTask videoUploadTask);
  }

  public UploadVideoThread(
      LinkedBlockingDeque<VideoUploadTask> videoUploadTaskLinkedBlockingDeque,
      DeleteCallBack deleteCallBack,
      String token) {
    this.videoUploadTaskLinkedBlockingDeque = videoUploadTaskLinkedBlockingDeque;
    this.deleteCallBack = deleteCallBack;
    this.token = token;
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
  }

  @Override
  public void run() {
    isExit = false;
    try {
      while (!isExit) {
        firstVideoUploadTask =
            videoUploadTaskLinkedBlockingDeque.poll(TAKE_OUT_WAIT_TIME_SECONDS, TimeUnit.SECONDS);
        if (firstVideoUploadTask != null) {
          secondVideoUploadTask = firstVideoUploadTask;
          videoApi
              .createVideo(token, new VideoId(secondVideoUploadTask.getId()))
              .enqueue(
                  new Callback<CreateVideoResponse>() {
                    @Override
                    public void onResponse(
                        Call<CreateVideoResponse> call, Response<CreateVideoResponse> response) {
                      if (response.isSuccessful()) {
                        videoApi
                            .updateVideo(
                                token,
                                response.body().getData().getUploadKey(),
                                prepareFilePart(
                                    KEY_VIDEO, secondVideoUploadTask.getVideoPath(), MEDIA_VIDEO),
                                prepareFilePart(
                                    KEY_IMAGE, secondVideoUploadTask.getImagePath(), MEDIA_IMAGE))
                            .enqueue(
                                new Callback<UploadVideoResponse>() {
                                  @Override
                                  public void onResponse(
                                      Call<UploadVideoResponse> call,
                                      Response<UploadVideoResponse> response) {
                                    if (response.isSuccessful()) {
                                      FileUtils.clearFile(secondVideoUploadTask.getVideoPath());
                                      FileUtils.clearFile(secondVideoUploadTask.getImagePath());
                                      deleteCallBack.onDelete(secondVideoUploadTask);
                                    }
                                  }

                                  @Override
                                  public void onFailure(
                                      Call<UploadVideoResponse> call, Throwable t) {
                                    isExit = true;
                                  }
                                });
                      }
                    }

                    @Override
                    public void onFailure(Call<CreateVideoResponse> call, Throwable t) {
                      isExit = true;
                    }
                  });
        }
      }
    } catch (InterruptedException e) {
      Log.d(TAG, e.getMessage());
    }
  }

  private MultipartBody.Part prepareFilePart(String partName, String filePath, String mediaType) {
    File file = new File(filePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse(mediaType), file);
    return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
  }

  public void setExit(boolean exit) {
    isExit = exit;
  }
}
