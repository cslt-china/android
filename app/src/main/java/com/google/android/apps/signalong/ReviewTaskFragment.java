package com.google.android.apps.signalong;

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
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.widget.RecordedDataView;
import com.google.android.apps.signalong.widget.RecordedDataViewAdapter;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public class ReviewTaskFragment extends BaseFragment implements
    VideoReviewViewModel.UnreviewedVideoListResponseCallbacks {
  private static final String TAG = "ReviewTaskFragment";

  private static final TaskType TASK_TYPE = TaskType.NEW_REVIEW;

  private View viewContainer;

  private RecordedDataViewAdapter taskViewAdapter;
  private List<DataBean> unreviewedVideoList;
  private VideoReviewViewModel videoReviewViewModel;

  private OnClickListener taskViewOnClickListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    videoReviewViewModel = ViewModelProviders.of(this).get(VideoReviewViewModel.class);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_review_task, container, false);
    return viewContainer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    taskViewAdapter = new RecordedDataViewAdapter();
    taskViewOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        List<DataBean> reviewDataList = new ArrayList<>();
        reviewDataList.add(((RecordedDataView) v).getData());
        ActivityUtils.startReviewActivity(getActivity(), reviewDataList);
      }
    };

    viewContainer.findViewById(R.id.review_video_button)
        .setOnClickListener(
            view -> {
              ActivityUtils.startReviewActivity(getActivity(), unreviewedVideoList);
            }
        );

    RecyclerView recyclerView = (RecyclerView) viewContainer.findViewById(
        R.id.review_task_recyclerview);
    recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 1));
    recyclerView.setAdapter(taskViewAdapter);
  }

  @Override
  public void onResume() {
    initView();
    super.onResume();
  }

  private void initView() {
    videoReviewViewModel.getUnreviewedVideoList(this);
  }

  public void onSuccessUnreviewedVideoListResponse(Response<VideoListResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0) {
      ((TextView) viewContainer.findViewById(R.id.review_task_count_textview))
          .setText(
              String.format(
                  getString(R.string.label_review_task_count),
                  response.body().getDataBeanList().getTotal()));
      this.unreviewedVideoList = response.body().getDataBeanList().getData();
      taskViewAdapter.setVideoList(
          getContext(), response.body().getDataBeanList(), TASK_TYPE, taskViewOnClickListener);
    }
  }


  public void onFailureResponse(Throwable t) {
    Log.e(TAG, String.valueOf(t));
  }

}
