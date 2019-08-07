package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.PagedList;
import android.arch.paging.PagedList.Config;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.AuthResponse;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
import com.google.android.apps.signalong.jsonentities.RefreshRequest;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.TimerUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SignAlongViewModel implements display profile and unreviewed videos data and check login logic.
 */
public class SignAlongViewModel extends AndroidViewModel {

    private final UserApi userApi;
    private final MutableLiveData<Boolean> isLoginLiveData;

    public SignAlongViewModel(@NonNull Application application) {
        super(application);
        userApi = ApiHelper.getRetrofit().create(UserApi.class);
        isLoginLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> checkLogin() {
        if (TimerUtils.verifyTime(LoginSharedPreferences.getAccessExp(getApplication()))) {
            isLoginLiveData.setValue(true);
        } else if (TimerUtils.verifyTime(LoginSharedPreferences.getRefreshExp(getApplication()))) {
            userApi.refresh(new RefreshRequest(LoginSharedPreferences.getRefreshToken(getApplication())))
                    .enqueue(
                            new Callback<AuthResponse>() {
                                @Override
                                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                                    if (response.isSuccessful()
                                            && response.body() != null
                                            && response.body().getData() != null
                                            && response.body().getCode() == 0) {
                                        LoginSharedPreferences.saveAccessUserData(
                                                getApplication(), response.body().getData().getAccess());
                                        isLoginLiveData.setValue(true);
                                        return;
                                    }
                                    onFailure(call, null);
                                }

                                @Override
                                public void onFailure(Call<AuthResponse> call, Throwable t) {
                                    isLoginLiveData.setValue(false);
                                }
                            });
        } else {
            isLoginLiveData.setValue(false);
        }
        return isLoginLiveData;
    }

    public MutableLiveData<ProfileResponse> getCurrentPointAndUsernameLiveData() {
        MutableLiveData<ProfileResponse> currentPointAndUsernameLiveData = new MutableLiveData<>();
        userApi.getProfile(LoginSharedPreferences.getAccessToken(getApplication())).enqueue(
                new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(
                            Call<ProfileResponse> call, Response<ProfileResponse> response) {
                        if (response.isSuccessful() && response.body().getCode() == 0) {
                            currentPointAndUsernameLiveData.setValue(response.body());
                            return;
                        }
                        onFailure(call, null);
                    }

                    @Override
                    public void onFailure(Call<ProfileResponse> call, Throwable t) {
                        currentPointAndUsernameLiveData.setValue(null);
                    }
                });
        return currentPointAndUsernameLiveData;
    }
}
