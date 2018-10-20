package com.google.android.apps.signalong;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.StateMachineConfig;
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

  static private String fsmTag = "SignAlongStateMachine";

  private static final int LARGE_WORD_PROMPT_WAIT_TIME = 1500;
  /* Suffix of video file.*/
  private static final String VIDEO_SUFFIX = ".mp4";
  private CountdownFragment countdownFragment;
  private CameraView cameraView;
  private TextView titleTextView;
  private ViewGroup realRecordLayout;
  private String videoFilePath;
  private CameraViewModel cameraViewModel;
  private ProgressBar progressBar;
  private List<SignPromptBatchResponse.DataBean> signPromptList;
  private int currentSignIndex;
  private LearnVideoDialog pausingDialog;
  ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);

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
      startFSM();
    });
  }

  private void fireFsmEvent(FSMEvent event) {
    if (fsm == null || !fsm.canFire(event)) {
      return;
    }
    Log.i(fsmTag, "before fire " + event);
    fsm.fire(event);
    Log.i(fsmTag, "after fire " + event);
  }

  @Override
  public void initViews() {
    pausingDialog = new LearnVideoDialog();
    progressBar = findViewById(R.id.progres_bar);
    titleTextView = findViewById(R.id.topic_title);
    cameraView = findViewById(R.id.camera_view);
    realRecordLayout = findViewById(R.id.record_layout);
    countdownFragment = (CountdownFragment)
        getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);

    cameraView.setCallBack(new CallBack() {
      @Override
      public void onCameraOpened() {

      }
      @Override
      public void onCameraError() {
        //TODO: stop fsm
        finishWithToastInfo(getString(R.string.open_camera_failed));
      }
    });

    initCountdownAnimation();
    initProgressAnimation();
    fillModel();
  }

  void initCountdownAnimation() {
    countdownFragment.addListener(new CancelOrEndAnimatorListener() {
      @Override
      public void onStart(Animator animation) {
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

  private void startCountdownAnimation() {
    int countDownSecond = VideoRecordingSharedPreferences.getTiming(
        getApplicationContext(), TimingType.PREPARE_TIME);
    String text = signPromptList.get(currentSignIndex).getText();

    countdownFragment.startAnimation(countDownSecond, text);
  }

  private void cancelCountdownAnimation() {
    countdownFragment.cancelAnimation();
  }

  private void initProgressAnimation() {
    progressBar.setMax(100);
    progressAnimator = ValueAnimator.ofInt(0, 100);
    progressAnimator.addUpdateListener(valueAnimator ->
    {
      progressBar.setProgress((int) valueAnimator.getAnimatedValue());
    });

    progressAnimator.addListener(
        new CancelOrEndAnimatorListener() {
          boolean valid;
          @Override
          public void onStart(Animator animator) {
            Log.i("CameraActiviy", "progress animation start");
          }
          @Override
          public void onEnd(Animator animator) {
            Log.i("CameraActiviy", "progress animation end");
            fireFsmEvent(FSMEvent.RecordingEnd);
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
    titleTextView.setText(String.format(getString(R.string.please_sign), text));
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
    PrepareRecord,
    Record,
    RecordingEnd,
    Confirm,
    Retry,
    Stop
  }

  enum FSMState {
    Inited,
    Learning,
    Countdowning,
    Recording,
    WaitingConfirm,
    Stopped
  }

  StateMachine<FSMState, FSMEvent> fsm;

  void initFSM() {

    StateMachineConfig<FSMState, FSMEvent> config = new StateMachineConfig<>();

    config.configure(FSMState.Inited)
          .permit(FSMEvent.Start, FSMState.Learning)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.Learning)
          .onEntry(this::entryLearning)
          .onExit(this::exitLearning)
          .permit(FSMEvent.PrepareRecord, FSMState.Countdowning)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.Countdowning)
          .onEntry(this::entryCountdowning)
          .onExit(this::exitCountdowning)
          .permit(FSMEvent.Record, FSMState.Recording)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.Recording)
          .onEntry(this::entryRecording)
          .onExit(this::exitRecording)
          .permit(FSMEvent.RecordingEnd, FSMState.WaitingConfirm)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.WaitingConfirm)
          .onEntry(this::entryWaitingConfirm)
          .onExit(this::exitWaitingConfirm)
          .permit(FSMEvent.Retry, FSMState.Learning)
          .permitDynamic(FSMEvent.Confirm,
                         ()-> {
                           Log.i(fsmTag, String.format("dyamic Confirm: currsignIndex %d", currentSignIndex));
                           if (currentSignIndex < signPromptList.size() - 1) {
                             return FSMState.Learning;
                           } else {
                             return FSMState.Stopped;
                           }
                         },
                         ()-> currentSignIndex++
                        )
          .permit(FSMEvent.Stop, FSMState.Stopped);


    config.configure(FSMState.Stopped)
          .onEntry(this::entryStopped);

    fsm = new StateMachine<>(FSMState.Inited, config);
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

  public void entryLearning() {
    Log.i(fsmTag, "entryLearning");
    //NOTE: not use runOnUiThread, because this is UI Thread.
    //we must run it later
    postToUIThread(()->fireFsmEvent(FSMEvent.PrepareRecord));
  }

  public void exitLearning() {
    Log.i(fsmTag, "exitLearning");
  }

  public void entryCountdowning() {
    Log.i(fsmTag, "entryCountdowning");
    cameraView.startPreview();
    realRecordLayout.setVisibility(View.GONE);
    countdownFragment.setVisibility(View.VISIBLE);
    updateViewContent();
    startCountdownAnimation();
  }

  public void exitCountdowning(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitCountdowning");
    countdownFragment.setVisibility(View.GONE);
    cancelCountdownAnimation();
  }

  public void entryRecording() {
    Log.i(fsmTag, "entryRecording");
    realRecordLayout.setVisibility(View.VISIBLE);
    makeVideoPath();
    cameraView.startRecord(videoFilePath);
    startProgressAnimation();
  }

  public void exitRecording(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitRecording");
    cameraView.stopRecording();
    cancelProgressAnimation();
    progressBar.setProgress(0);

  }

  private void postToUIThread(Runnable r) {
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(r);
  }

  public void entryWaitingConfirm() {
    postToUIThread(()->fsm.fire(FSMEvent.Confirm));
  }

  public void exitWaitingConfirm(Transition<FSMState, FSMEvent> transition) {
    if (transition.getTrigger() == FSMEvent.Confirm) {
      uploadVideo();
    } else {
      FileUtils.clearFile(videoFilePath);
    }
  }

  public void entryStopped() {
    finishWithToastInfo(
        (currentSignIndex > 0) ? String.format(getString(R.string.thanks), currentSignIndex) : null);
  }

  public void entryCountdowningPaused() {
    Log.i(fsmTag, "entryCountdowningPaused");
    showPausedDialog();
  }

  public void entryRecordingingPaused() {
    Log.i(fsmTag, "entryRecordingingPaused");
    showPausedDialog();
  }
}
