package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.VideoStatus;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MyVideoViewModel implements get a batch of rejected video and pending approval video and approved
 * video logic.
 */
public class MyVideoViewModel extends AndroidViewModel {
  private static final String TAG = "MyVideoViewModel";

  private static final int LIST_SIZE = 100;
  private final VideoApi videoApi;

  public MyVideoViewModel(@NonNull Application application) {
    super(application);
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
  }

  public void getPersonalVideoList(
      VideoStatus videoStatus, PersonalVideoListResponseCallbacks callback) {
    videoApi
        .getPersonalVideoList(
            LoginSharedPreferences.getAccessToken(getApplication()),
            videoStatus.name().toLowerCase(),
            LIST_SIZE)
        .enqueue(
            new Callback<VideoListResponse>() {
              @Override
              public void onResponse(
                  Call<VideoListResponse> call, Response<VideoListResponse> response) {
                callback.onSuccessPersonalVideoListResponse(videoStatus, response);
              }

              @Override
              public void onFailure(Call<VideoListResponse> call, Throwable t) {
                Log.i(TAG, "video status = " + videoStatus + " call failed: "  +t);
                callback.onFailureResponse(t);
              }
            });
  }

  public interface PersonalVideoListResponseCallbacks {
    void onSuccessPersonalVideoListResponse(
        VideoStatus status, Response<VideoListResponse> response);

    void onFailureResponse(Throwable t);
  }
}
