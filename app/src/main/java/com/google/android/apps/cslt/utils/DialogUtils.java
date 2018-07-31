package com.google.android.apps.cslt.utils;

import android.app.AlertDialog;
import android.content.Context;

public class DialogUtils {
    public static void showAlertDialog(Context context,String msg)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.create().show();
    }
}
