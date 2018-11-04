package com.google.android.apps.signalong;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.google.android.apps.signalong.MyVideoViewModel.PersonalVideoStatus;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.RecordedDataView;
import com.google.android.apps.signalong.widget.RecordedDataViewAdapter;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Response;

public class MyVideoFragment extends Fragment implements
    MyVideoViewModel.PersonalVideoListResponseCallbacks {
  private static final String TAG = "MyVideoFragment";

  private static final TaskType TASK_TYPE = TaskType.MY_RECORDING;

  private static final Map<PersonalVideoStatus, Pair<Integer, Integer>>
      PERSONAL_VIDEO_STATUS_VIEW_ID_MAP = new HashMap<>();
  static {
    PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.put(PersonalVideoStatus.REJECTED,
        Pair.create(R.id.my_rejected_video_count_textview, R.string.label_rejected_video_count));
    PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.put(PersonalVideoStatus.PENDING_APPROVAL,
        Pair.create(R.id.my_pending_video_count_textview, R.string.label_pending_video_count));
    PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.put(PersonalVideoStatus.APPROVED,
        Pair.create(R.id.my_accepted_video_count_textview, R.string.label_accepted_video_count));
  };

  private View viewContainer;
  private RecordedDataViewAdapter taskViewAdapter;
  private OnClickListener taskViewOnClickListener;
  private MyVideoViewModel myVideoViewModel;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    myVideoViewModel = ViewModelProviders.of(this).get(MyVideoViewModel.class);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_my_video, container, false);
    taskViewAdapter = new RecordedDataViewAdapter();

    taskViewOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        VideoListResponse.DataBeanList.DataBean data = ((RecordedDataView) v).getData();
        ViewMyVideoActivity.startActivity(getActivity(), data);
      }
    };

    return viewContainer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initRecyclerView(R.id.my_recent_videos_recyclerview, taskViewAdapter);
  }

  private void initRecyclerView(int recylcerViewId, RecordedDataViewAdapter adapter) {
    RecyclerView recyclerView = viewContainer.findViewById(recylcerViewId);
    recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 1));
    recyclerView.setAdapter(adapter);
  }

  @Override
  public void onResume() {
    initView();
    super.onResume();
  }

  private void initView() {
    taskViewAdapter.initVideolist(getActivity().getApplicationContext(), TASK_TYPE,
        taskViewOnClickListener);
    for(PersonalVideoStatus status : PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.keySet()) {
      Log.i(TAG, "initView for status " + status);
      myVideoViewModel.getPersonalVideoList(status, this);
    }
  }

  public void onSuccessPersonalVideoListResponse(
      PersonalVideoStatus status, Response<VideoListResponse> response) {
    if (response == null || !response.isSuccessful()) {
      ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_connect_fail));
      Log.i(TAG, "onSuccessPersonalVideoListResponse connection failed " + status);
      return;
    }
    if (response.body() != null && response.body().getCode() == 0) {
      VideoListResponse.DataBeanList dataBeanList = response.body().getDataBeanList();
      ((TextView) viewContainer.findViewById(PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.get(status).first))
        .setText(String.format(getString(PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.get(status).second),
            dataBeanList.getTotal()));
      if (dataBeanList.getData() != null && !dataBeanList.getData().isEmpty()) {
        taskViewAdapter
            .addVideoList(getActivity().getApplicationContext(), TASK_TYPE, dataBeanList);
      }
    } else {
      Log.i(TAG, "onSuccessPersonalVideoListResponse status: " + status + "no response");
    }
  }

  public void onFailureResponse(Throwable t) {
    Log.e(TAG, String.valueOf(t));
  }
}