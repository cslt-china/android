package com.google.android.apps.signalong;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.TimerUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import java.util.HashMap;
import java.util.Map;

public class ViewMyVideoActivity extends BaseActivity {
  private static final String TAG = "ViewMyVideoActivity";

  // The intent param to pass video databean from caller activity.
  public static final String VIDEO_DATA_PARAM = "my_video_param";

  private static final Map<String, Integer> GLOSS_STATUS_LABEL_ID_MAP = new HashMap<>();
  static {
    GLOSS_STATUS_LABEL_ID_MAP.put("APPROVED", R.string.label_gloss_status_approved);
    GLOSS_STATUS_LABEL_ID_MAP.put("PENDING_APPROVAL", R.string.label_gloss_status_pending_approval);
    GLOSS_STATUS_LABEL_ID_MAP.put("REJECTED", R.string.label_gloss_status_rejected);
  }

  private VideoListResponse.DataBeanList.DataBean myVideoData;
  private VideoViewFragment videoView;

  @Override
  public int getContentView() { return R.layout.activity_view_my_video; }

  @Override
  public void init() {}

  @Override
  public void initViews() {
    myVideoData = getIntent().getParcelableExtra(ViewMyVideoActivity.VIDEO_DATA_PARAM);
    if (myVideoData == null) {
      ToastUtils.show(getApplicationContext(), "Cannot parse video data from intent!!");
      onBackPressed();
      return;
    }

    ((TextView) findViewById(R.id.gloss_text_textview)).setText(myVideoData.getGlossText());
    ((TextView) findViewById(R.id.gloss_creation_time_textview))
        .setText(TimerUtils.convertTimestamp(myVideoData.getCreatedTime()));
    ((TextView) findViewById(R.id.gloss_status_textview))
        .setText(getString(GLOSS_STATUS_LABEL_ID_MAP.get(myVideoData.getStatus())));
    ((TextView) findViewById(R.id.gloss_approved_counter_textview))
        .setText(String.valueOf(myVideoData.getApprovedReviewCounter()));
    ((TextView) findViewById(R.id.gloss_rejected_counter_textview))
        .setText(String.valueOf(myVideoData.getRejectedReviewCounter()));


    videoView = (VideoViewFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_my_video_view);
    videoView.setVideoViewCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
      }
    });
    videoView.viewVideo(Uri.parse(myVideoData.getVideoPath()),
                        Uri.parse(myVideoData.getThumbnail()));
  }

  public static void startActivity(
      Activity sourceActivity, VideoListResponse.DataBeanList.DataBean myVideoData) {
    Intent intent = new Intent(sourceActivity.getApplicationContext(),
        ViewMyVideoActivity.class);

    intent.putExtra(VIDEO_DATA_PARAM, myVideoData);
    sourceActivity.startActivity(intent);
  }

}
