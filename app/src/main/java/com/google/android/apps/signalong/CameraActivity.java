package com.google.android.apps.signalong;

import static com.google.android.apps.signalong.CameraActivity.RecordState.PREVIEW;
import static com.google.android.apps.signalong.CameraActivity.RecordState.RECORDING;

import android.arch.lifecycle.ViewModelProviders;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.apps.signalong.broadcast.NetworkReceiver;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;
import com.google.android.apps.signalong.widget.CameraView;
import com.google.android.apps.signalong.widget.CameraView.CallBack;
import com.google.android.apps.signalong.widget.LearnVideoDialog;
import com.google.android.apps.signalong.widget.LearnVideoDialog.DialogListener;
import com.google.android.apps.signalong.widget.RecordButton;
import com.google.android.apps.signalong.widget.RecordButton.RecordListener;
import java.io.File;
import java.util.List;

/**
 * CameraActivity implements video recording activity, reference code link
 * https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/Camera2VideoFragment.java.
 */
public class CameraActivity extends BaseActivity {

  /** The state of RecordState that this object specifies. */
  public enum RecordState {
    RECORDING,
    PREVIEW
  }

  private static final String TAG = "CameraActivity";
  private static final int LARGE_WORD_PROMPT_WAIT_TIME = 1500;
  /* Suffix of video file.*/
  private static final String VIDEO_SUFFIX = ".mp4";
  private CameraView cameraView;
  private TextView countDownTextView;
  private TextView wordTextView;
  private TextView counterTextView;
  private TextView largeWordTextView;
  private RecordButton recordStartButton;
  private ViewGroup largeWordPromptLayout;
  private ViewGroup realRecordLayout;
  private ImageButton refreshButton;
  private NetworkReceiver networkReceiver;
  private String videoFilePath;
  private CameraViewModel cameraViewModel;
  private List<SignPromptBatchResponse.DataBean> signPromptList;
  private SignPromptBatchResponse.DataBean currentSignPrompt;
  private int timeStart;
  private int counter;
  private RecordState currentState;
  private LearnVideoDialog learnVideoDialog;
  private Thread countDownThread;
  private boolean isCountDownThreadExit;

  @Override
  public int getContentView() {
    return R.layout.activity_camera;
  }

  @Override
  public void init() {
    cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
    counter = 0;
    currentState = PREVIEW;
  }

  @Override
  public void initViews() {
    findViewById(R.id.back_button)
        .setOnClickListener(
            view -> {
              finish();
            });
    learnVideoDialog = new LearnVideoDialog();
    countDownTextView = (TextView) findViewById(R.id.count_down_text_view);
    wordTextView = (TextView) findViewById(R.id.word_text_view);
    cameraView = (CameraView) findViewById(R.id.camera_view);
    counterTextView = (TextView) findViewById(R.id.counter_text_view);
    largeWordTextView = (TextView) findViewById(R.id.large_word_text_view);
    recordStartButton = (RecordButton) findViewById(R.id.record_start_button);
    largeWordPromptLayout = (ViewGroup) findViewById(R.id.large_word_prompt_layout);
    realRecordLayout = (ViewGroup) findViewById(R.id.real_record_layout);
    refreshButton = (ImageButton) findViewById(R.id.refresh_button);
    refreshButton.setOnClickListener(
        view -> {
          cameraView.startPreview();
        });
    recordStartButton
        .setRecordEndListener(
            new RecordListener() {
              @Override
              public void onRecordingEnd() {
                cameraView.stopRecording();
                cameraViewModel.saveVideoUploadTask(videoFilePath, currentSignPrompt.getId());
                signPromptList.remove(currentSignPrompt);
                nextRecordingPrompt();
              }

              @Override
              public void onCancelRecording() {
                cameraView.stopRecording();
                FileUtils.clearFile(videoFilePath);
              }
            });
    cameraView.setCallBack(
        new CallBack() {
          @Override
          public void onCameraOpened() {
            cameraViewModel.getSignPromptBatch();
          }

          @Override
          public void onRecording() {
            currentState = RECORDING;
            runOnUiThread(
                () -> {
                  countDownTextView.setVisibility(View.GONE);
                  recordStartButton.intoRecordingStatus();
                });
          }

          @Override
          public void onPreview() {
            currentState = PREVIEW;
            runOnUiThread(() -> recordStartButton.intoPreviewStatus());
          }

          @Override
          public String onOutVideoPath() {
            return getVideoPath();
          }
        });
    timeStart =
        VideoRecordingSharedPreferences.getTiming(getApplicationContext(), TimingType.PREPARE_TIME);
    cameraViewModel
        .getSignPromptBatchResponseLiveData()
        .observe(
            this,
            signPromptBatchResponse -> {
              if (signPromptBatchResponse == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                finish();
              } else if (signPromptBatchResponse.isSuccessful()
                  && signPromptBatchResponse.body().getCode() == 0
                  && signPromptBatchResponse.body().getData() != null) {
                signPromptList = signPromptBatchResponse.body().getData();
                nextRecordingPrompt();
              } else {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
                finish();
              }
            });
    learnVideoDialog.setDialogListener(
        new DialogListener() {
          @Override
          public void onRerecordClick() {
            refreshButton.setVisibility(View.GONE);
            showLargeWordForTime();
          }

          @Override
          public void onCancelClick() {}

          @Override
          public String getVideoPath() {
            return currentSignPrompt.getSampleVideo().getVideoPath();
          }
        });
    realRecordLayout.setOnClickListener(
        view -> {
          if (currentState == RECORDING) {
            recordStartButton.intoCancelRecordingStatus();
            cameraView.startPreview();
          } else {
            isCountDownThreadExit = true;
          }
          learnVideoDialog.show(getFragmentManager(), LearnVideoDialog.class.getName());
          refreshButton.setVisibility(View.VISIBLE);
        });
    networkReceiver = new NetworkReceiver(() -> cameraViewModel.startUploadThread());
    registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
  }

