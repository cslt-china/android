package com.google.android.apps.signalong.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.VideoView;
import com.google.android.apps.signalong.R;

/** LearnVideoDialog is used to watch standard video to teach people how to sign language. */
public class LearnVideoDialog extends DialogFragment {

  private DialogListener dialogListener;
  private VideoView videoView;
  /** DialogListener for user to click rerecord and cancel button and get video path. */
  public interface DialogListener {
    void onRerecordClick();

    void onCancelClick();

    String getVideoPath();
  }

  public void setDialogListener(DialogListener dialogListener) {
    this.dialogListener = dialogListener;
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    if (videoView != null && videoView.isPlaying()) {
      videoView.stopPlayback();
    }
    super.onCancel(dialog);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle bundle) {

    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
    View contentView = layoutInflater.inflate(R.layout.dialog_learn_video, null);
    videoView = (VideoView) contentView.findViewById(R.id.video_view);
    videoView.setZOrderOnTop(true);
    contentView
        .findViewById(R.id.play_button)
        .setOnClickListener(
            view -> {
              {
                if (videoView.isPlaying()) {
                  videoView.stopPlayback();
                }
                videoView.setVideoURI(Uri.parse(dialogListener.getVideoPath()));
                videoView.start();
              }
            });

    dialogBuilder.setView(contentView);
    dialogBuilder
        .setNegativeButton(
            getString(R.string.btn_rerecord), (dialog, which) -> dialogListener.onRerecordClick())
        .setPositiveButton(
            getString(R.string.btn_cancel), (dialog, which) -> dialogListener.onCancelClick());
    return dialogBuilder.create();
  }
}
