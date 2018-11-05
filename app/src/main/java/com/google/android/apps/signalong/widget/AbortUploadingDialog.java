package com.google.android.apps.signalong.widget;


import com.google.android.apps.signalong.BaseActivity;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.util.List;

import com.google.android.apps.signalong.CameraViewModel;

/** HelpDialog displays help content. */
@SuppressLint("ValidFragment")
public class AbortUploadingDialog extends DialogFragment {
  CameraViewModel cameraViewModel;
  String confirmButtonText;
  String cancelButtonText;

  Runnable onConfirmed = null;
  Runnable onCanceled = null;
  Activity activity;

  public AbortUploadingDialog(Activity activity, String confirmBottonText, String cancelButtonText,
                              CameraViewModel cameraViewModel,
                              Runnable onConfirmed,
                              Runnable onCanceled
                              ) {
    this.confirmButtonText = confirmBottonText;
    this.cancelButtonText = cancelButtonText;
    this.cameraViewModel = cameraViewModel;
    this.onConfirmed = onConfirmed;
    this.onCanceled = onCanceled;
    this.activity = activity;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle bundle) {

    android.support.v7.app.AlertDialog.Builder builder =
        new android.support.v7.app.AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.check_uploading);
    builder.setMessage(R.string.has_uploading_task);
    builder.setPositiveButton(confirmButtonText, (DialogInterface dialog, int which) ->{

        cameraViewModel.clearAllTask(()-> {
          dialog.dismiss();

          this.activity.runOnUiThread(() -> {
            if (onConfirmed != null) {
              onConfirmed.run();
            }
          });
        });

    });

    builder.setNegativeButton(cancelButtonText, (DialogInterface dialog, int which) -> {
        dialog.dismiss();
        if (onCanceled != null) {
          onCanceled.run();
        }
    });

    return builder.create();
  }

  public static void check(BaseActivity activity,
                    String confirmBottonText, String cancelButtonText,
                    CameraViewModel cameraViewModel,
                    Runnable onConfirmed,
                    Runnable onCanceled) {
    cameraViewModel.checkUnfinishedTask((List<VideoUploadTask> tasks) -> {
      if (tasks.isEmpty()) {
        activity.runOnUiThread(onConfirmed);
        return;
      }

      activity.runOnUiThread(()-> {
        AbortUploadingDialog dialog = new AbortUploadingDialog(activity, confirmBottonText,
            cancelButtonText, cameraViewModel,
            onConfirmed, onCanceled);

        dialog.show(activity.getSupportFragmentManager(),
                    AbortUploadingDialog.class.getSimpleName());
      });
    });
  }
}

