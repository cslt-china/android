package com.google.android.apps.signalong;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CountdownFragment extends BaseFragment {
  private final String TAG = "CountdownFragment";

  private static final int DISPLAY_START_TIME_MILLISECOND = 200;

  private TextView titleTextView;
  private TextView textView;
  private ValueAnimator animator = new ValueAnimator();

  private void initCountdownAnimation() {
    animator.addUpdateListener(animator -> {
      long currentTime = animator.getCurrentPlayTime();
      long totalTime = animator.getDuration();
      long leftTime = totalTime - currentTime;
      //Log.i(TAG, String.format("countdowning %d", leftTime / 1000));

      if (leftTime <= DISPLAY_START_TIME_MILLISECOND) {
        textView.setText(getString(R.string.start));
      } else {
        textView.setText(
            String.valueOf((leftTime - DISPLAY_START_TIME_MILLISECOND) / 1000 + 1));
      }
    });
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initCountdownAnimation();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View viewContainer =
        inflater.inflate(R.layout.fragment_countdown, container, false);
    textView = viewContainer.findViewById(R.id.count_text);
    titleTextView = viewContainer.findViewById(R.id.topic_title);
    return viewContainer;
  }

  public void startCountdown(int countDownSecond, String text) {
    titleTextView.setText(String.format(getString(R.string.please_sign), text));
    int totalTime = countDownSecond * 1000 + DISPLAY_START_TIME_MILLISECOND;
    animator.setIntValues(totalTime / 100, 0);
    animator.setDuration(totalTime);
    animator.start();
    //Log.i(TAG, String.format("start countdowning"));
  }

  public void addListener(CancelOrEndAnimatorListener listener) {
    animator.addListener(listener);
  }

  public void endCountdown() {
    animator.end();
    //Log.i(TAG, String.format("end countdowning"));
  }

  public void cancelCountdown() {
    //Log.i(TAG, String.format("cancel countdowning"));
    animator.cancel();
  }
}

