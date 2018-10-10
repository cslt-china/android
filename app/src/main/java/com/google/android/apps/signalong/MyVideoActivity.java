package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import com.google.android.apps.signalong.MyVideoViewModel.PersonalVideoStatus;
import com.google.android.apps.signalong.jsonentities.ProfileResponse.DataBean.ScoresBean;
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
  private ImmutableMap<PersonalVideoStatus, TextView> videoCountTextViewMap;
  private ImmutableMap<PersonalVideoStatus, TextView> emptyVideoListTextViewMap;
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
              startActivity(new Intent(getApplicationContext(), SettingActivity.class));
            });
    emptyVideoListTextViewMap =
        ImmutableMap.of(
            PersonalVideoStatus.APPROVED,
            (TextView) findViewById(R.id.empty_approved_video_text_view),
            PersonalVideoStatus.PENDING_APPROVAL,
            (TextView) findViewById(R.id.empty_pending_approval_video_text_view),
            PersonalVideoStatus.REJECTED,
            (TextView) findViewById(R.id.empty_rejected_video_text_view));
    recyclerViewMap =
        ImmutableMap.of(
            PersonalVideoStatus.APPROVED,
            (RecyclerView) findViewById(R.id.approved_video_recycler_view),
            PersonalVideoStatus.PENDING_APPROVAL,
            (RecyclerView) findViewById(R.id.pending_approval_video_recycler_view),
            PersonalVideoStatus.REJECTED,
            (RecyclerView) findViewById(R.id.rejected_video_recycler_view));
    videoCountTextViewMap =
        ImmutableMap.of(
            PersonalVideoStatus.ALL,
                (TextView) findViewById(R.id.personal_video_count_text_view),
            PersonalVideoStatus.APPROVED,
                (TextView) findViewById(R.id.personal_approved_video_count_text_view));
    videoAdapterMap = new HashMap<>();
    for (PersonalVideoStatus personalVideoStatus : recyclerViewMap.keySet()) {
      videoAdapterMap.put(personalVideoStatus, new VideoGridAdapter());
      initRecyclerView(personalVideoStatus);
      initVideoDataAndEmptyListTextView(personalVideoStatus);
    }
    initVideoCount(PersonalVideoStatus.ALL);
    initVideoCount(PersonalVideoStatus.APPROVED);
    myVideoViewModel
        .getProfileResponseLiveData()
        .observe(
            this,
            profileResponse -> {
              if (profileResponse == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                return;
              }
              if (profileResponse.isSuccessful()
                  && profileResponse.body() != null
                  && profileResponse.body().getCode() == 0) {
                ((TextView) findViewById(R.id.username_text_view))
                    .setText(
                        String.format(
                            getString(R.string.label_current_points),
                            profileResponse.body().getData().getUsername()));
                ScoresBean scoresBean = profileResponse.body().getData().getScores();
                ((TextView) findViewById(R.id.points_text_view))
                    .setText(
                        String.valueOf(
                            scoresBean.getVideoQualityScore()
                                + scoresBean.getGlossCreationScore()
                                + scoresBean.getVideoCreationScore()
                                + scoresBean.getVideoReviewScore()));
                return;
              }
              ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
            });
  }

  @Override
  protected void onResume() {
    myVideoViewModel.getProfile();
    super.onResume();
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

  private void initVideoDataAndEmptyListTextView(PersonalVideoStatus videoStatus) {
    myVideoViewModel
        .getPersonalVideoList(videoStatus)
        .observe(
            this,
            videoListResponse -> {
              handleVideoListResponse(
                  videoListResponse,
                  videoListData -> {
                    emptyVideoListTextViewMap
                        .get(videoStatus)
                        .setVisibility(videoListData.getDataBeanList().getTotal());
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
                    videoCountTextViewMap
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
