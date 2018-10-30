package com.google.android.apps.signalong;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.utils.TimerUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

public class ViewMyVideoActivity extends BaseActivity {
  private static final String TAG = "ViewMyVideoActivity";

  // The intent param to pass video databean from caller activity.
  public static final String VIDEO_DATA_PARAM = "my_video_param";

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
