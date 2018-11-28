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
import com.google.android.apps.signalong.db.AppDatabase;
import com.google.android.apps.signalong.db.dao.VideoUploadTaskDao;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse.DataBean;
import com.google.android.apps.signalong.service.FetchUnfinishedVideoUploadTask;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.PromptDataView;
import com.google.android.apps.signalong.widget.PromptDataViewAdapter;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import retrofit2.Response;

public class RecordingTaskFragment extends BaseFragment implements
    CameraViewModel.SignPromptBatchResponseCallbacks,
    FetchUnfinishedVideoUploadTask.Callbacks {
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
        for(int i = ((PromptDataView) v).getPosition()-1; i >= 0; i--) {
          promptList.getData().remove(i);
        }
        ActivityUtils.startCameraActivity(getActivity(), promptList);
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

  // This is a temporary solution to solve the following issue:
  //   When the CameraActivity returns back to the main activity, the last video uploading
  // might not be finished, and server side would not mark the video in the right status.
  // The main activity will fetch prompt bunch from server, often showing the lastly recorded gloss.
  //
  // The solution here is to check remaining uploading gloss entries in VideoUploadTaskDao, and
  // remove them from the server response of prompt list.
  public void onUnfinishedVideoUploadFetched(Set<Integer> uploadUnfinishedPromptIds) {
    if (promptList.getData() != null && promptList.getData().size() > 0) {
      for (int i = promptList.getData().size() - 1; i >= 0; i--) {
        SignPromptBatchResponse.DataBean prompt = promptList.getData().get(i);
        if (uploadUnfinishedPromptIds.contains(prompt.getId())) {
          Log.i(TAG, String.format(
                  "Removing prompt %d: %s because it was recorded and is not uploaded to server yet.",
                  i, prompt.getText()));
          promptList.getData().remove(i);
        }
      }
    }
    recordTaskCountTextview.setText(
        String.format(getString(R.string.label_recording_task_count),
            promptList.getData() == null ? 0 : promptList.getData().size()));

    if (promptList.getData() != null && promptList.getData().size() > 0) {
      taskViewAdapter.setPromptList(this.getContext(), promptList, taskViewOnClickListener);
      recordVideoButton.setEnabled(true);
    }
  }

  public void onSuccessSignPromptBatchResponse(Response<SignPromptBatchResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0) {
      this.promptList = response.body();
      new FetchUnfinishedVideoUploadTask(getActivity(), this).execute();
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
