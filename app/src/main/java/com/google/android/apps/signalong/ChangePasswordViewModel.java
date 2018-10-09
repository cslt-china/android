package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.jsonentities.BaseResponse;
import com.google.android.apps.signalong.jsonentities.ChangePasswords;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordViewModel extends AndroidViewModel {
  private final UserApi userApi;
  private MutableLiveData<Boolean> changePasswordLiveData;
  private String errorMessage;
  public ChangePasswordViewModel(@NonNull Application application) {
    super(application);
    userApi = ApiHelper.getRetrofit().create(UserApi.class);
  }
  
  public MutableLiveData<Boolean> changePassword(ChangePasswords passwords) {
    MutableLiveData<Boolean> changePasswordLiveData = new MutableLiveData<>();
    userApi.changePassword(
      LoginSharedPreferences.getAccessToken(getApplication()), passwords)
      .enqueue(new Callback<BaseResponse>() {
        @Override
        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
          if(response.body().getCode() == 0) {
            changePasswordLiveData.setValue(true);
          } else {
            changePasswordLiveData.setValue(false);
            errorMessage = response.body().getMessage();
          }
        }

        @Override
        public void onFailure(Call<BaseResponse> call, Throwable t) {
          changePasswordLiveData.setValue(false);
        }
      });

    return changePasswordLiveData;
  }

  public MutableLiveData<Boolean> getChangePasswordLiveData() {
    return changePasswordLiveData;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
