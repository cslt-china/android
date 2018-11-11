package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.widget.TaskView.TaskType;

// TODO(jxue): This adapter is highly similar to RecordedDataViewAdapter, but
// no elegant abstraction was found to avoid such highly overlapped implementation yet.
public class PromptDataViewAdapter extends
    RecyclerView.Adapter<PromptDataViewAdapter.ViewHolder>{
  private static final String TAG = "TaskViewAdapter";

  private SignPromptBatchResponse promptList;
  private Context context;
  private OnClickListener listener;

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public PromptDataView view;

    public ViewHolder(PromptDataView view) {
      super(view);
      this.view = view;
    }
  }

  public PromptDataViewAdapter() {
    context = null;
    promptList = null;
  }

  public void clearPromptList() {
    if (promptList != null && promptList.getData() != null ) {
      this.promptList.getData().clear();
      notifyDataSetChanged();
    }
  }

  public void setPromptList(Context context, SignPromptBatchResponse promptList,
      @Nullable  OnClickListener listener) {
    this.context = context;
    this.promptList = promptList;
    this.listener = listener;
    notifyDataSetChanged();
  }

  @Override
  public PromptDataViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    PromptDataView view = new PromptDataView(parent.getContext(), null);
    ViewHolder viewHolder = new ViewHolder(view);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int position) {
    viewHolder.view.setData(promptList.getData().get(position), TaskType.NEW_RECORDING, position);
    if (this.listener != null) {
      viewHolder.view.setOnClickListener(this.listener);
    }
  }

  @Override
  public int getItemCount() {
    if (promptList == null || promptList.getData() == null) {
      return 0;
    }
    return promptList.getData().size();
  }
}
