package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
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
    initSeekBar(
        (SeekBar) findViewById(R.id.prepare_time_seek_bar),
        (TextView) findViewById(R.id.textview_prepare_time),
        TimingType.PREPARE_TIME);
    initSeekBar(
        (SeekBar) findViewById(R.id.record_time_seek_bar),
        (TextView) findViewById(R.id.textview_record_time),
        TimingType.RECORD_TIME);
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
  }

  private void showHelpDialog() {
    helpDialog.show(getSupportFragmentManager(), HelpDialog.class.getSimpleName());
  }

  private void initSeekBar(SeekBar seekBar, TextView textView, TimingType timingType) {

    int time = VideoRecordingSharedPreferences.getTiming(getApplicationContext(), timingType);
    textView.setText(String.format(getString(R.string.label_time), time));
    seekBar.setProgress(time);
    seekBar.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            textView.setText(String.format(getString(R.string.label_time), progress));
            VideoRecordingSharedPreferences.saveTiming(
                getApplicationContext(), timingType, progress);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });
  }
}
