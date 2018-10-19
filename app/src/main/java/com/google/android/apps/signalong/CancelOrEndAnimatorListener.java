package com.google.android.apps.signalong;

import android.animation.Animator;

class CancelOrEndAnimatorListener implements Animator.AnimatorListener {
  private boolean valid;
  @Override
  final public void onAnimationStart(Animator animation) {
    valid = true;
    onStart(animation);
  }

  @Override
  final public void onAnimationEnd(Animator animation) {
    if (valid) {
      onEnd(animation);
    }
  }

  @Override
  final public void onAnimationCancel(Animator animation) {
    valid = false;
    onCancel(animation);
  }

  @Override
  final public void onAnimationRepeat(Animator animation) {}

   public void onStart(Animator animator) {};
   public void onEnd(Animator animator) {};
   public void onCancel(Animator animator) {};
}
