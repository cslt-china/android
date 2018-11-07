package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.ReviewVideoResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import java.util.List;
import retrofit2.Response;

/** VideoReviewActivity implements review video by user. */
public class VideoReviewActivity extends BaseActivity implements
    VideoReviewViewModel.UnreviewedVideoListResponseCallbacks,
    VideoReviewViewModel.ReviewVideoResponseCallbacks,
    VideoViewFragment.VideoPlayCompletionCallback {
  private static final String TAG = "[VideoReviewActivity]";

  /* This provides parameters for communication between activities for videoReviewActivity. */
  public static final String REVIEW_TASK_PARAM = "review_param";

  /* Review approve for video.*/
  private static final String REVIEW_APPROVE = "approve";
  /* Review reject for video.*/
  private static final String REVIEW_REJECT = "reject";
  private VideoReviewViewModel videoReviewViewModel;
  private List<VideoListResponse.DataBeanList.DataBean> unreviewedVideoList;
  private VideoListResponse.DataBeanList.DataBean currentUnreviewedVideoData;
  private Button approvalButton;
  private Button rejectButton;
  private Integer counter;
  private VideoViewFragment videoView;

  @Override
  public int getContentView() {
    return R.layout.activity_video_review;
  }

  @Override
  public void init() {
    counter = 0;
    videoReviewViewModel = ViewModelProviders.of(this).get(VideoReviewViewModel.class);
  }

  @Override
  public void onBackPressed() {
    showDoneCountTask();
    super.onBackPressed();
  }

  @Override
  public void initViews() {
    videoView = (VideoViewFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_video_view);
    videoView.setVideoViewCompletionListener(this);
    findViewById(R.id.ok_button).setOnClickListener(view -> finish());
    rejectButton = findViewById(R.id.review_result_reject_button);
    rejectButton.setOnClickListener(
        view -> videoReviewViewModel.reviewVideo(
            currentUnreviewedVideoData.getUuid(), REVIEW_REJECT, this));
    approvalButton = findViewById(R.id.review_result_approve_button);
    approvalButton.setOnClickListener(
        view -> videoReviewViewModel.reviewVideo(
            currentUnreviewedVideoData.getUuid(),REVIEW_APPROVE, this));
    enableReviewButtons(false);
    unreviewedVideoList = ActivityUtils.parseReviewTaskFromIntent(this);
    if (unreviewedVideoList == null || unreviewedVideoList.isEmpty()) {
      Log.i(TAG, "fetch unreviewed video list from server");
      videoReviewViewModel.getUnreviewedVideoList(this);
    } else {
      nextUnreviewedVideoData();
    }
  }

  public void onVideoPlayCompletion() {
    enableReviewButtons(true);
  }

  public void onSuccessUnreviewedVideoListResponse(Response<VideoListResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0) {
      unreviewedVideoList = response.body().getDataBeanList().getData();
      if (unreviewedVideoList != null && !unreviewedVideoList.isEmpty()) {
        nextUnreviewedVideoData();
      } else {
        enableReviewButtons(false);
        findViewById(R.id.finish_layout).setVisibility(View.VISIBLE);
      }
    } else {
      ToastUtils.show(getApplicationContext(), "Cannot download unreviewed video list!!!");
    }
  }

  public void onSuccessReviewVideoResponse(Response<ReviewVideoResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0 && unreviewedVideoList != null) {
      unreviewedVideoList.remove(0);
      nextUnreviewedVideoData();
    } else {
      ToastUtils.show(getApplicationContext(), "Cannot upload review result!!!");
    }
  }

  public void onFailureResponse(Throwable t) {
    Log.e(TAG, String.valueOf(t));
    ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
    finish();
  }

  private void enableReviewButtons(boolean enable) {
    approvalButton.setEnabled(enable);
    rejectButton.setEnabled(enable);
  }

  private void nextUnreviewedVideoData() {
    if (!unreviewedVideoList.isEmpty()) {
      counter++;
      currentUnreviewedVideoData = unreviewedVideoList.get(0);
      ((TextView) findViewById(R.id.task_number_textview))
          .setText(String.format(getString(R.string.label_task_number), counter));
      ((TextView) findViewById(R.id.review_word_textview))
          .setText(
              String.format(
                  getString(R.string.label_review_word_prompt),
                  currentUnreviewedVideoData.getGlossText()));
      enableReviewButtons(false);
      videoView.viewVideo(Uri.parse(currentUnreviewedVideoData.getVideoPath()),
                          Uri.parse(currentUnreviewedVideoData.getThumbnail()));
    } else {
      videoReviewViewModel.getUnreviewedVideoList(this);
    }
  }

  private void showDoneCountTask() {
    if (counter - 1 > 0) {
      ToastUtils.show(
          getApplicationContext(),
          String.format(getString(R.string.tip_finish_count_task), counter - 1));
    } else {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_no_task));
    }
  }

}
