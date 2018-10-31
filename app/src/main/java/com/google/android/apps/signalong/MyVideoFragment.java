package com.google.android.apps.signalong;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract.Profile;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Person;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.google.android.apps.signalong.MyVideoViewModel.PersonalVideoStatus;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
import com.google.android.apps.signalong.jsonentities.ProfileResponse.DataBean.ScoresBean;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.TaskView;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import com.google.android.apps.signalong.widget.TaskViewAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Response;

public class MyVideoFragment extends Fragment implements
    MyVideoViewModel.PersonalVideoListResponseCallbacks,
    MyVideoViewModel.PersonalProfileResponseCallbacks {
  private static final String TAG = "MyVideoFragment";

  private static final int SPAN_COUNT = 4;

  protected static final Map<PersonalVideoStatus, TaskType> VIDEO_STATUS_TO_TASK_TYPE_MAP =
      new HashMap<>();
  static {
    VIDEO_STATUS_TO_TASK_TYPE_MAP.put(PersonalVideoStatus.REJECTED,
        TaskType.REJECTED_RECORDING);
    VIDEO_STATUS_TO_TASK_TYPE_MAP.put(PersonalVideoStatus.PENDING_APPROVAL,
        TaskType.PENDING_RECORDING);
    VIDEO_STATUS_TO_TASK_TYPE_MAP.put(PersonalVideoStatus.APPROVED,
        TaskType.ACCEPTED_RECORDING);
  }

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
  private Map<PersonalVideoStatus, TaskViewAdapter> taskViewAdapterMap;
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
    taskViewAdapterMap = new HashMap<>();
    for(PersonalVideoStatus status : PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.keySet()) {
      taskViewAdapterMap.put(status, new TaskViewAdapter());
    }

    taskViewOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        VideoListResponse.DataBeanList.DataBean data = ((TaskView) v).getVideoData();
        ViewMyVideoActivity.startActivity(getActivity(), data);
      }
    };

    return viewContainer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initRecyclerView(R.id.my_recent_videos_recyclerview,
        taskViewAdapterMap.get(PersonalVideoStatus.PENDING_APPROVAL));
    initRecyclerView(R.id.my_rejected_videos_recyclerview,
        taskViewAdapterMap.get(PersonalVideoStatus.REJECTED));
    initRecyclerView(R.id.my_approved_videos_recyclerview,
        taskViewAdapterMap.get(PersonalVideoStatus.APPROVED));
  }

  private void initRecyclerView(int recylcerViewId, TaskViewAdapter adapter) {
    RecyclerView recyclerView = viewContainer.findViewById(recylcerViewId);
    recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(),
        SPAN_COUNT));
    recyclerView.setAdapter(adapter);
  }

  @Override
  public void onResume() {
    initView();
    super.onResume();
  }

  private void initView() {
    myVideoViewModel.getProfile(this);
    for(PersonalVideoStatus status : PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.keySet()) {
      myVideoViewModel.getPersonalVideoList(status, this);
    }
  }

  public void onSuccessPersonalProfileResponse(Response<ProfileResponse> response) {
    if (response == null || !response.isSuccessful()) {
      ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_connect_fail));
      return;
    }
    if (response.body() == null ) {
      ((TextView) viewContainer.findViewById(R.id.my_task_profile_title_textview))
          .setText(getString(R.string.label_loading));
      return;
    }
    if (response.body().getCode() != 0) {
      return;
    }

    ProfileResponse.DataBean data = response.body().getData();
    ((TextView) viewContainer.findViewById(R.id.my_task_profile_title_textview))
        .setText(String.format(getString(R.string.label_task_profile_title),
            data.getUsername()));
    ScoresBean scoresBean = data.getScores();
    ((TextView) viewContainer.findViewById(R.id.my_uploaded_video_count_textview))
        .setText(String.format(getString(R.string.label_uploaded_video_count),
            scoresBean.getVideoCreationCount()));
    ((TextView) viewContainer.findViewById(R.id.my_recording_score_textview))
        .setText(
            String.format(getString(R.string.label_my_recording_score),
                scoresBean.getTotalScore() - scoresBean.getVideoReviewScore()));
    ((TextView) viewContainer.findViewById(R.id.my_reviewed_video_count_textview))
        .setText(String.format(getString(R.string.label_reviewed_video_count),
            scoresBean.getVideoReviewCount()));
    ((TextView) viewContainer.findViewById(R.id.my_review_score_textview))
        .setText(String.format(getString(R.string.label_my_review_score),
            scoresBean.getVideoReviewScore()));
  }

  public void onSuccessPersonalVideoListResponse(
      PersonalVideoStatus status, Response<VideoListResponse> response) {
    if (response == null || !response.isSuccessful()) {
      ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_connect_fail));
      return;
    }
    if (response.body() != null && response.body().getCode() == 0) {
      VideoListResponse.DataBeanList dataBeanList = response.body().getDataBeanList();
      ((TextView) viewContainer.findViewById(PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.get(status).first))
        .setText(String.format(getString(PERSONAL_VIDEO_STATUS_VIEW_ID_MAP.get(status).second),
            dataBeanList.getTotal()));
      taskViewAdapterMap.get(status).setVideoList(
          getActivity().getApplicationContext(),
          dataBeanList,
          VIDEO_STATUS_TO_TASK_TYPE_MAP.get(status),
          taskViewOnClickListener);
    }
  }

  public void onFailureResponse() {
  }
}