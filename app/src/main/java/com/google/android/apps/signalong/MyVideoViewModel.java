package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MyVideoViewModel implements get a batch of rejected video and pending approval video and approved
 * video logic.
 */
public class MyVideoViewModel extends AndroidViewModel {
  private static final String TAG = "MyVideoViewModel";

  /** The type of PersonalVideoStatus that this object specifies. */
  public enum PersonalVideoStatus {
    /* The request to get all videos of current user.*/
    ALL,
    /* The request to get of rejected videos current user.*/
    REJECTED,
    /* The request to get approved videos of current user.*/
    APPROVED,
    /* The request to get pending approval videos of current user.*/
    PENDING_APPROVAL
  }

  private static final int LIST_SIZE = 8;
  private final VideoApi videoApi;
  private final Map<PersonalVideoStatus, MutableLiveData<Response<VideoListResponse>>>
      personalVideoList;

  public MyVideoViewModel(@NonNull Application application) {
    super(application);
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
    personalVideoList = new HashMap<>();
    for (PersonalVideoStatus personalVideoStatus : PersonalVideoStatus.values()) {
      personalVideoList.put(personalVideoStatus, new MutableLiveData<>());
    }
  }

  public void getPersonalVideoList(
      PersonalVideoStatus videoStatus, PersonalVideoListResponseCallbacks callback) {
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
        PersonalVideoStatus status, Response<VideoListResponse> response);

    void onFailureResponse(Throwable t);
  }
}
