package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.VideoReviewActivity;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class TaskView extends AppCompatTextView {
  private static final String TAG = "ReviewTaskView";

  private TaskType taskType;
  private VideoListResponse.DataBeanList.DataBean videoData;
  private SignPromptBatchResponse.DataBean promptData;

  public enum TaskType {
    UNKNOWN,
    // The task to do new recordings according to SignPromptBatchResponse
    NEW_RECORDING,
    // The task to review other user's pending recordings according to VideoListResponse
    NEW_REVIEW,
    // The task to view user's own accepted recordings according to VideoListResponse
    ACCEPTED_RECORDING,
    // The task to view user's own pending recordings according to VideoListResponse
    PENDING_RECORDING,
    // The task to view user's own rejected recordings according to VideoListResponse
    REJECTED_RECORDING
  }

  public TaskView(Context context, AttributeSet attrs) throws InvalidParameterException {
    super(context, attrs);

    videoData = null;
    promptData = null;
    this.setHeight(50);
    this.setElegantTextHeight(true);
    this.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    this.setVisibility(View.INVISIBLE);
  }

  public void setData(VideoListResponse.DataBeanList.DataBean data, TaskType taskType) {
    videoData = data;
    this.taskType = taskType;
    this.setText(data.getGlossText());
    updateView();
  }

  public void setData(SignPromptBatchResponse.DataBean data, TaskType taskType) {
    if (taskType != TaskType.NEW_RECORDING) {
      Log.e(TAG, "task type must be NEW_RECORDING for SignPromptBatchResponse");
    }
    this.taskType = TaskType.NEW_RECORDING;
    promptData = data;
    this.setText(data.getText());
    updateView();
  }

  private void updateView() {
    switch (taskType) {
      case NEW_RECORDING:
      case NEW_REVIEW:
        this.setTextColor(getResources().getColor(R.color.colorPrimary, null));
        break;
      case ACCEPTED_RECORDING:
        this.setTextColor(getResources().getColor(R.color.green, null));
        break;
      case PENDING_RECORDING:
        this.setTextColor(getResources().getColor(R.color.yellow, null));
        break;
      case REJECTED_RECORDING:
        this.setTextColor(getResources().getColor(R.color.red, null));
        break;
      default:
        Log.e(TAG, "unknown task type");
        throw new InvalidParameterException(
            "Invalid AttributeSet of TaskTExtView_taskType=UNKNOWN");
    }
    this.setVisibility(View.VISIBLE);
  }
}
