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
  private static final String TAG = "TaskView";

  private TaskType taskType;
  private VideoListResponse.DataBeanList.DataBean videoData;
  private SignPromptBatchResponse.DataBean promptData;

  public enum TaskType {
    UNKNOWN,
    // The task to do new recordings according to SignPromptBatchResponse
    NEW_RECORDING,
    // The task to review other user's pending recordings according to VideoListResponse
    NEW_REVIEW,
    // The task to view user's own recordings according to VideoListResponse
    MY_RECORDING,
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

  public VideoListResponse.DataBeanList.DataBean getVideoData() {
    if (taskType == TaskType.NEW_REVIEW || taskType == TaskType.MY_RECORDING) {
      return videoData;
    } else {
      return null;
    }
  }

  public SignPromptBatchResponse.DataBean getPromptData() {
    if (taskType == TaskType.NEW_RECORDING) {
      return promptData;
    } else {
      return null;
    }
  }

  public void setData(VideoListResponse.DataBeanList.DataBean data, TaskType taskType) {
    Log.i(TAG, "setData with task type " + taskType);
    videoData = data;
    this.taskType = taskType;
    this.setText(data.getGlossText());
    updateView();
  }

  public void setData(SignPromptBatchResponse.DataBean data, TaskType taskType) {
    if (taskType != TaskType.NEW_RECORDING) {
      Log.e(TAG, "task type must be NEW_RECORDING for SignPromptBatchResponse");
      return;
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
      case MY_RECORDING:
        switch (videoData.getStatus()) {
          case "APPROVED":
            this.setTextColor(getResources().getColor(R.color.green, null));
            break;
          case "PENDING_APPROVAL":
            this.setTextColor(getResources().getColor(R.color.yellow, null));
            break;
          case "REJECTED":
            this.setTextColor(getResources().getColor(R.color.red, null));
            break;
        }
        break;
      default:
        Log.e(TAG, "updateView cannot update unknown task type");
        throw new InvalidParameterException(
            "Invalid AttributeSet of TaskTExtView_taskType=UNKNOWN");
    }
    this.setVisibility(View.VISIBLE);
  }
}
