package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.VideoReviewActivity;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.TimerUtils;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class TaskView<T> extends LinearLayoutCompat {
  public enum TaskType {
    UNKNOWN,
    // The task to do new recordings according to SignPromptBatchResponse
    NEW_RECORDING,
    // The task to review other user's pending recordings according to VideoListResponse
    NEW_REVIEW,
    // The task to view user's own recordings according to VideoListResponse
    MY_RECORDING,
  }

  protected T data;
  protected TaskType taskType;
  protected LinearLayoutCompat layout;

  public TaskView(Context context, AttributeSet attrs) {
    super(context, attrs);
    data = null;
    taskType = null;
    layout = null;
  }

  public TaskView(Context context, AttributeSet attrs, int layoutResourceId) {
    super(context, attrs);
    layout = (LinearLayoutCompat) inflate(context, layoutResourceId, this);
  }

  abstract public T getData();

  abstract public void setData(T data, TaskType taskType);

}
