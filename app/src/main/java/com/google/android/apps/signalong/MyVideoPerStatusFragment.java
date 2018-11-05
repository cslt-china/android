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
import com.google.android.apps.signalong.IntroFragment.FragmentListener;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.VideoStatus;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.RecordedDataView;
import com.google.android.apps.signalong.widget.RecordedDataViewAdapter;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import retrofit2.Response;


public class MyVideoPerStatusFragment extends BaseFragment implements
    MyVideoViewModel.PersonalVideoListResponseCallbacks  {
  private static final String TAG = "MyVideoPerStatusFrag";

  private static final String STATUS_PARAM = "status";
  private static final String TITLE_TEMPLATE_PARAM = "title";

  private static final TaskType TASK_TYPE = TaskType.MY_RECORDING;

  private VideoStatus status;
  private String titleTemplate;
  private int totalVideoCount;
  private FragmentListener listener;

  private View viewContainer;
  private RecordedDataViewAdapter taskViewAdapter;
  private OnClickListener taskViewOnClickListener;
  private MyVideoViewModel myVideoViewModel;
  private TextView totalVideoCountTextView;
  private String totalVideoCountTemplate;

  public static MyVideoPerStatusFragment newInstance(VideoStatus statusParam) {
    MyVideoPerStatusFragment fragment = new MyVideoPerStatusFragment();
    Bundle args = new Bundle();
    args.putInt(STATUS_PARAM, statusParam.ordinal());
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    status = VideoStatus.values()[(int)getArguments().get(STATUS_PARAM)];
    totalVideoCount = 0;
    myVideoViewModel = ViewModelProviders.of(this).get(MyVideoViewModel.class);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_my_video_per_status, container, false);
    taskViewAdapter = new RecordedDataViewAdapter();

    taskViewOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        VideoListResponse.DataBeanList.DataBean data = ((RecordedDataView) v).getData();
        ViewMyVideoActivity.startActivity(getActivity(), data);
      }
    };
    totalVideoCountTextView = viewContainer.findViewById(R.id.total_video_count_textview);
    totalVideoCountTemplate = getResources().getString(R.string.label_video_total_count);
    return viewContainer;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initRecyclerView(R.id.my_videos_per_status_recyclerview, taskViewAdapter);
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

  public void onSuccessPersonalVideoListResponse(
      VideoStatus status, Response<VideoListResponse> response) {
    if (response == null || !response.isSuccessful()) {
      ToastUtils.show(getActivity().getApplicationContext(), getString(R.string.tip_connect_fail));
      Log.i(TAG, "onSuccessPersonalVideoListResponse connection failed " + status);
      return;
    }
    if (response.body() != null && response.body().getCode() == 0) {
      VideoListResponse.DataBeanList dataBeanList = response.body().getDataBeanList();
      totalVideoCount = dataBeanList.getTotal();
      totalVideoCountTextView.setText(String.format(totalVideoCountTemplate, totalVideoCount));
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

  private void initView() {
    taskViewAdapter.initVideolist(getActivity().getApplicationContext(), TASK_TYPE,
        taskViewOnClickListener);
    Log.i(TAG, "initView for status " + status);
    myVideoViewModel.getPersonalVideoList(status, this);
  }

}
