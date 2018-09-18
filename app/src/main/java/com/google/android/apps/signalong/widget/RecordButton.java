package com.google.android.apps.signalong.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.widget.Button;

/** RecordButton provides display record status. */
public class RecordButton extends Button {
  /* Preview status.*/
  private static final Integer PREVIEW_STATUS = 0;
  /* Recording process status.*/
  private static final Integer RECORDING_STATUS = 1;
  private RecordListener recordListener;
  private ValueAnimator valueAnimator;
  private Integer currentStatus;
  private Integer currentProgress;
  private int currentTime;
  /** DialogListener be call after recording end. */
  public interface RecordListener {
    void onRecordEnd();
  }

  public RecordButton setRecordEndListener(RecordListener recordEndListener) {
    this.recordListener = recordEndListener;
    return this;
  }

  public RecordButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    valueAnimator = ValueAnimator.ofInt(0, 361);
    currentStatus = PREVIEW_STATUS;
    valueAnimator.addUpdateListener(
        valueAnimator -> {
          currentProgress = (Integer) valueAnimator.getAnimatedValue();
          currentTime =
              (int) (valueAnimator.getDuration() - valueAnimator.getCurrentPlayTime()) / 1000;
          invalidate();
        });
    valueAnimator.addListener(
        new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animator) {}

          @Override
          public void onAnimationEnd(Animator animator) {
            if (recordListener == null) {
              return;
            }
            recordListener.onRecordEnd();
          }

          @Override
          public void onAnimationCancel(Animator animator) {}

          @Override
          public void onAnimationRepeat(Animator animator) {}
        });
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (currentStatus.equals(RECORDING_STATUS)) {
      drawRecording(canvas);
    }
  }

  private void drawRecording(Canvas canvas) {
    int cx = (getMeasuredWidth() / 2);
    int cy = (getMeasuredHeight() / 2);
    int outSize = Math.min(cx, cy) - 30;
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setTextSize(50);
    paint.setTextAlign(Paint.Align.CENTER);
    paint.setColor(Color.WHITE);
    paint.setTextAlign(Align.CENTER);
    canvas.drawText(String.format("%ds", currentTime), cx, cy + 15, paint);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth((float) 20.0);
    canvas.drawArc(cx - outSize, cy - outSize, cx + outSize, cy + outSize, 0, 360, false, paint);
    paint.setColor(Color.BLUE);
    canvas.drawArc(
        cx - outSize, cy - outSize, cx + outSize, cy + outSize, 0, currentProgress, false, paint);
  }

  public void intoPreviewStatus() {
    currentStatus = PREVIEW_STATUS;
    setVisibility(GONE);
  }

  public void intoRecordingStatus() {
    currentStatus = RECORDING_STATUS;
    setVisibility(VISIBLE);
    valueAnimator.start();
  }

  public RecordButton setRecordingDuration(long time) {
    valueAnimator.setDuration((time + 1) * 1000);
    return this;
  }
}
