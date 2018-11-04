package com.google.android.apps.signalong;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.TimerUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.RecordedDataView;
import com.google.android.apps.signalong.widget.TaskView.TaskType;
import java.security.InvalidParameterException;


public class ViewMyVideoActivity extends BaseActivity {
  private static final String TAG = "ViewMyVideoActivity";

  // The intent param to pass video databean from caller activity.
  public static final String VIDEO_DATA_PARAM = "my_video_param";

  private static final TaskType TASK_TYPE = TaskType.MY_RECORDING;

  private VideoListResponse.DataBeanList.DataBean data;
  private VideoViewFragment videoView;

  @Override
  public int getContentView() { return R.layout.activity_view_my_video; }

  @Override
  public void init() {}

  @Override
  public void initViews() {
    data = getIntent().getParcelableExtra(ViewMyVideoActivity.VIDEO_DATA_PARAM);
    if (data == null) {
      ToastUtils.show(getApplicationContext(), "Cannot parse video data from intent!!");
      onBackPressed();
      return;
    }

    videoView = (VideoViewFragment) getSupportFragmentManager().findFragmentById(
        R.id.fragment_my_video_view);
    videoView.setVideoViewCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
      }
    });
    videoView.viewVideo(Uri.parse(data.getVideoPath()),
        Uri.parse(data.getThumbnail()));

    // TODO(jxue): Remove and reuse the replicated code from RecordedDataView.
    ((TextView) findViewById(R.id.gloss_text_textview)).setText(data.getGlossText());
    ((TextView) findViewById(R.id.gloss_creation_time_textview))
        .setText(TimerUtils.convertTimestamp(data.getCreatedTime()));
    ((TextView) findViewById(R.id.gloss_status_textview))
        .setText(getResources().getText(
            RecordedDataView.GLOSS_STATUS_LABEL_ID_MAP.get(data.getStatus())));
    ((TextView) findViewById(R.id.gloss_approved_counter_textview))
        .setText(String.valueOf(data.getApprovedReviewCounter()));
    ((TextView) findViewById(R.id.gloss_rejected_counter_textview))
        .setText(String.valueOf(data.getRejectedReviewCounter()));

    CardView card = findViewById(R.id.recorded_data_cardview);
    switch (data.getStatus()) {
      case "APPROVED":
        card.setCardBackgroundColor(getResources().getColor(R.color.green, null));
        break;
      case "PENDING_APPROVAL":
        card.setCardBackgroundColor(getResources().getColor(R.color.yellow, null));
        break;
      case "REJECTED":
        card.setCardBackgroundColor(getResources().getColor(R.color.red, null));
        break;
      default:
        throw new InvalidParameterException(
            "cannot init my video in the status of " + data.getStatus());
    }

  }

  public static void startActivity(
      Activity sourceActivity, VideoListResponse.DataBeanList.DataBean myVideoData) {
    Intent intent = new Intent(sourceActivity.getApplicationContext(),
        ViewMyVideoActivity.class);

    intent.putExtra(VIDEO_DATA_PARAM, myVideoData);
    sourceActivity.startActivity(intent);
  }

}
