package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.ToastUtils;
import java.util.List;

/** VideoReviewActivity implements review video by user. */
public class VideoReviewActivity extends BaseActivity {
  private static final String TAG = "[VideoReviewActivity]";
  /* Review approve for video.*/
  private static final String REVIEW_APPROVE = "approve";
  /* Review reject for video.*/
  private static final String REVIEW_REJECT = "reject";
  private VideoReviewViewModel videoReviewViewModel;
  private List<VideoListResponse.DataBeanList.DataBean> unreviewedVideoList;
  private VideoListResponse.DataBeanList.DataBean currentUnreviewedVideoData;
  private RelativeLayout approveRejectButtonsLayout;
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
    approveRejectButtonsLayout = (RelativeLayout) findViewById(R.id.approve_reject_buttons_layout);
    videoView = (VideoViewFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_video_view);
    videoView.setVideoViewCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        approveRejectButtonsLayout.setVisibility(View.VISIBLE);
      }
    });
    findViewById(R.id.ok_button).setOnClickListener(view -> finish());
    findViewById(R.id.review_result_reject_button).setOnClickListener(
        view -> reviewVideo(REVIEW_REJECT));
    findViewById(R.id.review_result_approve_button).setOnClickListener(
        view -> reviewVideo(REVIEW_APPROVE));
    videoReviewViewModel
        .getUnreviewedVideoResponseLiveData()
        .observe(
            this,
            unreviewedResponse -> {
              if (unreviewedResponse == null) {
                finish();
                return;
              }
              if (unreviewedResponse.isSuccessful() && unreviewedResponse.body().getCode() == 0) {
                unreviewedVideoList = unreviewedResponse.body().getDataBeanList().getData();
                if (unreviewedVideoList != null && !unreviewedVideoList.isEmpty()) {
                  nextUnreviewedVideoData();
                } else {
                  approveRejectButtonsLayout.setVisibility(View.GONE);
                  findViewById(R.id.finish_layout).setVisibility(View.VISIBLE);
                }
                return;
              }
              ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
            });
    videoReviewViewModel
        .getReviewVideoResponseLiveData()
        .observe(
            this,
            reviewVideoResponse -> {
              if (reviewVideoResponse == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                finish();
                return;
              }
              if (reviewVideoResponse.isSuccessful() && reviewVideoResponse.body().getCode() == 0) {
                unreviewedVideoList.remove(currentUnreviewedVideoData);
                nextUnreviewedVideoData();
                return;
              }
              Log.e(TAG, "Failed fetching review list.");
              ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
            });
    getUnreviewedVideoDataFromIntent();
  }

  private void reviewVideo(String status) {
    videoReviewViewModel.reviewVideo(currentUnreviewedVideoData.getUuid(), status);
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
      videoView.viewVideo(currentUnreviewedVideoData.getVideoPath());
      approveRejectButtonsLayout.setVisibility(View.INVISIBLE);
    } else {
      videoReviewViewModel.getUnreviewedVideoList();
    }
  }

  private void getUnreviewedVideoDataFromIntent() {
    unreviewedVideoList = getIntent().getParcelableArrayListExtra(SignAlongActivity.UNREVIEW_PARAM);
    if (unreviewedVideoList != null && !unreviewedVideoList.isEmpty()) {
      nextUnreviewedVideoData();
    } else {
      videoReviewViewModel.getUnreviewedVideoList();
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
