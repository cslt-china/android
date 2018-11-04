package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** The SettingViewModel class implements setting business logic. */
public class SettingViewModel extends AndroidViewModel {

  private final UserApi userApi;
  private final MutableLiveData<Response<ProfileResponse>> profileResponseLiveData;

  private final MutableLiveData<Boolean> logOutLiveData;

  public SettingViewModel(@NonNull Application application) {
    super(application);
    logOutLiveData = new MutableLiveData<>();
    userApi = ApiHelper.getRetrofit().create(UserApi.class);
    profileResponseLiveData = new MutableLiveData<>();
  }

  public MutableLiveData<Boolean> logOut() {
    LoginSharedPreferences.clearUserData(getApplication());
    logOutLiveData.setValue(true);
    return logOutLiveData;
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
                callback.onFailureResponse(t);
              }
            });
  }

  public interface PersonalProfileResponseCallbacks {
    void onSuccessPersonalProfileResponse(Response<ProfileResponse> response);

    void onFailureResponse(Throwable t);
  }


}
