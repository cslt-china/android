package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
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

    public TaskViewHolder(TaskView view) {
      super(view);
      taskView = view;
    }
  }

  public TaskViewAdapter() {
    this.context = null;
    this.videoList = null;
    this.signPromptList = null;
    taskType = TaskType.UNKNOWN;
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
    } else {
      viewHolder.taskView.setData(videoList.getData().get(position), taskType);
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
      return signPromptList.getData().size();
    } else {
      return videoList.getData().size();
    }
  }
}