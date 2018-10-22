package com.google.android.apps.signalong;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;
import com.google.android.apps.signalong.VideoViewFragment;


public class ReferenceVideoViewFragment extends Fragment {
  private static final String TAG = "ReferenceVideoViewFrag";
  private VideoViewFragment videoView;

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewContainer = inflater.inflate(R.layout.fragment_reference_video_view, container,
        false);

    videoView = (VideoViewFragment) getChildFragmentManager().findFragmentById(
        R.id.fragment_reference_video_view);

    return viewContainer;
  }

  public void setVideoViewCompletionListener(MediaPlayer.OnCompletionListener listener) {
    videoView.setVideoViewCompletionListener(listener);
  }

  public void stopPlayback() {
    videoView.stopPlayback();
  }

  public void playReference(String title, String videoPath) {
    Log.i(TAG, "play reference video for " + title);
    videoView.viewVideo(videoPath);
  }
}
