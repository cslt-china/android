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
import com.google.android.apps.signalong.utils.TimerUtils;
import com.google.android.apps.signalong.utils.VideoScreenUtils;

import org.checkerframework.checker.nullness.NullnessUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CameraViewModel implements save video upload task and upload video business logic.
 */
public class CameraViewModel extends AndroidViewModel {
    private static final String TAG = "CameraViewModel";

    private static final int PROMPT_BATCH_LIMIT = 100;
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
    private static final int UPLOAD_CONCURRENT = 2;

    private Semaphore semaphore = new Semaphore(UPLOAD_CONCURRENT);
    private volatile boolean threadExitFlag = true;

    public CameraViewModel(@NonNull Application application) {
        super(application);
        videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
        videoUploadTaskDao = AppDatabase.getDatabase(getApplication()).videoUploadTaskDao();
    }

    /**
     * 将上传任务保存到sqlite中等待上传，上传成功后删掉该任务，否则继续等待上传
     */
    public void saveVideoUploadTask(String videoPath, Integer uuid) {
        assert (videoPath != null);

        //access database and get a frame from video on the non-UI thread.
        new Thread(() -> {
            VideoUploadTask task = new VideoUploadTask();
            task.setVideoPath(videoPath);
            task.setId(uuid);
            //VideoScreenUtils.screenFromVideo is a long time task, so move it to new thread
            task.setImagePath(VideoScreenUtils.screenFromVideo(videoPath));
            VideoUploadTask videoUploadTaskOld = videoUploadTaskDao.get(uuid);
            if (videoUploadTaskOld == null) {
                Log.i(TAG, "insert video to Dao: " + task);
                videoUploadTaskDao.insert(task);
            } else {
                Log.i(TAG, "update video to Dao: " + task);
                videoUploadTaskDao.update(task);
            }
        }).start();
    }

    public void getSignPromptBatch(SignPromptBatchResponseCallbacks callback) {
        videoApi
                .getSignPromptBatch(LoginSharedPreferences.getAccessToken(getApplication()),
                        PROMPT_BATCH_LIMIT)
                .enqueue(
                        new Callback<SignPromptBatchResponse>() {
                            @Override
                            public void onResponse(
                                    Call<SignPromptBatchResponse> call, Response<SignPromptBatchResponse> response) {
                                callback.onSuccessSignPromptBatchResponse(response);
                            }

                            @Override
                            public void onFailure(Call<SignPromptBatchResponse> call, Throwable t) {
                                callback.onFailureResponse(t);
                            }
                        });
    }

    private boolean checkTaskFileValid(VideoUploadTask task, String name, String path) {
        if (path == null) {
            Log.i(TAG, String.format("unvalid %s, no %s path", task, name));
            return false;
        }

        File f = new File(path);
        if (!f.exists()) {
            Log.i(TAG, String.format("unvalid %s, %s path not exist %s", task, name, path));
            return false;
        }

        return true;
    }

    private boolean checkTaskValid(VideoUploadTask task) {
        return checkTaskFileValid(task, "video", task.getVideoPath())
                && checkTaskFileValid(task, "image", task.getImagePath());
    }

    public interface OnCheckResult {
        void onResult(List<VideoUploadTask> tasks);
    }

    public void checkUnfinishedTask(OnCheckResult onCheckResult) {
        new Thread(() -> {
            List<VideoUploadTask> videoUploadTaskList = videoUploadTaskDao.getAll();
            onCheckResult.onResult(videoUploadTaskList);
        }).start();
    }

    public void clearAllTask(Runnable callback) {
        new Thread(() -> {
            stopUploadThread();
            //TODO: when uploaded, remove a task not exists. try catch...
            //可以上传的时候马上 clearAllTask, 查看结果
            videoUploadTaskDao.clear();
            callback.run();
        }).start();
    }

