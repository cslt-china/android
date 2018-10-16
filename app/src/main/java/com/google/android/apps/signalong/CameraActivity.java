package com.google.android.apps.signalong;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
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

import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.StateMachineLogger;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

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

  static private String fsmTag = "RecordFSM";

  private static final int LARGE_WORD_PROMPT_WAIT_TIME = 1500;
  private static final int DISPLAY_START_TIME = 1000;
  /* Suffix of video file.*/
  private static final String VIDEO_SUFFIX = ".mp4";
  private CameraView cameraView;
  private TextView countDownTextView;
  private TextView wordTextView;
  private TextView counterTextView;
  private TextView largeWordTextView;
  private ViewGroup largeWordPromptLayout;
  private ViewGroup realRecordLayout;
  private NetworkReceiver networkReceiver;
  private String videoFilePath;
  private CameraViewModel cameraViewModel;
  private ProgressBar progressBar;
  private List<SignPromptBatchResponse.DataBean> signPromptList;
  private int currentSignIndex;
  private LearnVideoDialog pausingDialog;
  ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
  ValueAnimator countdownAnimator = new ValueAnimator();
  MyStateMachine fsm;

  @Override
  public int getContentView() {
    return R.layout.activity_camera;
  }

  @Override
  public void init() {
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    currentSignIndex = 0;
    initFSM();

    initModel(()-> {
      networkReceiver = new NetworkReceiver(() -> cameraViewModel.startUploadThread());
      registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
      makeVideoPath();
      startFSM();
    });
  }

  private void fireFsmEvent(FSMEvent event) {
    if (fsm == null || !fsm.isStarted() || fsm.isError()) {
      return;
    }
    fsm.fire(event, this);
  }

  private void terminateFsm() {
    if (fsm == null || !fsm.isStarted()) {
      return;
    }

    fsm.terminate(this);
  }

  @Override
  public void initViews() {
    pausingDialog = new LearnVideoDialog();
    countDownTextView = findViewById(R.id.count_down_text_view);
    progressBar = findViewById(R.id.progres_bar);
    wordTextView = findViewById(R.id.word_text_view);
    cameraView = findViewById(R.id.camera_view);
    counterTextView = findViewById(R.id.counter_text_view);
    largeWordTextView = findViewById(R.id.large_word_text_view);
    largeWordPromptLayout = findViewById(R.id.large_word_prompt_layout);
    realRecordLayout = findViewById(R.id.real_record_layout);

    pausingDialog.setDialogListener(()->{
      fireFsmEvent(FSMEvent.Continue);
    });

    realRecordLayout.setOnClickListener(view -> {
      fireFsmEvent(FSMEvent.Pause);
    });

    cameraView.setCallBack(new CallBack() {
      @Override
      public void onCameraOpened() {
        fillModel();
      }
      @Override
      public void onCameraError() {
        finishWithToastInfo(getString(R.string.open_camera_failed));
      }
    });

    initCountdownAnimation();
    initProgressAnimation();
  }

  StateMachineLogger fsmLogger;
  private void initFSM() {
    StateMachineBuilder<MyStateMachine, FSMState, FSMEvent, CameraActivity> builder =
        StateMachineBuilderFactory.create(MyStateMachine.class,
            FSMState.class, FSMEvent.class, CameraActivity.class);
    fsm = builder.newStateMachine(FSMState.Countdowning);
  }

  private void startFSM() {
    fsm.start(this);
  }

  private void fillModel() {
    cameraViewModel.getSignPromptBatch();
  }

  private void initModel(Runnable onResponseSuccess) {
    cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
    cameraViewModel
        .getSignPromptBatchResponseLiveData()
        .observe( this, signPromptBatchResponse -> {
          if (signPromptBatchResponse == null) {
            finishWithToastInfo(getString(R.string.tip_request_fail));
            return;
          }

          if (!signPromptBatchResponse.isSuccessful()
              || signPromptBatchResponse.body().getCode() != 0) {
            finishWithToastInfo(getString(R.string.tip_request_fail));
            return;
          }

          signPromptList = signPromptBatchResponse.body().getData();

          if (signPromptList == null || signPromptList.isEmpty()) {
            finishWithToastInfo(getString(R.string.no_new_task));
            return;
          }
          onResponseSuccess.run();
        });
  }

  private void initCountdownAnimation() {
    countdownAnimator.addUpdateListener(animator -> {
      long currentTime = animator.getCurrentPlayTime();
      long totalTime = animator.getDuration();
      long leftTime = totalTime - currentTime;

      if (currentTime > LARGE_WORD_PROMPT_WAIT_TIME) {
        largeWordPromptLayout.setVisibility(View.GONE);
        realRecordLayout.setVisibility(View.VISIBLE);

        if (leftTime <= DISPLAY_START_TIME) {
          countDownTextView.setText(getString(R.string.start));
        } else {
          countDownTextView.setText(String.valueOf((leftTime - DISPLAY_START_TIME)/1000 + 1));
          countDownTextView.invalidate();
        }
      }
    });

    countdownAnimator.addListener(new AnimatorListener() {
      @Override
      public void onAnimationStart(Animator animation) {
        largeWordPromptLayout.setVisibility(View.VISIBLE);
        realRecordLayout.setVisibility(View.GONE);
      }
      @Override
      public void onAnimationEnd(Animator animation) {
        fireFsmEvent(FSMEvent.Record);
      }
    });
  }

  private void startCountdownAnimation() {

    int countDownSecond = VideoRecordingSharedPreferences.getTiming(getApplicationContext(),
        TimingType.PREPARE_TIME);
    int totalTime = LARGE_WORD_PROMPT_WAIT_TIME + countDownSecond * 1000 + DISPLAY_START_TIME;
    countdownAnimator.setIntValues(totalTime/100, 0);
    countdownAnimator.setDuration(totalTime);
    countdownAnimator.start();
  }

  private void cancelCountdownAnimation() {
    countdownAnimator.cancel();
  }

  private void initProgressAnimation() {
    progressBar.setMax(100);
    progressAnimator = ValueAnimator.ofInt(0, 100);
    progressAnimator.addUpdateListener(valueAnimator ->
    {
      progressBar.setProgress((int) valueAnimator.getAnimatedValue());
    });

    progressAnimator.addListener(
        new AnimatorListener() {
          @Override
          public void onAnimationEnd(Animator animator) {
            fireFsmEvent(FSMEvent.Next);
          }
        });
  }

  void startProgressAnimation() {
    int scale = VideoRecordingSharedPreferences.getTiming(
        getApplicationContext(), TimingType.RECORD_TIME_SCALE);

    int recordingTime = (int)Math.ceil(
        signPromptList.get(currentSignIndex).getDuration() / 1000.0 * scale / 100.0);

    if (recordingTime < 2) {
      recordingTime = 2;
    }

    progressAnimator.setDuration(recordingTime * 1000);
    progressAnimator.start();
  }

  void cancelProgressAnimation() {
    progressAnimator.cancel();
  }

  @Override
  protected void onDestroy() {
    cameraView.closeCamera();
    cameraViewModel.stopUploadThread();
    if (networkReceiver != null) {
      unregisterReceiver(networkReceiver);
    }
    super.onDestroy();
  }

  private void makeVideoPath() {
    videoFilePath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()
            + File.separator
            + System.currentTimeMillis()
            + VIDEO_SUFFIX;
  }

  private void uploadVideo() {
    cameraViewModel.saveVideoUploadTask(videoFilePath,
        signPromptList.get(currentSignIndex).getId());
  }

  private void finishWithToastInfo(String info) {
    ToastUtils.show(getApplicationContext(), info);
    finish();
  }

  @Override
  protected void onStop() {
    if (currentSignIndex > 0) {
      ToastUtils.show(getApplication(),
          String.format(getString(R.string.thanks),  currentSignIndex));
    }
    super.onStop();
  }

  /** Update the contents of the view. */
  private void updateViewContent() {
    String text = signPromptList.get(currentSignIndex).getText();
    largeWordTextView.setText(text);
    wordTextView.setText(String.format(getString(R.string.please_sign), text));
    counterTextView.setText(String.format(getString(R.string.label_counter), currentSignIndex + 1));
  }

  @Override
  public void onBackPressed() {
    terminateFsm();
    finish();
  }

  enum FSMEvent {
    Next,
    Record,
    Pause,
    Continue,
  }

  enum FSMState {
    Countdowning,
    CountdowningPaused,
    Recording,
    RecordingPaused,
  }


  @Transitions({
      @Transit(from="Countdowning", on="Pause", to="CountdowningPaused"),
      @Transit(from="CountdowningPaused", on="Continue", to="Countdowning"),
      @Transit(from="Countdowning", on="Record", to="Recording"),

      @Transit(from="Recording", on="Pause", to="RecordingPaused"),
      @Transit(from="RecordingPaused", on="Continue", to="Recording"),
      @Transit(from="Recording", on="Next", to="Countdowning"),
  })
  static class MyStateMachine extends AbstractStateMachine
                                            <MyStateMachine, FSMState, FSMEvent, CameraActivity> {

    public void entryCountdowning(FSMState from, FSMState to,
                                  FSMEvent event, CameraActivity context) {
      Log.i(fsmTag, String.format("entry state: %s -> %s", from, to));

      context.countDownTextView.setVisibility(View.VISIBLE);
      context.updateViewContent();
      context.startCountdownAnimation();
    }

    public void exitCountdowning(FSMState from, FSMState to,
                                 FSMEvent event, CameraActivity context) {
      Log.i(fsmTag, String.format("exit state: %s -> %s", from, to));
      if (event == FSMEvent.Record) {
        context.countDownTextView.setVisibility(View.GONE);
      }
      context.cancelCountdownAnimation();
    }

    public void entryRecording(FSMState from, FSMState to,
                               FSMEvent event, CameraActivity context) {
      Log.i(fsmTag, String.format("entry state: %s -> %s", from, to));
      context.cameraView.startRecord(context.videoFilePath);
      context.startProgressAnimation();
    }

    public void exitRecording(FSMState from, FSMState to, FSMEvent event, CameraActivity context) {
      Log.i(fsmTag, String.format("exit state: %s -> %s", from, to));
      context.cameraView.stopRecording();
      context.cameraView.startPreview();
      context.progressBar.setProgress(0);

      if (event == FSMEvent.Next) {
        context.currentSignIndex++;
        context.makeVideoPath();
        if (context.currentSignIndex == context.signPromptList.size()) {
          Log.i(fsmTag, "before terminate");
          this.terminate(context);
          Log.i(fsmTag, "end terminate");
          context.finishWithToastInfo(String.format("今天完成了%d", context.signPromptList.size()));
        }
      } else {
        FileUtils.clearFile(context.videoFilePath);
      }
    }

    public void entryCountdowningPaused(FSMState from, FSMState to,
                                        FSMEvent event, CameraActivity context) {
      Log.i(fsmTag, String.format("entry state: %s -> %s", from, to));
      context.showPausedDialog();
    }

    public void entryRecordingingPaused(FSMState from, FSMState to,
                                        FSMEvent event, CameraActivity context) {
      Log.i(fsmTag, String.format("entry state: %s -> %s", from, to));
      context.showPausedDialog();
    }

  }

  private void showPausedDialog() {
    //make the dialog align to camereVieo
    int[] location = new int[2];
    cameraView.getLocationInWindow(location);
    pausingDialog.setPosition(location[0], location[1],
        cameraView.getWidth(), cameraView.getHeight());
    pausingDialog.show(getFragmentManager(),
        LearnVideoDialog.class.getName(),
        signPromptList.get(currentSignIndex).getSampleVideo().getVideoPath(),
        signPromptList.get(currentSignIndex).getSampleVideo().getThumbnail());
  }


  static class AnimatorListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(Animator animation) {}

    @Override
    public void onAnimationEnd(Animator animation) {}

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}
  }
}
