package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
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
  private final UserApi userApi;
  private final Map<PersonalVideoStatus, MutableLiveData<Response<VideoListResponse>>>
      personalVideoList;
  private final MutableLiveData<Response<ProfileResponse>> profileResponseLiveData;

  public MyVideoViewModel(@NonNull Application application) {
    super(application);
    videoApi = ApiHelper.getRetrofit().create(VideoApi.class);
    userApi = ApiHelper.getRetrofit().create(UserApi.class);
    personalVideoList = new HashMap<>();
    for (PersonalVideoStatus personalVideoStatus : PersonalVideoStatus.values()) {
      personalVideoList.put(personalVideoStatus, new MutableLiveData<>());
    }
    profileResponseLiveData = new MutableLiveData<>();
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
                callback.onFailureResponse();
              }
            });
  }

  public void getProfile(PersonalProfileResponseCallbacks callback) {
    userApi
        .getProfile(LoginSharedPreferences.getAccessToken(getApplication()))
        .enqueue(
            new Callback<ProfileResponse>() {
              @Override
              public void onResponse(
                  Call<ProfileResponse> call, Response<ProfileResponse> response) {
                callback.onSuccessPersonalProfileResponse(response);
              }

              @Override
              public void onFailure(Call<ProfileResponse> call, Throwable t) {
                callback.onFailureResponse();
              }
            });
  }

  public interface PersonalVideoListResponseCallbacks {
    void onSuccessPersonalVideoListResponse(
        PersonalVideoStatus status, Response<VideoListResponse> response);

    void onFailureResponse();
  }

  public interface PersonalProfileResponseCallbacks {
    void onSuccessPersonalProfileResponse(Response<ProfileResponse> response);

    void onFailureResponse();
  }
}
