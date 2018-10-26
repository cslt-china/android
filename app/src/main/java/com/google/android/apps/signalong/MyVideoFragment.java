package com.google.android.apps.signalong;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore.Video;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Response;

public class MyVideoFragment extends Fragment {
  private static final String TAG = "MyVideoFragment";

  private static final int SPAN_COUNT = 4;

  private MyVideoViewModel myVideoViewModel;
  private View viewContainer;
  private TaskViewAdapter pendingTaskViewAdapter;
  private TaskViewAdapter rejectedTaskViewAdapter;
  private TaskViewAdapter approvedTaskViewAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    myVideoViewModel = ViewModelProviders.of(this).get(MyVideoViewModel.class);
    myVideoViewModel.getPersonalVideoList(PersonalVideoStatus.REJECTED);
    myVideoViewModel.getPersonalVideoList(PersonalVideoStatus.PENDING_APPROVAL);
    myVideoViewModel.getPersonalVideoList(PersonalVideoStatus.APPROVED);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_my_video, container, false);
    pendingTaskViewAdapter = new TaskViewAdapter();
    rejectedTaskViewAdapter = new TaskViewAdapter();
    approvedTaskViewAdapter = new TaskViewAdapter();
    return viewContainer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initProfileView();
    initVideoListView();
    initRecyclerView(R.id.my_recent_videos_recyclerview, pendingTaskViewAdapter);
    initRecyclerView(R.id.my_rejected_videos_recyclerview, rejectedTaskViewAdapter);
    initRecyclerView(R.id.my_approved_videos_recyclerview, approvedTaskViewAdapter);
  }

  private void initRecyclerView(int recylcerViewId, TaskViewAdapter adapter) {
    RecyclerView recyclerView = viewContainer.findViewById(recylcerViewId);
    recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(),
        SPAN_COUNT));
    recyclerView.setAdapter(adapter);
  }

  @Override
  public void onResume() {
    myVideoViewModel.getProfile();
    super.onResume();
  }

  private void initProfileView() {
    myVideoViewModel
        .getProfileResponseLiveData()
        .observe(
            this,
            profileResponse -> {
              if (profileResponse == null) {
                ((TextView) viewContainer.findViewById(R.id.my_task_profile_title_textview))
                    .setText(getString(R.string.label_loading));
                return;
              }
              if (profileResponse.isSuccessful()
                  && profileResponse.body() != null
                  && profileResponse.body().getCode() == 0) {
                ProfileResponse.DataBean data = profileResponse.body().getData();
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
                return;
              }
              ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_request_fail));
            });
  }

  private void initVideoListView() {
    Log.i(TAG, "rejected video count");
    initVideoListView(PersonalVideoStatus.REJECTED,
        R.id.my_rejected_video_count_textview, R.string.label_rejected_video_count);
    Log.i(TAG, "pending_video_count");
    initVideoListView(PersonalVideoStatus.PENDING_APPROVAL,
        R.id.my_pending_video_count_textview, R.string.label_pending_video_count);
    Log.i(TAG, "accepted_video_count");
    initVideoListView(PersonalVideoStatus.APPROVED,
        R.id.my_accepted_video_count_textview, R.string.label_accepted_video_count);
  }

  private void initVideoListView(PersonalVideoStatus videoStatus,
      int counterTextViewId, int counterLabelId) {
    myVideoViewModel
        .getPersonalVideoListMutableLiveData(videoStatus)
        .observe(
            this,
            videoListResponse -> {
              handleVideoListResponse(videoListResponse, videoListResponseData -> {
                ((TextView) viewContainer.findViewById(counterTextViewId))
                    .setText(String.format(getString(counterLabelId),
                        videoListResponseData.getDataBeanList().getTotal()));
                Context context = getActivity().getApplicationContext();
                VideoListResponse.DataBeanList datalist = videoListResponseData.getDataBeanList();
                Log.i(TAG, "video list data " + videoStatus + datalist.getData().size());
                switch (videoStatus) {
                  case REJECTED:
                    rejectedTaskViewAdapter.setVideoList(context, datalist,
                        TaskType.REJECTED_RECORDING);
                    break;
                  case APPROVED:
                    approvedTaskViewAdapter.setVideoList(context, datalist,
                        TaskType.ACCEPTED_RECORDING);
                    break;
                  case PENDING_APPROVAL:
                    pendingTaskViewAdapter.setVideoList(context, datalist,
                      TaskType.PENDING_RECORDING);
                    break;
                }
              });
            });
  }

  private void handleVideoListResponse(
      Response<VideoListResponse> response, VideoListResponseCallBack videoListResponseCallBack) {
    if (response == null) {
      ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_connect_fail));
      return;
    }
    if (response.isSuccessful() && response.body() != null && response.body().getCode() == 0) {
      videoListResponseCallBack.onSuccess(response.body());
      return;
    }
    ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_request_fail));
  }

  /*
   * VideoListResponseCallBack is called when VideoListResponse body is success.*/
  private interface VideoListResponseCallBack {
    void onSuccess(VideoListResponse videoListResponse);
  }

}
