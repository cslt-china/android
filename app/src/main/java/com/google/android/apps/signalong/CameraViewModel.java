package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.db.AppDatabase;
import com.google.android.apps.signalong.db.dao.VideoUploadTaskDao;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.service.UploadVideoThread;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.VideoScreenUtils;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** CameraViewModel implements save video upload task and upload video business logic. */
public class CameraViewModel extends AndroidViewModel {
  private static final String TAG = "CameraViewModel";
  /* The capacity of this deque. */
  private static final int CAPACITY = 20;
  private final VideoUploadTaskDao videoUploadTaskDao;
  private final LinkedBlockingDeque<VideoUploadTask> videoTaskLinkedBlockingDeque;
  private final VideoApi videoApi;
  private final MutableLiveData<Response<SignPromptBatchResponse>> signPromptBatchResponseLiveData;
  private final UploadVideoThread uploadVideoThread;

  public CameraViewModel(@NonNull Application application) {
    super(application);
    videoTaskLinkedBlockingDeque = new LinkedBlockingDeque<>(CAPACITY);
    signPromptBatchResponseLiveData = new MutableLiveData<>();
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
    videoUploadTaskDao = AppDatabase.getDatabase(getApplication()).videoUploadTaskDao();
    uploadVideoThread =
        new UploadVideoThread(
            videoTaskLinkedBlockingDeque,
            videoUploadTask -> new Thread(() -> videoUploadTaskDao.delete(videoUploadTask)).start(),
            LoginSharedPreferences.getAccessToken(getApplication()));
  }

  /**
   * Used to take out video upload task list from the database and then save them to the
   * LinkedBlockingDeque to continue uploading.
   */
  private void takeOutVideoUploadTaskAddBlockingDeque() {
    new Thread(
            () -> {
              List<VideoUploadTask> videoUploadTaskList = videoUploadTaskDao.getAll();

              for (VideoUploadTask videoUploadTask : videoUploadTaskList) {
                try {
                  videoTaskLinkedBlockingDeque.put(videoUploadTask);
                } catch (InterruptedException e) {
                  Log.d(TAG, e.getMessage());
                }
              }
            })
        .start();
  }

  public void startUploadThread() {
    uploadVideoThread.start();
    takeOutVideoUploadTaskAddBlockingDeque();
  }

  public void stopUploadThread() {
    if (uploadVideoThread != null) {
      uploadVideoThread.setExit(true);
    }
  }

  public void saveVideoUploadTask(String videoPath, Integer uuid) {
    new Thread(
            () -> {
              VideoUploadTask videoUploadTaskNew = new VideoUploadTask();
              videoUploadTaskNew.setVideoPath(videoPath);
              videoUploadTaskNew.setId(uuid);
              videoUploadTaskNew.setImagePath(VideoScreenUtils.screenFromVideo(videoPath));
              VideoUploadTask videoUploadTaskOld = videoUploadTaskDao.get(uuid);
              if (videoUploadTaskOld == null) {
                videoUploadTaskDao.insert(videoUploadTaskNew);
              } else {
                videoUploadTaskDao.update(videoUploadTaskNew);
              }
              try {
                videoTaskLinkedBlockingDeque.put(videoUploadTaskNew);
              } catch (InterruptedException e) {
                Log.d(TAG, e.getMessage());
              }
            })
        .start();
  }

  public void getSignPromptBatch() {
    videoApi
        .getSignPromptBatch(LoginSharedPreferences.getAccessToken(getApplication()))
        .enqueue(
            new Callback<SignPromptBatchResponse>() {
              @Override
              public void onResponse(
                  Call<SignPromptBatchResponse> call, Response<SignPromptBatchResponse> response) {
                signPromptBatchResponseLiveData.setValue(response);
              }

              @Override
              public void onFailure(Call<SignPromptBatchResponse> call, Throwable t) {
                signPromptBatchResponseLiveData.setValue(null);
              }
            });
  }

  public MutableLiveData<Response<SignPromptBatchResponse>> getSignPromptBatchResponseLiveData() {
    return signPromptBatchResponseLiveData;
  }
}
