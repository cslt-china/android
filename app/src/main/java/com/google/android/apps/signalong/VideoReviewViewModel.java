package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.ReviewVideoResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** VideoReviewViewModel implements review video and download video business logic. */
public class VideoReviewViewModel extends AndroidViewModel {

  private static final String TAG = "VideoReviewViewModel";
  /* Load size per page.*/
  private static final Integer LIMIT_SIZE = 8;
  private final VideoApi videoApi;
  private final MutableLiveData<Response<ReviewVideoResponse>> reviewVideoResponseLiveData;
  private final MutableLiveData<Response<VideoListResponse>> unreviewedVideoResponseLiveData;

  public VideoReviewViewModel(@NonNull Application application) {
    super(application);
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
    reviewVideoResponseLiveData = new MutableLiveData<>();
    unreviewedVideoResponseLiveData = new MutableLiveData<>();
  }

  public void reviewVideo(String uuid, String status,
      VideoReviewViewModel.ReviewVideoResponseCallbacks callback) {
    videoApi
        .reviewVideo(LoginSharedPreferences.getAccessToken(getApplication()), uuid, status)
        .enqueue(
            new Callback<ReviewVideoResponse>() {
              @Override
              public void onResponse(
                  Call<ReviewVideoResponse> call, Response<ReviewVideoResponse> response) {
                callback.onSuccessReviewVideoResponse(response);
              }

              @Override
              public void onFailure(Call<ReviewVideoResponse> call, Throwable t) {
                callback.onFailureResponse();
              }
            });
  }

  public void getUnreviewedVideoList(UnreviewedVideoListResponseCallbacks callback) {
    videoApi.getUnreviewedVideoList(
        LoginSharedPreferences.getAccessToken(getApplication()), 0, LIMIT_SIZE)
        .enqueue(new Callback<VideoListResponse>() {
          @Override
          public void onResponse(Call<VideoListResponse> call,
              Response<VideoListResponse> response) {
            callback.onSuccessUnreviewedVideoListResponse(response);
          }

          @Override
          public void onFailure(Call<VideoListResponse> call, Throwable t) {
            Log.e(TAG, "get unreviewed video list call failed " + t);
            callback.onFailureResponse();
          }
        });
  }

  public interface UnreviewedVideoListResponseCallbacks {
    void onSuccessUnreviewedVideoListResponse(Response<VideoListResponse> response);

    void onFailureResponse();
  }

  public interface ReviewVideoResponseCallbacks {
    void onSuccessReviewVideoResponse(Response<ReviewVideoResponse> response);

    void onFailureResponse();
  }
}
