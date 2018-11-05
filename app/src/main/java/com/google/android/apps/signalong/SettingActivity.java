package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
import com.google.android.apps.signalong.jsonentities.ProfileResponse.DataBean.ScoresBean;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;
import com.google.android.apps.signalong.widget.AbortUploadingDialog;
import com.google.android.apps.signalong.widget.HelpDialog;
import com.google.common.collect.ImmutableMap;
import retrofit2.Response;

/** SettingActivity is be used to set multiple information by user. */
public class SettingActivity extends BaseActivity implements
    SettingViewModel.PersonalProfileResponseCallbacks {
  public static final Integer LOGOUT_SUCCESS = 4;
  public static final Integer LOGOUT_FAIL = 5;
  private static final String TAG = "SettingActivity";
  private SettingViewModel settingViewModel;
  private HelpDialog helpDialog;

  private static final ImmutableMap<TimingType, Integer> TIMING_SETTING_MIN_VALUE = ImmutableMap.of(
    TimingType.RECORD_TIME_SCALE, 50,
    TimingType.PREPARE_TIME, 1);

  private static final ImmutableMap<TimingType, Integer> TIMING_SETTING_LABEL_IDS = ImmutableMap.of(
      TimingType.RECORD_TIME_SCALE, R.string.label_setting_recording_scale,
      TimingType.PREPARE_TIME, R.string.label_setting_prepare_time);

  @Override
  public int getContentView() {
    return R.layout.activity_setting;
  }

  @Override
  public void onBackPressed() {
    this.setResult(LOGOUT_FAIL);
    super.onBackPressed();
  }

  @Override
  public void init() {
    settingViewModel = ViewModelProviders.of(this).get(SettingViewModel.class);
    helpDialog = new HelpDialog(view -> helpDialog.dismiss());
  }

  @Override
  public void initViews() {
    settingViewModel.getProfile(this);

    initSeekBar(
      findViewById(R.id.prepare_time_seek_bar),
      findViewById(R.id.textview_prepare_time),
      TimingType.PREPARE_TIME);
    initSeekBar(
      findViewById(R.id.record_time_seek_bar),
      findViewById(R.id.textview_record_time),
      TimingType.RECORD_TIME_SCALE);

    findViewById(R.id.logout_button).setOnClickListener(view -> {
      CameraViewModel cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);

      AbortUploadingDialog.check(this, getString(R.string.enforce_exit),
          getString(R.string.cancel),
          cameraViewModel,
          ()->logout(), null);
    });

    findViewById(R.id.help_button)
        .setOnClickListener(
            view -> {
              showHelpDialog();
            });
    findViewById(R.id.change_password_button)
      .setOnClickListener(
        view -> startActivityForResult(new Intent(getApplicationContext(), ChangePasswordActivity.class), 0)
      );
  }

  private void logout() {
    settingViewModel.logOut().observe( this,
        logOut -> {
          if (logOut) {
            this.setResult(LOGOUT_SUCCESS);
            finish();
          }
        });
  }

  private void showHelpDialog() {
    helpDialog.show(getSupportFragmentManager(), HelpDialog.class.getSimpleName());
  }

  private void setTextView(TextView textView, TimingType timingType, int value) {
    textView.setText(String.format(getString(TIMING_SETTING_LABEL_IDS.get(timingType)), value));
  }

  private void initSeekBar(SeekBar seekBar, TextView textView, TimingType timingType) {
    int progress = VideoRecordingSharedPreferences.getTiming(getApplicationContext(), timingType);

    setTextView(textView, timingType, progress);
    // Note that SeekBar min value is always 0, so here we must offset the progress by
    // the corresponding min value.
    seekBar.setProgress(progress - TIMING_SETTING_MIN_VALUE.get(timingType));
    seekBar.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Here we restore the value from progress by adding up the minimum value.
            int value = progress + TIMING_SETTING_MIN_VALUE.get(timingType);
            setTextView(textView, timingType, value);
            VideoRecordingSharedPreferences.saveTiming(
                getApplicationContext(), timingType, value);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });
  }

  public void onSuccessPersonalProfileResponse(Response<ProfileResponse> response) {
    if (response == null || !response.isSuccessful()) {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
      return;
    }
    if (response.body() == null ) {
      ((TextView) findViewById(R.id.my_profile_title_textview))
          .setText(getString(R.string.label_loading));
      return;
    }
    if (response.body().getCode() != 0) {
      return;
    }

    ProfileResponse.DataBean data = response.body().getData();
    ((TextView) findViewById(R.id.my_profile_title_textview))
        .setText(String.format(getString(R.string.label_my_profile_title),
            data.getUsername()));
    ScoresBean scoresBean = data.getScores();
    ((TextView) findViewById(R.id.my_total_score_textview))
        .setText(String.valueOf(data.getScores().getTotalScore()));
    ((TextView) findViewById(R.id.my_recording_score_textview))
        .setText(
            String.format(getString(R.string.label_my_recording_score),
                scoresBean.getTotalScore() - scoresBean.getVideoReviewScore()));
    ((TextView) findViewById(R.id.my_review_score_textview))
        .setText(String.format(getString(R.string.label_my_review_score),
            scoresBean.getVideoReviewScore()));
  }

  public void onFailureResponse(Throwable t) {
    Log.e(TAG, String.valueOf(t));
  }

}
