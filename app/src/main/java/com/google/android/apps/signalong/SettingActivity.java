package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;
import com.google.android.apps.signalong.widget.HelpDialog;
import com.google.common.collect.ImmutableMap;

/** SettingActivity is be used to set multiple information by user. */
public class SettingActivity extends BaseActivity {
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
    Toolbar toolbar = findViewById(R.id.setting_toolbar);
    setSupportActionBar(toolbar);
    final Drawable upArrow = getResources().getDrawable(R.drawable.back);

    getSupportActionBar().setHomeAsUpIndicator(upArrow);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    toolbar.setNavigationOnClickListener(v -> finish());

    initSeekBar(
      findViewById(R.id.prepare_time_seek_bar),
      findViewById(R.id.textview_prepare_time),
      TimingType.PREPARE_TIME);
    initSeekBar(
      findViewById(R.id.record_time_seek_bar),
      findViewById(R.id.textview_record_time),
      TimingType.RECORD_TIME_SCALE);

    findViewById(R.id.logout_button)
        .setOnClickListener(
            view -> {
              settingViewModel
                  .logOut()
                  .observe(
                      this,
                      logOut -> {
                        if (logOut) {
                          this.setResult(LOGOUT_SUCCESS);
                          finish();
                        }
                      });
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
}