    /**
     * Used to take out video upload task list from the database and then save them to the
     * LinkedBlockingDeque to continue uploading.
     */
    public void startUploadThread() {
        if (threadExitFlag == false) {
            return;
        }

        Log.i(TAG, "start uploading thread");

        threadExitFlag = false;
        new Thread(() -> {
            List<VideoUploadTask> runingList = Collections.synchronizedList(new ArrayList<>());
            while (!threadExitFlag) {

                List<VideoUploadTask> videoUploadTaskList = videoUploadTaskDao.getAll();
                videoUploadTaskList.removeAll(runingList);

                for (VideoUploadTask videoUploadTask : videoUploadTaskList) {
                    if (!checkTaskValid(videoUploadTask)) {
                        Log.i(TAG, String.format("remove unvalid %s", videoUploadTask));
                        new Thread(() -> videoUploadTaskDao.delete(videoUploadTask)).start();
                        continue;
                    }

                    runingList.add(videoUploadTask);
                    Log.i(TAG, String.format("enqueue task %s", videoUploadTask));

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

                    Log.i(TAG, String.format("acquired samaphore,  now availablePermits is %d",
                            semaphore.availablePermits()));

                    UploadVideoCallback callback = new UploadVideoCallback() {
                        @Override
                        public void onCreateFailed(VideoUploadTask task, String errorMsg) {
                            Log.i(TAG, String.format("create video failed: %s, error %s", task, errorMsg));

                            runingList.remove(task);
                            Log.i(TAG, String.format("running task size: %d", videoUploadTaskList.size()));
                            semaphore.release();
                        }

                        @Override
                        public void onUploadFailed(VideoUploadTask task, String videoKey, String errorMsg) {
                            Log.i(TAG, String.format("upload video failed: %s, error %s ", task, errorMsg));

                            runingList.remove(task);
                            Log.i(TAG, String.format("running task size: %d", videoUploadTaskList.size()));
                            semaphore.release();
                        }

                        @Override
                        public void onSuccess(VideoUploadTask task, String videoKey) {
                            //the callback is scheduled to UI thread. so start a new thread to access db.
                            new Thread(() -> {
                                FileUtils.clearFile(task.getVideoPath());
                                FileUtils.clearFile(task.getImagePath());

                                videoUploadTaskDao.delete(task);
                                Log.i(TAG, String.format("upload video success: %s", task));

                                runingList.remove(task);
                                Log.i(TAG, String.format("running task size: %d", videoUploadTaskList.size()));
                                semaphore.release();
                            }).start();
                        }
                    };

                    Log.i(TAG, String.format("start task %s", videoUploadTask));
                    uploadVideoStepOne(videoUploadTask, callback);
                }

                TimerUtils.enoughSleep(2000);
            }
        }).start();
    }

    interface UploadVideoCallback {
        void onCreateFailed(VideoUploadTask task, String errorMsg);

        void onUploadFailed(VideoUploadTask task, String videoKey, String errorMsg);

        void onSuccess(VideoUploadTask task, String videoKey);
    }

    /**
     * 视频上传第一步：
     *        判断上传任务是否已经创建了生成key,若已创建，则通过该key进行视频上传第2步；
     *        否则，调用生成上传key接口创建上传key，然后将key保存如sqlite中该上传任务中，之后进行视频上传第2步；
     */
    private void uploadVideoStepOne(VideoUploadTask task, UploadVideoCallback callback) {
        String token = LoginSharedPreferences.getAccessToken(getApplication());
        if (task.getUploadKey() != null) {
            uploadVideoStepTwo(task,callback);
        } else {
            videoApi.createVideo(token, new VideoId(task.getId())).enqueue(
                    new Callback<CreateVideoResponse>() {
                        @Override
                        public void onResponse(Call<CreateVideoResponse> call, Response<CreateVideoResponse> response) {
                            if (response.isSuccessful()) {
                                String videoKey = response.body().getData().getUploadKey();
                                Log.i(TAG, String.format("create video success: %s - video key %s", task, task.getVideoPath(), videoKey));
                                task.setUploadKey(videoKey);
                                new Thread(() -> videoUploadTaskDao.update(task)).start();
                                uploadVideoStepTwo(task,callback);

                            }
                        }

                        @Override
                        public void onFailure(Call<CreateVideoResponse> call, Throwable t) {
                            callback.onCreateFailed(task, t.getMessage());
                        }
                    });
        }

    }

    /**
     * 视频上传第二步：调用视频上传接口进行上传服务器操作
     */
    private void uploadVideoStepTwo(VideoUploadTask task, UploadVideoCallback callback){
        String token = LoginSharedPreferences.getAccessToken(getApplication());
        videoApi.updateVideo(token,
                task.getUploadKey(),
                prepareFilePart(KEY_VIDEO, task.getVideoPath(), MEDIA_VIDEO),
                prepareFilePart(KEY_IMAGE, task.getImagePath(), MEDIA_IMAGE))
                .enqueue(new Callback<UploadVideoResponse>() {
                    @Override
                    public void onResponse(Call<UploadVideoResponse> call, Response<UploadVideoResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getCode() == 0) {
                                callback.onSuccess(task, task.getUploadKey());
                            } else {
                                callback.onUploadFailed(task, task.getUploadKey(), response.body().getMessage());
                            }
                        } else {
                            callback.onUploadFailed(task, task.getUploadKey(), response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadVideoResponse> call, Throwable t) {
                        callback.onUploadFailed(task, task.getUploadKey(), t.getMessage());
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

    public interface SignPromptBatchResponseCallbacks {
        void onSuccessSignPromptBatchResponse(Response<SignPromptBatchResponse> response);

        void onFailureResponse(Throwable t);
    }
}
