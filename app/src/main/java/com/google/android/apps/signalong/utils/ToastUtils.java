package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.widget.Toast;

/** The ToastUtils class simplifies the display of Toast. */
public class ToastUtils {

  public static void show(Context context, String msg) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
  }
}
