package com.google.android.apps.signalong;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;

import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.VideoScreenUtils;


public class SelfAssessRecordedVideoFragment extends BaseFragment {
  private static final String TAG = "SelfAssessRecordedVideo";
  private VideoViewFragment videoView;
  private TextView titleTextView;
  private RelativeLayout buttonsLayout;
  private Button retryButton;
  private Button submitButton;

  private OnSelfAssessRecordedVideoListerner selfAssessmentCallback;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewContainer = inflater.inflate(R.layout.fragment_recorded_video_view, container,
        false);
    titleTextView = viewContainer.findViewById(R.id.topic_title);
    videoView = (VideoViewFragment) getChildFragmentManager().findFragmentById(
        R.id.fragment_recorded_video);
    Log.i(TAG, "viewView:" + String.valueOf(videoView));
    retryButton = viewContainer.findViewById(R.id.button_retry);
    submitButton = viewContainer.findViewById(R.id.button_submit);
    return viewContainer;
  }

  @Override
  public void onStart() {
    super.onStart();
    try {
      videoView.setVideoViewCompletionListener(new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          Log.i(TAG, "video view completed.");
          retryButton.setEnabled(true);
          submitButton.setEnabled(true);
        }
      });
      selfAssessmentCallback = (OnSelfAssessRecordedVideoListerner) getActivity();
      retryButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          videoView.stopPlayback();
          selfAssessmentCallback.onRejectRecordedVideo();
        }
      });
      submitButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          videoView.stopPlayback();
          selfAssessmentCallback.onAcceptRecordedVideo();
        }
      });
    } catch (ClassCastException e) {
      throw new ClassCastException(getActivity().toString()
          + " must implet OnReferenceCompletionListerner");
    }
  }

  public void playRecorded(String title, String videoPath) {
    Log.i(TAG, "play recorded video for " + title + " from " + videoPath);
    //There is a moment of black screen, before renderering the first frame,
    //On this moment, another videoview will exposed which in a hidden fragment.
    //So to cover the incorrectly exposed videoview, we must add a thumbnail 
    //image.
    titleTextView.setText(String.format(getString(R.string.please_sign), title));
    //TODO: place the thumbnail to tmpDir
    String thumbnailPath = videoPath + "_thumbernal.png";
    VideoScreenUtils.screenFromVideo(videoPath, thumbnailPath);
    videoView.viewVideo(FileUtils.buildUri(videoPath),
                        FileUtils.buildUri(thumbnailPath));
  }

  public void setVisibility(int visibility) {
    retryButton.setEnabled(visibility==View.VISIBLE);
    submitButton.setEnabled(visibility==View.VISIBLE);
    videoView.setVisibility(visibility);
    super.setVisibility(visibility);
  }

  public interface OnSelfAssessRecordedVideoListerner {
    public void onRejectRecordedVideo();

    public void onAcceptRecordedVideo();
  }

}
