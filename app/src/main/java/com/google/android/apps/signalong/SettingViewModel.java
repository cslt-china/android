package com.google.android.apps.signalong;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;

/** The SettingViewModel class implements setting business logic. */
public class SettingViewModel extends AndroidViewModel {

  private final MutableLiveData<Boolean> logOutLiveData;

  public SettingViewModel(@NonNull Application application) {
    super(application);
    logOutLiveData = new MutableLiveData<>();
  }

  public MutableLiveData<Boolean> logOut() {
    logOutLiveData.setValue(LoginSharedPreferences.clearUserData(getApplication()));
    return logOutLiveData;
  }
}
