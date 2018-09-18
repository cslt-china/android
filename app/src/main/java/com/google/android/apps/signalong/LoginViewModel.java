package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.jsonentities.AuthResponse;
import com.google.android.apps.signalong.jsonentities.User;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** The LoginViewModel class implements login business logic */
public class LoginViewModel extends AndroidViewModel {
  private final UserApi userApi;

  public LoginViewModel(@NonNull Application application) {
    super(application);
    userApi = ApiHelper.getRetrofit().create(UserApi.class);
  }

  public MutableLiveData<Response<AuthResponse>> login(User user) {
    MutableLiveData<Response<AuthResponse>> loginLiveData = new MutableLiveData<>();
    userApi
        .login(user)
        .enqueue(
            new Callback<AuthResponse>() {
              @Override
              public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful()
                    && response.body() != null
                    && response.body().getCode() == 0) {
                  LoginSharedPreferences.saveAccessUserData(
                      getApplication(), response.body().getData().getAccess());
                  LoginSharedPreferences.saveRefreshUserData(
                      getApplication(), response.body().getData().getRefresh());
                  loginLiveData.setValue(response);
                  return;
                }
                onFailure(call, null);
              }

              @Override
              public void onFailure(Call<AuthResponse> call, Throwable t) {
                loginLiveData.setValue(null);
              }
            });
    return loginLiveData;
  }
}
