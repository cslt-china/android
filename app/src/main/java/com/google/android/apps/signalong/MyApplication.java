package com.google.android.apps.signalong;

import android.app.Application;
import android.content.Context;

import com.google.android.apps.signalong.utils.CrashHandlerUtil;
import com.google.android.apps.signalong.utils.FileLogUtil;

public class MyApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        CrashHandlerUtil.getInstance().init(this);
        FileLogUtil.init(this);
    }

    public static Context getAppContext(){
        return sContext;
    }
}
