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
import com.google.android.apps.signalong.jsonentities.CreateVideoResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.UploadVideoResponse;
import com.google.android.apps.signalong.jsonentities.VideoId;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.VideoScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** CameraViewModel implements save video upload task and upload video business logic. */
public class CameraViewModel extends AndroidViewModel {
  private static final String TAG = "CameraViewModel";

  /* Internet video file type.*/
  private static final String MEDIA_VIDEO = "video/mp4";
  /* Internet image file type.*/
  private static final String MEDIA_IMAGE = "image/png";
  /* Key name of the uploaded video file.*/
  private static final String KEY_VIDEO = "video";
  /* Key name of the uploaded image file.*/
  private static final String KEY_IMAGE = "thumbnail";

  private final VideoUploadTaskDao videoUploadTaskDao;
  private final VideoApi videoApi;
  private final MutableLiveData<Response<SignPromptBatchResponse>> signPromptBatchResponseLiveData;

  public CameraViewModel(@NonNull Application application) {
    super(application);
    signPromptBatchResponseLiveData = new MutableLiveData<>();
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
    videoUploadTaskDao = AppDatabase.getDatabase(getApplication()).videoUploadTaskDao();
  }

  public void saveVideoUploadTask(String videoPath, Integer uuid) {
    VideoUploadTask videoUploadTaskNew = new VideoUploadTask();
    videoUploadTaskNew.setVideoPath(videoPath);
    videoUploadTaskNew.setId(uuid);
    videoUploadTaskNew.setImagePath(VideoScreenUtils.screenFromVideo(videoPath));

    //access database on the non-UI thread.
    new Thread(()->{
    VideoUploadTask videoUploadTaskOld = videoUploadTaskDao.get(uuid);
    if (videoUploadTaskOld == null) {
      Log.i(TAG, "insert video to Dao");
      videoUploadTaskDao.insert(videoUploadTaskNew);
    } else {
      Log.i(TAG, "update video to Dao");
      videoUploadTaskDao.update(videoUploadTaskNew);
    }
    }).start();
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


  static final int UPLOAD_CONCURRENT = 3;

  private Semaphore semaphore = new Semaphore(UPLOAD_CONCURRENT);
  private volatile boolean threadExitFlag = false;

  /**
   * Used to take out video upload task list from the database and then save them to the
   * LinkedBlockingDeque to continue uploading.
   */
  public void startUploadThread() {
    String uploadTag = "uploadvideo";
    threadExitFlag = false;
    new Thread(
        () -> {
          List<VideoUploadTask> runingList = Collections.synchronizedList(new ArrayList<>());
          while (!threadExitFlag) {
            List<VideoUploadTask> videoUploadTaskList = videoUploadTaskDao.getAll();
            videoUploadTaskList.removeAll(runingList);

            for (VideoUploadTask videoUploadTask : videoUploadTaskList) {

              runingList.add(videoUploadTask);
              Log.i(uploadTag, String.format("enqueue task %d %d %s", videoUploadTask.getId(),
                                             videoUploadTask.hashCode(), videoUploadTask.getVideoPath()));

              try {
                while (!semaphore.tryAcquire(3, TimeUnit.SECONDS)) {
                  if (threadExitFlag) {
                    return;
                  }
                }
              } catch (InterruptedException e) {
                e.printStackTrace();
                return;
              }

              UploadVideoCallback callback = new UploadVideoCallback() {
                @Override
                public void onCreateFailed(VideoUploadTask task, String errorMsg) {
                  Log.i(uploadTag, String.format("create video failed: task %d  %d, error %s",
                                                 task.getId(), task.hashCode(), errorMsg));
                  runingList.remove(task);
                  Log.i(uploadTag, String.format("running task size: %d", videoUploadTaskList.size()));
                  semaphore.release();
                }

                @Override
                public void onUploadFailed(VideoUploadTask task, String videoKey, String errorMsg) {
                  String logInfo = String.format("task %d %d, error %s",
                                                 task.getId(), task.hashCode(), errorMsg);
                  Log.i(uploadTag, "upload video failed: " + logInfo);
                  runingList.remove(task);
                  Log.i(uploadTag, String.format("running task size: %d", videoUploadTaskList.size()));
                  semaphore.release();
                }

                @Override
                public void onSuccess(VideoUploadTask task, String videoKey) {
                  //the callback is scheduled to UI thread. so start a new thread to access db.
                  new Thread(() -> {
                    String logInfo = String.format("task %d %d", task.getId(), task.hashCode());
                    FileUtils.clearFile(task.getVideoPath());
                    FileUtils.clearFile(task.getImagePath());

                    videoUploadTaskDao.delete(task);
                    Log.i(uploadTag, "upload video success: " + logInfo);
                    runingList.remove(task);
                    Log.i(uploadTag, String.format("running task size: %d", videoUploadTaskList.size()));
                    semaphore.release();
                  }).start();
                }
              };

              Log.i(uploadTag, String.format("start task %d %d", videoUploadTask.getId(),
                                             videoUploadTask.hashCode()));

              uploadVideo(videoUploadTask, callback);
            }

            enoughSleep(2000);
          }
        }).start();
  }

  void enoughSleep(int timeToSleep) {
    long start;
    while(timeToSleep > 0) {
      start = System.currentTimeMillis();
      try{
        Thread.sleep(timeToSleep);
        break;
      } catch(InterruptedException e){
        timeToSleep -= System.currentTimeMillis() - start;
      }
    }
  }

  interface UploadVideoCallback {
    void onCreateFailed(VideoUploadTask task, String errorMsg);
    void onUploadFailed(VideoUploadTask task, String videoKey, String errorMsg);
    void onSuccess(VideoUploadTask task, String videoKey);
  }

  private void uploadVideo(VideoUploadTask task, UploadVideoCallback callback) {

    String token = LoginSharedPreferences.getAccessToken(getApplication());

    videoApi.createVideo(token, new VideoId(task.getId())).enqueue(
        new Callback<CreateVideoResponse>() {
          @Override
          public void onResponse(
              Call<CreateVideoResponse> call, Response<CreateVideoResponse> response) {
            if (response.isSuccessful()) {
              String videoKey = response.body().getData().getUploadKey();
              Log.i(TAG, String.format("create video success: task %d - video key %s",
                                       task.getId(),
                                       task.getVideoPath(),
                                       videoKey));

              videoApi.updateVideo(token,
                                   videoKey,
                                   prepareFilePart(KEY_VIDEO, task.getVideoPath(), MEDIA_VIDEO),
                                   prepareFilePart(KEY_IMAGE, task.getImagePath(), MEDIA_IMAGE))
                      .enqueue(new Callback<UploadVideoResponse>() {
                        @Override
                        public void onResponse(Call<UploadVideoResponse> call,
                                               Response<UploadVideoResponse> response) {

                          if (response.isSuccessful()) {
                            callback.onSuccess(task, videoKey);
                          } else {
                            //TODO:
                            callback.onUploadFailed(task, videoKey, "server error");
                          }
                        }

                        @Override
                        public void onFailure(Call<UploadVideoResponse> call, Throwable t) {
                          callback.onUploadFailed(task, videoKey, t.getMessage());
                        }
                      });
            }
          }

          @Override
          public void onFailure(Call<CreateVideoResponse> call, Throwable t) {
            callback.onCreateFailed(task, t.getMessage());
          }
        });
  }

  public void stopUploadThread() {
    threadExitFlag = true;
  }

  private MultipartBody.Part prepareFilePart(String partName, String filePath, String mediaType) {
    File file = new File(filePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse(mediaType), file);
    return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
  }

}
