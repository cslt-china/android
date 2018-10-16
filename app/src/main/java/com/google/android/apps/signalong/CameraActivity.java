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

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.android.apps.signalong.broadcast.NetworkReceiver;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;
import com.google.android.apps.signalong.widget.CameraView;
import com.google.android.apps.signalong.widget.CameraView.CallBack;
import com.google.android.apps.signalong.widget.LearnVideoDialog;

import java.io.File;
import java.util.List;


/**
 * CameraActivity implements video recording activity, reference code link
 * https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/Camera2VideoFragment.java.
 */
public class CameraActivity extends BaseActivity {

  static private String fsmTag = "StateMachine";

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
      cameraViewModel.startUploadThread();
      makeVideoPath();
      startFSM();
    });
  }

  private void fireFsmEvent(FSMEvent event) {
    runOnUiThread(()-> {
      if (fsm == null || !fsm.canFire(event)) {
        return;
      }
      Log.i(fsmTag, "before fire " + event);
      fsm.fire(event);
      Log.i(fsmTag, "after fire " + event);
    });
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

  private void startFSM() {
    fireFsmEvent(FSMEvent.Start);
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
      public void onStart(Animator animation) {
        largeWordPromptLayout.setVisibility(View.VISIBLE);
        realRecordLayout.setVisibility(View.GONE);
      }
      @Override
      public void onEnd(Animator animation) {
        Log.i("CameraActiviy", "countdown animation end");
        fireFsmEvent(FSMEvent.Record);
      }

      @Override
      public void onCancel(Animator animator) {
        Log.i("CameraActiviy", "countdown animation canceled");
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
          boolean valid;
          @Override
          public void onStart(Animator animator) {
            Log.i("CameraActiviy", "progress animation start");
          }
          @Override
          public void onEnd(Animator animator) {
            Log.i("CameraActiviy", "progress animation end");
            fireFsmEvent(FSMEvent.Next);
          }
          @Override
          public void onCancel(Animator animator) {
            Log.i("CameraActiviy", "progress animation canceled");
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
    if (info != null && info.length() > 0) {
      ToastUtils.show(getApplicationContext(), info);
    }
    finish();
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
    if (fsm != null && fsm.getState() != FSMState.Stopped) {
      fireFsmEvent(FSMEvent.Stop);
    } else {
      finish();
    }
  }

  enum FSMEvent {
    Start,
    Next,
    Record,
    Pause,
    Continue,
    Stop
  }

  enum FSMState {
    Created,
    Countdowning,
    CountdowningPaused,
    Recording,
    RecordingPaused,
    Stopped
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

  public void entryCountdowning() {
    Log.i(fsmTag, "entryCountdowning");
    cameraView.startPreview();
    countDownTextView.setVisibility(View.VISIBLE);
    updateViewContent();
    startCountdownAnimation();
  }

  public void exitCountdowning(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitCountdowning");
    if (transition.getTrigger() == FSMEvent.Record) {
      countDownTextView.setVisibility(View.GONE);
    }
    cancelCountdownAnimation();
  }

  public void entryRecording() {
    Log.i(fsmTag, "entryRecording");
    cameraView.startRecord(videoFilePath);
    startProgressAnimation();
  }

  public void onFSMStopped() {
    finishWithToastInfo(
        (currentSignIndex > 0) ? String.format(getString(R.string.thanks), currentSignIndex) : null);
  }

  public void exitRecording(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitRecording");
    cameraView.stopRecording();
    cancelProgressAnimation();
    progressBar.setProgress(0);

    if (transition.getTrigger() == FSMEvent.Next) {
      uploadVideo();
      currentSignIndex++;
      makeVideoPath();
    } else {
      FileUtils.clearFile(videoFilePath);
    }
  }

  public void entryCountdowningPaused() {
    Log.i(fsmTag, "entryCountdowningPaused");
    showPausedDialog();
  }

  public void entryRecordingingPaused() {
    Log.i(fsmTag, "entryRecordingingPaused");
    showPausedDialog();
  }

  StateMachine<FSMState, FSMEvent> fsm;

  void initFSM() {

    StateMachineConfig<FSMState, FSMEvent> config = new StateMachineConfig<>();

    config.configure(FSMState.Created).
        permit(FSMEvent.Start, FSMState.Countdowning)
        .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.Countdowning)
          .onEntry(this::entryCountdowning)
          .onExit(this::exitCountdowning)
          .permit(FSMEvent.Pause, FSMState.CountdowningPaused)
          .permit(FSMEvent.Record, FSMState.Recording)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.CountdowningPaused)
          .onEntry(this::entryCountdowningPaused)
          .permit(FSMEvent.Continue, FSMState.Countdowning)
          .permit(FSMEvent.Stop, FSMState.Stopped);


    config.configure(FSMState.Recording)
          .onEntry(this::entryRecording)
          .onExit(this::exitRecording)
          .permitDynamic(FSMEvent.Next,
                         ()-> {
                           Log.i(fsmTag, String.format("dyamic next: currsignIndex %d", currentSignIndex));
                           if (currentSignIndex < signPromptList.size() - 1) {
                             return FSMState.Countdowning;
                           } else {
                             return FSMState.Stopped;
                           }
                         })
          .permit(FSMEvent.Pause, FSMState.RecordingPaused)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.RecordingPaused)
          .onEntry(this::entryRecordingingPaused)
          .permit(FSMEvent.Continue, FSMState.Countdowning)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.Stopped)
          .onEntry(this::onFSMStopped);


    fsm = new StateMachine<>(FSMState.Created, config);
  }

  static abstract class AnimatorListener implements Animator.AnimatorListener {
    private boolean valid;
    @Override
    final public void onAnimationStart(Animator animation) {
      valid = true;
      onStart(animation);
    }

    @Override
    final public void onAnimationEnd(Animator animation) {
      if (valid) {
        onEnd(animation);
      }
    }

    @Override
    final public void onAnimationCancel(Animator animation) {
      valid = false;
      onCancel(animation);
    }

    @Override
    final public void onAnimationRepeat(Animator animation) {}

    abstract public void onStart(Animator animator);
    abstract public void onEnd(Animator animator);
    abstract public void onCancel(Animator animator);
  }
}
