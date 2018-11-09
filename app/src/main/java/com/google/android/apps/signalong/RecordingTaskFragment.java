package com.google.android.apps.signalong;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse.DataBean;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.PromptDataView;
import com.google.android.apps.signalong.widget.PromptDataViewAdapter;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import retrofit2.Response;

public class RecordingTaskFragment extends BaseFragment implements
    CameraViewModel.SignPromptBatchResponseCallbacks {
  private static final String TAG = "RecordingTaskFragment";

  private static final TaskType TASK_TYPE = TaskType.NEW_RECORDING;

  private View viewContainer;

  private CameraViewModel cameraViewModel;
  private SignPromptBatchResponse promptList;
  private PromptDataViewAdapter taskViewAdapter;
  private Button recordVideoButton;
  private TextView recordTaskCountTextview;

  private OnClickListener taskViewOnClickListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_recording_task, container, false);
    return viewContainer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    taskViewAdapter = new PromptDataViewAdapter();
    taskViewOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        DataBean data = ((PromptDataView) v).getData();
        ActivityUtils.startCameraActivity(getActivity(), new SignPromptBatchResponse(data));
      }
    };

    recordVideoButton = viewContainer.findViewById(R.id.record_video_button);
    recordVideoButton.setOnClickListener(
            view -> {
              ActivityUtils.startCameraActivity(getActivity(), promptList);
            });
    recordVideoButton.setEnabled(false);
    recordTaskCountTextview = viewContainer.findViewById(R.id.recording_task_count_textview);
    recordTaskCountTextview.setText(
        String.format(getString(R.string.label_recording_task_count), 0));
    RecyclerView recyclerView = viewContainer.findViewById(R.id.recording_task_recyclerview);
    recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 1));
    recyclerView.setAdapter(taskViewAdapter);
  }

  @Override
  public void onResume() {
    initView();
    super.onResume();
  }

  private void initView() {
    cameraViewModel.getSignPromptBatch(this);
  }

  public void onSuccessSignPromptBatchResponse(Response<SignPromptBatchResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0) {
      this.promptList = response.body();
      recordTaskCountTextview.setText(
          String.format(getString(R.string.label_recording_task_count),
              promptList.getData() == null ? 0 : promptList.getData().size()));
      Activity activity = getActivity();
      taskViewAdapter.setPromptList(this.getContext(), response.body(), taskViewOnClickListener);
      recordVideoButton.setEnabled(true);
    } else if (response.isSuccessful() && response.body().getCode() == 406) {
      recordTaskCountTextview.setText(
          String.format(getString(R.string.label_recording_task_count), 0)
              + ", " + response.body().getMessage());
      taskViewAdapter.clearPromptList();
      recordVideoButton.setEnabled(false);
    }
  }

  public void onFailureResponse(Throwable t) {
    Log.e(TAG, String.valueOf(t));
  }

}