  @Override
  protected void onDestroy() {
    isCountDownThreadExit = true;
    cameraView.closeCamera();
    cameraViewModel.stopUploadThread();
    if (networkReceiver != null) {
      unregisterReceiver(networkReceiver);
    }
    super.onDestroy();
  }

  private String getVideoPath() {
    videoFilePath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()
            + File.separator
            + System.currentTimeMillis()
            + VIDEO_SUFFIX;
    return videoFilePath;
  }

  /** Start the countdown and display it on the screen. */
  private void startPreviewCountDown() {
    countDownTextView.setVisibility(View.VISIBLE);
    isCountDownThreadExit = false;
    countDownThread =
        new Thread(
            () -> {
              final int[] currentPrepareTime = {timeStart};
              while (0 < currentPrepareTime[0] && !isCountDownThreadExit) {
                try {
                  runOnUiThread(
                      () -> {
                        countDownTextView.setText(String.valueOf(currentPrepareTime[0]--));
                      });
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  Log.d(TAG, e.getMessage());
                }
              }
              if (!isCountDownThreadExit) {
                int scale = VideoRecordingSharedPreferences.getTiming(
                        getApplicationContext(), TimingType.RECORD_TIME_SCALE);

                int recordingTime = (int)Math.ceil(
                    currentSignPrompt.getDuration() / 1000.0 * scale / 100.0);

                recordStartButton.setRecordingDuration(recordingTime);
                runOnUiThread(() -> cameraView.startRecord());
              }
            });

    countDownThread.start();
  }

  private void nextRecordingPrompt() {
    if (signPromptList != null && !signPromptList.isEmpty()) {
      counter++;
      currentSignPrompt = signPromptList.get(0);
      updateViewContent();
      cameraView.startPreview();
      showLargeWordForTime();
    } else {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_finish));
      finish();
    }
  }

  /** Update the contents of the view. */
  private void updateViewContent() {
    if (currentSignPrompt != null) {
    largeWordTextView.setText(currentSignPrompt.getText());
    wordTextView.setText(currentSignPrompt.getText());
    counterTextView.setText(String.format(getString(R.string.label_counter), counter));
    }
  }
  /** Show large word for a period of time. */
  private void showLargeWordForTime() {
    realRecordLayout.setVisibility(View.GONE);
    largeWordPromptLayout.setVisibility(View.VISIBLE);
    new Thread(
            () -> {
              try {
                Thread.sleep(LARGE_WORD_PROMPT_WAIT_TIME);
              } catch (InterruptedException e) {
                Log.d(TAG, e.getMessage());
              }
              runOnUiThread(
                  () -> {
                    largeWordPromptLayout.setVisibility(View.GONE);
                    realRecordLayout.setVisibility(View.VISIBLE);
                    startPreviewCountDown();
                  });
            })
        .start();
  }
}
