package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.google.android.apps.signalong.MyVideoViewModel.PersonalVideoStatus;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Response;

/**
 * MyVideoActivity is be used to display three data list such as pending approval video and approved
 * video and rejected video.
 */
public class MyVideoActivity extends BaseActivity {
  private static final int SPAN_COUNT = 4;

  private static final ImmutableMap<PersonalVideoStatus, Integer> LABEL_STRING_MAP =
      ImmutableMap.of(
          PersonalVideoStatus.ALL,
          R.string.label_personal_video_count,
          PersonalVideoStatus.APPROVED,
          R.string.label_personal_approved_video_count);
  private Map<PersonalVideoStatus, VideoGridAdapter> videoAdapterMap;
  private ImmutableMap<PersonalVideoStatus, RecyclerView> recyclerViewMap;
  private ImmutableMap<PersonalVideoStatus, TextView> textViewMap;
  private MyVideoViewModel myVideoViewModel;

  @Override
  public int getContentView() {
    return R.layout.activity_my_video;
  }

  @Override
  public void init() {
    myVideoViewModel = ViewModelProviders.of(this).get(MyVideoViewModel.class);
  }

  @Override
  public void initViews() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(view -> finish());
    findViewById(R.id.setting_button)
        .setOnClickListener(
            view -> {
              // TODO(zhichongh): Fill jump to the setting activity handler here.
            });
    recyclerViewMap =
        ImmutableMap.of(
            PersonalVideoStatus.APPROVED,
            (RecyclerView) findViewById(R.id.approved_video_recycler_view),
            PersonalVideoStatus.PENDING_APPROVAL,
            (RecyclerView) findViewById(R.id.pending_approval_video_recycler_view),
            PersonalVideoStatus.REJECTED,
            (RecyclerView) findViewById(R.id.rejected_video_recycler_view));
    textViewMap =
        ImmutableMap.of(
            PersonalVideoStatus.ALL, (TextView) findViewById(R.id.personal_video_count_textview),
            PersonalVideoStatus.APPROVED,
                (TextView) findViewById(R.id.personal_approved_video_count_textview));
    videoAdapterMap = new HashMap<>();
    for (PersonalVideoStatus personalVideoStatus : recyclerViewMap.keySet()) {
      videoAdapterMap.put(personalVideoStatus, new VideoGridAdapter());
      initRecyclerView(personalVideoStatus);
      initVideoData(personalVideoStatus);
    }
    initVideoCount(PersonalVideoStatus.ALL);
    initVideoCount(PersonalVideoStatus.APPROVED);
  }

  private void initRecyclerView(PersonalVideoStatus personalVideoStatus) {
    recyclerViewMap
        .get(personalVideoStatus)
        .setLayoutManager(new GridLayoutManager(getApplicationContext(), SPAN_COUNT));
    videoAdapterMap
        .get(personalVideoStatus)
        .setItemListener(
            videoData -> {
              // TODO(zhichongh): Fill jump to the video activity handler here.
            });
    recyclerViewMap.get(personalVideoStatus).setAdapter(videoAdapterMap.get(personalVideoStatus));
  }

  private void initVideoData(PersonalVideoStatus videoStatus) {
    myVideoViewModel
        .getPersonalVideoList(videoStatus)
        .observe(
            this,
            videoListResponse -> {
              handleVideoListResponse(
                  videoListResponse,
                  videoListData -> {
                    videoAdapterMap
                        .get(videoStatus)
                        .addItems(videoListData.getDataBeanList().getData());
                  });
            });
  }

  private void initVideoCount(PersonalVideoStatus personalVideoStatus) {
    myVideoViewModel
        .getPersonalVideoList(personalVideoStatus)
        .observe(
            this,
            videoListResponse -> {
              handleVideoListResponse(
                  videoListResponse,
                  videoListData -> {
                    textViewMap
                        .get(personalVideoStatus)
                        .setText(
                            String.format(
                                getString(LABEL_STRING_MAP.get(personalVideoStatus)),
                                videoListData.getDataBeanList().getTotal()));
                  });
            });
  }

  private void handleVideoListResponse(
      Response<VideoListResponse> response, VideoListResponseCallBack videoListResponseCallBack) {
    if (response == null) {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
      return;
    }
    if (response.isSuccessful() && response.body() != null && response.body().getCode() == 0) {
      videoListResponseCallBack.onSuccess(response.body());
      return;
    }
    ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
  }

  /*
   * VideoListResponseCallBack is called when VideoListResponse body is success.*/
  private interface VideoListResponseCallBack {
    void onSuccess(VideoListResponse videoListResponse);
  }
}
