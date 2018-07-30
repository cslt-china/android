package com.example.zhichongh.collectsignlanguage.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private volatile static ToastUtils mToastUtils;
    private ToastUtils (){}
    public static ToastUtils getSingleton() {
        if (mToastUtils == null) {
            synchronized (ToastUtils.class) {
                if (mToastUtils == null) {
                    mToastUtils = new ToastUtils();
                }
            }
        }
        return mToastUtils;
    }
    public void show(Context context,String msg)
    {
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
}
