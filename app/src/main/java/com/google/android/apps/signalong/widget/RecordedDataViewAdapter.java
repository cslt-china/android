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
import java.security.InvalidParameterException;
import java.util.List;

public class RecordedDataViewAdapter extends
    RecyclerView.Adapter<RecordedDataViewAdapter.ViewHolder>{
  private static final String TAG = "TaskViewAdapter";

  private DataBeanList videoList;
  private TaskView.TaskType taskType;
  private Context context;
  private OnClickListener listener;

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public RecordedDataView view;

    public ViewHolder(RecordedDataView view) {
      super(view);
      this.view = view;
    }
  }

  public RecordedDataViewAdapter() {
    context = null;
    taskType = null;
    videoList = null;
  }

  public RecordedDataViewAdapter(Context context, TaskType taskType) {
    this.context = context;
    this.taskType = taskType;
    videoList = null;
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

  @Override
  public RecordedDataViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    RecordedDataView view = new RecordedDataView(parent.getContext(), null);
    ViewHolder viewHolder = new ViewHolder(view);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int position) {
    if (taskType == TaskType.MY_RECORDING || taskType == TaskType.NEW_REVIEW){
      viewHolder.view.setData(videoList.getData().get(position), taskType);
    } else {
      throw new InvalidParameterException(
          "task type " + taskType  + " cannot be binded with view holder!!!");
    }
    if (this.listener != null) {
      viewHolder.view.setOnClickListener(this.listener);
    }
  }

  @Override
  public int getItemCount() {
    if (videoList == null || videoList.getData() == null) {
      return 0;
    }
    return videoList.getData().size();
  }
}
