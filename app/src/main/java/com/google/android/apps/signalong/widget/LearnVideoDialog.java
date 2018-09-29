package com.google.android.apps.signalong.widget;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.apps.signalong.R;

/** LearnVideoDialog is used to watch standard video to teach people how to sign language. */
public class LearnVideoDialog extends DialogFragment {

  private DialogListener dialogListener;
  private VideoView videoView;
  int x = 0;
  int y = 0;
  int width = ViewGroup.LayoutParams.MATCH_PARENT;
  int height = ViewGroup.LayoutParams.MATCH_PARENT;

  public void setPosition(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }
  /** DialogListener for user to click rerecord and cancel button and get video path. */
  public interface DialogListener {
    void onCloseClick();
    String getVideoPath();
    public String getThumbnail();
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

  private void setDialogLayout(Dialog dialog) {
    Window window = dialog.getWindow();
    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    WindowManager.LayoutParams params = window.getAttributes();
    params.x = this.x;
    params.y = this.y;
    params.width = this.width;
    params.height = this.height;
    window.setAttributes(params);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle bundle) {

    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
    LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
    View contentView = layoutInflater.inflate(R.layout.dialog_learn_video, null);
    videoView = (VideoView) contentView.findViewById(R.id.video_view);
    videoView.setZOrderOnTop(true);

    ImageView button = contentView.findViewById(R.id.play_button);
    Glide.with(button.getContext()).load(dialogListener.getThumbnail()).into(button);
    button
        .findViewById(R.id.play_button)
        .setOnClickListener(
            view -> {
              {
                videoView.setVisibility(View.VISIBLE);
                if (videoView.isPlaying()) {
                  videoView.stopPlayback();
                }
                videoView.setVideoURI(Uri.parse(dialogListener.getVideoPath()));
                videoView.start();

                //Prevent video and thumbnail cross, whenthe sizes of video and thumbernail are
                // different.
                button.setVisibility(View.INVISIBLE);
              }
            });

    dialogBuilder.setView(contentView);

    Dialog dialog = dialogBuilder.create();

    setDialogLayout(dialog);
    dialog.setCanceledOnTouchOutside(false);

    Button closeButton = contentView.findViewById(R.id.close_button);
    closeButton.setOnClickListener((view)-> {
      dialogListener.onCloseClick();
      dialog.cancel();
    });

    dialog.setOnKeyListener((DialogInterface arg0, int keyCode,
                    KeyEvent event) -> {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                  dialogListener.onCloseClick();
                  dialog.cancel();
                }
                return true;
            }
        );

    return dialog;
  }

  //https://blog.csdn.net/X1876631/article/details/53392880
  @Override
  public void show(FragmentManager manager, String tag) {
    try {
      //在每个add事务前增加一个remove事务，防止连续的add
      manager.beginTransaction().remove(this).commit();
      super.show(manager, tag);
    } catch (Exception e) {
      //同一实例使用不同的tag会异常,这里捕获一下
      e.printStackTrace();
    }
  }


}
