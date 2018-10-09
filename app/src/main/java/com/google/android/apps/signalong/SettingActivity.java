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

/** SettingActivity is be used to set multiple information by user. */
public class SettingActivity extends BaseActivity {
  public static final Integer LOGOUT_SUCCESS = 4;
  public static final Integer LOGOUT_FAIL = 5;
  private static final String TAG = "SettingActivity";
  private SettingViewModel settingViewModel;
  private HelpDialog helpDialog;

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
    if (timingType == TimingType.RECORD_TIME_SCALE) {
      textView.setText(String.format("%d%%", value));
    } else {
      textView.setText(String.format(getString(R.string.label_time), value));
    }
  }

  private void initSeekBar(SeekBar seekBar, TextView textView, TimingType timingType) {

    int value = VideoRecordingSharedPreferences.getTiming(getApplicationContext(), timingType);
    int progress = value / (timingType == TimingType.RECORD_TIME_SCALE ? 10 : 1);

    setTextView(textView, timingType, value);
    seekBar.setProgress(progress);

    seekBar.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int value = progress * (timingType == TimingType.RECORD_TIME_SCALE ? 10 : 1);

            setTextView(textView, timingType, value);
            VideoRecordingSharedPreferences.saveTiming(getApplicationContext(), timingType, value);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });
  }
}
