package com.google.android.apps.signalong.widget;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.apps.signalong.CameraViewModel;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.db.dao.VideoUploadTaskDao;

/** HelpDialog displays help content. */
@SuppressLint("ValidFragment")
public class AbortUploadingDialog extends DialogFragment {
  CameraViewModel cameraViewModel;
  String confirmButtonText;
  String cancelButtonText;

  Runnable onConfirmed = null;
  Runnable onCanceled = null;

  public AbortUploadingDialog(String confirmBottonText, String cancelButtonText) {
    this.confirmButtonText = confirmBottonText;
    this.cancelButtonText = cancelButtonText;
    cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle bundle) {

    android.support.v7.app.AlertDialog.Builder builder =
        new android.support.v7.app.AlertDialog.Builder(getActivity());
    builder.setTitle("has task");
    builder.setMessage("has task");
    builder.setPositiveButton(confirmButtonText, (DialogInterface dialog, int which) ->{
        dialog.dismiss();
        cameraViewModel.clearAllTask();
        if (onConfirmed != null) {
          onConfirmed.run();
        }
    });
    builder.setNegativeButton(cancelButtonText, (DialogInterface dialog, int which) -> {
        dialog.dismiss();
        if (onCanceled != null) {
          onCanceled.run();
        }
    });

    return builder.create();
  }

  public void setOnConfirmed(Runnable onConfirmed) {
    this.onConfirmed = onConfirmed;
  }

  public void setCanceled(Runnable onCanceled) {
    this.onConfirmed = onCanceled;
  }
}

