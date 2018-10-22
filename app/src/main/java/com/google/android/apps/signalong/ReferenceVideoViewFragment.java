package com.google.android.apps.signalong;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;
import com.google.android.apps.signalong.VideoViewFragment;


public class ReferenceVideoViewFragment extends Fragment {
  private static final String TAG = "ReferenceVideoViewFrag";
  private VideoViewFragment videoView;
  private TextView titleTextView;
  private TextView startRecordingTextView;

  private OnReferenceCompletionListerner viewCompletionCallback;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewContainer = inflater.inflate(R.layout.fragment_reference_video_view, container,
        false);
    titleTextView = viewContainer.findViewById(R.id.topic_title);
    videoView = (VideoViewFragment) getChildFragmentManager().findFragmentById(
        R.id.fragment_reference_video_view);
    startRecordingTextView = (TextView) viewContainer.findViewById(R.id.button_start_recording);
    return viewContainer;
  }

  @Override
  public void onStart() {
    super.onStart();
    try {
      viewCompletionCallback = (OnReferenceCompletionListerner) getActivity();
      videoView.setVideoViewCompletionListener(new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          viewCompletionCallback.onVideoViewCompletion();
        }
      });
      startRecordingTextView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          videoView.stopPlayback();
          viewCompletionCallback.onVideoViewCompletion();
        }
      });
    } catch (ClassCastException e) {
      throw new ClassCastException(getActivity().toString()
          + " must implet OnReferenceCompletionListerner");
    }
  }

  public void setVideoViewCompletionListener(MediaPlayer.OnCompletionListener listener) {
    videoView.stopPlayback();
    videoView.setVideoViewCompletionListener(listener);
  }

  public void playReference(String title, String videoPath) {
    Log.i(TAG, "play reference video for " + title);
    titleTextView.setText(String.format(getString(R.string.please_sign), title));
    videoView.viewVideo(videoPath);
  }

  public void setVisibility(int visibility) {
    getView().setVisibility(visibility);
  }

  public interface OnReferenceCompletionListerner {
    public void onVideoViewCompletion();
  }
}
