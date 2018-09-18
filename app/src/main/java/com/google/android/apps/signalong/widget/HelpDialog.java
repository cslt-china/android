package com.google.android.apps.signalong.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.apps.signalong.R;

/** HelpDialog displays help content. */
@SuppressLint("ValidFragment")
public class HelpDialog extends DialogFragment {
  private final DialogListener dialogListener;

  /** DialogListener for user to click button. */
  public interface DialogListener {
    void onClick(View view);
  }

  public HelpDialog(DialogListener dialogListener) {
    this.dialogListener = dialogListener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle bundle) {

    android.support.v7.app.AlertDialog.Builder dialogBuilder =
        new android.support.v7.app.AlertDialog.Builder(getActivity());
    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
    View contentView = layoutInflater.inflate(R.layout.dialog_help, null);
    contentView
        .findViewById(R.id.dialog_ok_button)
        .setOnClickListener(
            view -> {
              if (dialogListener == null) {
                return;
              }
              dialogListener.onClick(view);
            });
    return dialogBuilder.setView(contentView).create();
  }
}
