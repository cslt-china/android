package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList;
import com.google.android.apps.signalong.widget.TaskView.TaskType;

public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.TaskViewHolder>{
  private static final String TAG = "TaskViewAdapter";

  private VideoListResponse.DataBeanList videoList;
  private SignPromptBatchResponse signPromptList;
  private TaskView.TaskType taskType;
  private Context context;
  private OnClickListener listener;

  public static class TaskViewHolder extends RecyclerView.ViewHolder {
    public TaskView taskView;

    public TextView glossTextView;

    public TaskViewHolder(TaskView view) {
      super(view);
      taskView = view;
    }
  }

  public TaskViewAdapter() {
    context = null;
    taskType = null;
    videoList = null;
    signPromptList = null;
  }

  public TaskViewAdapter(Context context, TaskType taskType) {
    this.context = context;
    this.taskType = taskType;
    videoList = null;
    signPromptList = null;
  }

  public boolean initVideolist(Context context, TaskType taskType,
      @Nullable  OnClickListener listener ) {
    if (this.context != null && this.context != context) {
      Log.e(TAG, "Cannot clear videolist added from a different context!!!");
      return false;
    }
    if (this.taskType != null && this.taskType != taskType) {
      Log.e(TAG, "Cannot clear videolist added from a different taskType!!!");
    }
    if (this.context == null) {
      this.context = context;
    }
    if (this.taskType == null) {
      this.taskType = taskType;
    }
    this.listener = listener;
    videoList = new DataBeanList();
    Log.i(TAG, "initialized taskType " + this.taskType);
    notifyDataSetChanged();
    return true;
  }

  public void addVideoList(Context context, TaskView.TaskType taskType,
      VideoListResponse.DataBeanList videoList) {
    if (this.context != context || this.taskType != taskType) {
      throw new IllegalArgumentException(
          "Cannot add video list from different context or task type!!");
    }
    this.videoList.addAll(videoList);
    notifyDataSetChanged();
  }

  public void setVideoList(Context context, VideoListResponse.DataBeanList videoList,
      TaskView.TaskType taskType, @Nullable  OnClickListener listener) {
    this.context = context;
    this.videoList = videoList;
    this.taskType = taskType;
    this.listener = listener;
    notifyDataSetChanged();
  }

  public void setSignPromptList(Context context, SignPromptBatchResponse signPromptList,
      @Nullable OnClickListener listener) {
    this.context = context;
    this.signPromptList = signPromptList;
    this.taskType = TaskType.NEW_RECORDING;
    this.listener = listener;
    notifyDataSetChanged();
  }

  @Override
  public TaskViewAdapter.TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    TaskView view = new TaskView(parent.getContext(), null);
    TaskViewHolder viewHolder = new TaskViewHolder(view);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(TaskViewHolder viewHolder, int position) {
    if (taskType == TaskType.NEW_RECORDING) {
      viewHolder.taskView.setData(signPromptList.getData().get(position), taskType);
    } else if (taskType == TaskType.MY_RECORDING || taskType == TaskType.NEW_REVIEW){
      viewHolder.taskView.setData(videoList.getData().get(position), taskType);
    } else {
      Log.e(TAG, "task type " + taskType  + " cannot be binded with view holder!!!");
      return;
    }
    if (this.listener != null) {
      viewHolder.taskView.setOnClickListener(this.listener);
    }
  }

  @Override
  public int getItemCount() {
    if (taskType == TaskType.UNKNOWN) {
      return 0;
    } else if (taskType == TaskType.NEW_RECORDING) {
      if (signPromptList == null || signPromptList.getData() == null) {
        return 0;
      }
      return signPromptList.getData().size();
    } else {
      if (videoList == null || videoList.getData() == null) {
        return 0;
      }
      return videoList.getData().size();
    }
  }
}
