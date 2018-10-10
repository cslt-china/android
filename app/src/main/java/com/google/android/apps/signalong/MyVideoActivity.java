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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Text;
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

  private ImmutableList<VideoListEntity> videoListEntities;

  private MyVideoViewModel myVideoViewModel;
  /**
   * VideoListEntity is an internal wrapper class to pack all view related objects of
   * a specific video list.
   */
  private class VideoListEntity {
    private PersonalVideoStatus videoStatus;
    private int videoCount;
    private String titleLabelTemplate;
    private RecyclerView recyclerView;
    private TextView videoListTitleTextView;
    private VideoGridAdapter videoGridAdapter;

    /**
     * The constructor of a VideoListEntity object.
     * @param status specifies the PersonalVideoStatus of this instance
     * @param titleLabelStringId specifies the object ID of the title label string pattern.
     * @param titleTextViewId specifies the object ID of the title TextView object. Its text value
     *        depends on the above title label pattern and the actual video list count from server.
     * @param recyclerViewId specifies the object ID of the RecyclerView object. It is the container
     *        of the videos fetched from server.
     */
    VideoListEntity(
        PersonalVideoStatus status,
        int titleLabelStringId,
        int titleTextViewId,
        int recyclerViewId) {
      videoStatus = status;
      videoCount  = 0;
      titleLabelTemplate = getString(titleLabelStringId);
      videoListTitleTextView = (TextView) findViewById(titleTextViewId);
      recyclerView = (RecyclerView) findViewById(recyclerViewId);
      videoGridAdapter = new VideoGridAdapter();
      recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), SPAN_COUNT));
      recyclerView.setAdapter(videoGridAdapter);
    }

    public PersonalVideoStatus getVideoStatus() {
      return videoStatus;
    }

    /**
     * Update the views according the video list response fetched from server.
     * @param videoListResponse
     */
    public void updateView(VideoListResponse videoListResponse) {
      this.videoCount = videoListResponse.getDataBeanList().getTotal();
      videoListTitleTextView.setText(String.format(titleLabelTemplate, videoCount));
      videoGridAdapter.addItems(videoListResponse.getDataBeanList().getData());
    }
  }

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
    initProfileView();
    initVideoListSummary();
    initVideoListEntites();
  }

  @Override
  protected void onResume() {
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

  private void initVideoListSummary(){
    initVideoListSummaryPerStatus(PersonalVideoStatus.ALL,
        (TextView) findViewById(R.id.personal_video_count_text_view));
    initVideoListSummaryPerStatus(PersonalVideoStatus.APPROVED,
        (TextView) findViewById(R.id.personal_approved_video_count_text_view));

  }

  private void initVideoListSummaryPerStatus(
      PersonalVideoStatus personalVideoStatus, TextView videoListSummaryPerStatusTextView) {
    myVideoViewModel
        .getPersonalVideoList(personalVideoStatus)
        .observe(
            this,
            videoListResponse -> {
              handleVideoListResponse(
                  videoListResponse,
                  videoListData -> {
                    videoListSummaryPerStatusTextView
                        .setText(
                            String.format(
                                getString(LABEL_STRING_MAP.get(personalVideoStatus)),
                                videoListData.getDataBeanList().getTotal()));
                  });
            });
  }

  private void initVideoListEntites() {
    videoListEntities =  ImmutableList.of(
        new VideoListEntity(PersonalVideoStatus.APPROVED,
            R.string.label_personal_approved,
            R.id.approved_video_text_view,
            R.id.approved_video_recycler_view),
        new VideoListEntity(PersonalVideoStatus.PENDING_APPROVAL,
            R.string.label_personal_pending_approval,
            R.id.pending_approval_video_text_view,
            R.id.pending_approval_video_recycler_view),
        new VideoListEntity(PersonalVideoStatus.REJECTED,
            R.string.label_personal_rejected,
            R.id.rejected_video_text_view,
            R.id.rejected_video_recycler_view));
    for(VideoListEntity entity : videoListEntities) {
      myVideoViewModel
          .getPersonalVideoList(entity.getVideoStatus())
          .observe(
              this,
              videoListResponse -> {
                handleVideoListResponse(videoListResponse, videoListResponseData -> {
                  entity.updateView(videoListResponseData);
                });
              });
    }
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
