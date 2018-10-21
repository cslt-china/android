package com.google.android.apps.signalong;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.widget.CameraView;


public class RecordFragment extends Fragment {
  private final String TAG = "RecordFragment";
  private CameraView cameraView;
  private TextView titleTextView;
  private ProgressBar progressBar;
  private ValueAnimator progressAnimator;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View fragmentgView = inflater.inflate(R.layout.fragment_record, container, false);
    progressBar = fragmentgView.findViewById(R.id.progres_bar);
    titleTextView = fragmentgView.findViewById(R.id.topic_title);
    cameraView = fragmentgView.findViewById(R.id.camera_view);
    initProgressAnimation();
    return fragmentgView;
  }

  public void setVisibility(int visibility) {
    getView().setVisibility(visibility);
  }

  private void initProgressAnimation() {
    progressBar.setMax(100);
    progressAnimator = ValueAnimator.ofInt(0, 100);
    progressAnimator.addUpdateListener(valueAnimator ->
    {
      progressBar.setProgress((int) valueAnimator.getAnimatedValue());
    });
  }

  public void startPreview() {
    cameraView.startPreview();
  }

  public void startRecord(String name, String videoFilePath, int recordingTime) {
    titleTextView.setText(String.format(getString(R.string.please_sign), name));
    cameraView.startRecord(videoFilePath);

    //FIXME: move this to camera callback?
    if (recordingTime < 2) {
      recordingTime = 2;
    }
    progressAnimator.setDuration(recordingTime * 1000);
    progressAnimator.start();
  }

  public void stopRecording() {
    cameraView.stopRecording();
    progressAnimator.cancel();
    progressBar.setProgress(0);
  }

  public void setCameraCallBack(CameraView.CallBack callBack) {
    cameraView.setCallBack(callBack);
  }

  public void setRecordCallback(CancelOrEndAnimatorListener listener) {
    progressAnimator.addListener(listener);
  }

  //TODO: move to cameraview's onStop
  public void closeCamera() {
    cameraView.closeCamera();
  }
}
