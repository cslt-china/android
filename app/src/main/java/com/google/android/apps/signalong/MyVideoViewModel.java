package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
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

  public MutableLiveData<Response<VideoListResponse>> getPersonalVideoList(
      PersonalVideoStatus videoStatus) {
    videoApi
        .getPersonalVideoList(
            LoginSharedPreferences.getAccessToken(getApplication()),
            videoStatus.name().toLowerCase())
        .enqueue(
            new Callback<VideoListResponse>() {
              @Override
              public void onResponse(
                  Call<VideoListResponse> call, Response<VideoListResponse> response) {
                personalVideoList.get(videoStatus).setValue(response);
              }

              @Override
              public void onFailure(Call<VideoListResponse> call, Throwable t) {
                personalVideoList.get(videoStatus).setValue(null);
              }
            });
    return personalVideoList.get(videoStatus);
  }

  public void getProfile() {
    userApi
        .getProfile(LoginSharedPreferences.getAccessToken(getApplication()))
        .enqueue(
            new Callback<ProfileResponse>() {
              @Override
              public void onResponse(
                  Call<ProfileResponse> call, Response<ProfileResponse> response) {
                profileResponseLiveData.setValue(response);
              }

              @Override
              public void onFailure(Call<ProfileResponse> call, Throwable t) {
                profileResponseLiveData.setValue(null);
              }
            });
  }

  public MutableLiveData<Response<ProfileResponse>> getProfileResponseLiveData() {
    return profileResponseLiveData;
  }
}
