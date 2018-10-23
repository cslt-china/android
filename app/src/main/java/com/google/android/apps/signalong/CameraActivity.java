package com.google.android.apps.signalong;

import android.animation.Animator;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.android.apps.signalong.ReferenceVideoViewFragment.OnReferenceCompletionListerner;
import com.google.android.apps.signalong.SelfAssessRecordedVideoFragment.OnSelfAssessRecordedVideoListerner;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;
import com.google.android.apps.signalong.widget.CameraView;

import java.io.File;
import java.util.List;

/**
 * CameraActivity implements video recording activity, reference code link
 * https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/Camera2VideoFragment.java.
 */
public class CameraActivity extends BaseActivity implements
    OnReferenceCompletionListerner,
    OnSelfAssessRecordedVideoListerner {
  private static final String TAG = "CameraActivity";

  static private String fsmTag = "SignAlongStateMachine";

  private static final int LARGE_WORD_PROMPT_WAIT_TIME = 1500;
  /* Suffix of video file.*/
  private static final String VIDEO_SUFFIX = ".mp4";

  private ReferenceVideoViewFragment referenceFragment;
  private CountdownFragment countdownFragment;
  private RecordFragment2 recordFragment;
  private SelfAssessRecordedVideoFragment assessmentFragment;
  private CameraViewModel cameraViewModel;
  private List<SignPromptBatchResponse.DataBean> signPromptList;
  private int currentSignIndex;
  private String videoFilePath;

  @Override
  public int getContentView() {
    return R.layout.activity_camera;
  }

  @Override
  public void init() {
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    currentSignIndex = 0;
  }

  @Override
  public void initViews() {
    initReferenceFragment();
    initCountdownFragment();
    initRecordFragment();
    initAssessmentFragment();
    initModel();
  }

  @Override
  public void onReferenceVideoViewCompletion() {
    fireFsmEvent(FSMEvent.PrepareRecord);
  }

  @Override
  public void onRejectRecordedVideo() {
    fireFsmEvent(FSMEvent.Retry);
  }

  @Override
  public void onAcceptRecordedVideo() {
    fireFsmEvent(FSMEvent.Confirm);
  }

  private void initReferenceFragment() {
    referenceFragment = (ReferenceVideoViewFragment)
        getSupportFragmentManager().findFragmentById((R.id.reference_fragment));
  }

  private void initRecordFragment() {
    recordFragment = (RecordFragment2)
        getSupportFragmentManager().findFragmentById(R.id.record_fragment);

    recordFragment.setCameraCallback(new RecordFragment2.CameraCallback() {
      @Override
      public void onError(String errorMessage) {
        finishWithToastInfo(getString(R.string.open_camera_failed));
        Log.e(fsmTag, "open camera error: " + errorMessage);
      }
    });

    recordFragment.setRecordCallback(new RecordFragment2.RecordCallback() {
      public void onFinished() {
        Log.i(fsmTag, "recording finished");
        fireFsmEvent(CameraActivity.FSMEvent.RecordingEnd);
      }
      @Override
      public void onCancel() {
        Log.i(fsmTag, "recording is canceled");
      }
    });
  }

  void initCountdownFragment() {
    countdownFragment = (CountdownFragment)
        getSupportFragmentManager().findFragmentById(R.id.countdown_fragment);
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

  private void initAssessmentFragment() {
    assessmentFragment = (SelfAssessRecordedVideoFragment)
        getSupportFragmentManager().findFragmentById(R.id.self_assess_fragment);
  }

  private void initModel() {
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

          cameraViewModel.startUploadThread();
          initFSM();
          startFSM();
        });

    cameraViewModel.getSignPromptBatch();
  }


  private void fireFsmEvent(FSMEvent event) {
    if (fsm == null || !fsm.canFire(event)) {
      return;
    }
    Log.i(fsmTag, "before fire " + event);
    fsm.fire(event);
    Log.i(fsmTag, "after fire " + event);
  }

  private void startFSM() {
    fireFsmEvent(FSMEvent.Start);
  }

  private void stopFSM() {
    fireFsmEvent(FSMEvent.Stop);
  }

  private void startCountdownAnimation() {
    int countDownSecond = VideoRecordingSharedPreferences.getTiming(
        getApplicationContext(), TimingType.PREPARE_TIME);
    String text = signPromptList.get(currentSignIndex).getText();

    countdownFragment.startCountdown(countDownSecond, text);
  }

  @Override
  protected void onPause() {
    fireFsmEvent(FSMEvent.Retry);
    super.onPause();
  }

  @Override
  protected void onDestroy() {
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

  @Override
  public void onBackPressed() {
    if (fsm != null && fsm.getState() != FSMState.Stopped) {
      stopFSM();
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
          .permitDynamic(FSMEvent.Stop,
                         ()->FSMState.Stopped,
                         ()->recordFragment.cancelRecording());

    config.configure(FSMState.WaitingConfirm)
          .onEntry(this::entryWaitingConfirm)
          .onExit(this::exitWaitingConfirm)
          .permitDynamic(FSMEvent.Retry, () -> {
            FileUtils.clearFile(videoFilePath);
            return FSMState.Learning;
          })
          .permitDynamic(FSMEvent.Confirm, () -> {
            Log.i(fsmTag, String.format("dyamic Confirm: currsignIndex %d", currentSignIndex));
            uploadVideo();
            currentSignIndex++;
            return currentSignIndex < signPromptList.size() ? FSMState.Learning : FSMState.Stopped;
          })
          .permit(FSMEvent.Stop, FSMState.Stopped);


    config.configure(FSMState.Stopped)
          .onEntry(this::entryStopped);

    fsm = new StateMachine<>(FSMState.Inited, config);
  }

  public void entryLearning() {
    Log.i(fsmTag, "entryLearning");
    countdownFragment.setVisibility(View.GONE);
    recordFragment.setVisibility(View.GONE);
    assessmentFragment.setVisibility(View.GONE);
    referenceFragment.setVisibility(View.VISIBLE);

    referenceFragment.playReference(
        signPromptList.get(currentSignIndex).getText(),
        signPromptList.get(currentSignIndex).getSampleVideo().getVideoPath());
  }

  public void exitLearning() {
    Log.i(fsmTag, "exitLearning");
    referenceFragment.setVisibility(View.GONE);
  }

  public void entryCountdowning() {
    Log.i(fsmTag, "entryCountdowning");
    countdownFragment.setVisibility(View.VISIBLE);
    startCountdownAnimation();
  }

  public void exitCountdowning(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitCountdowning");
    countdownFragment.cancelCountdown();
    countdownFragment.setVisibility(View.GONE);
  }

  public void entryRecording() {
    Log.i(fsmTag, "entryRecording");
    recordFragment.setVisibility(View.VISIBLE);
    makeVideoPath();

    int scale = VideoRecordingSharedPreferences.getTiming(
        getApplicationContext(), TimingType.RECORD_TIME_SCALE);

    int recordingTime = (int)Math.ceil(
        signPromptList.get(currentSignIndex).getDuration() / 1000.0 * scale / 100.0);
    recordFragment.startRecord(signPromptList.get(currentSignIndex).getText(),
                               videoFilePath, recordingTime);
  }

  public void exitRecording(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitRecording");
  }


  public void entryWaitingConfirm() {
    assessmentFragment.setVisibility(View.VISIBLE);
    String text = signPromptList.get(currentSignIndex).getText();
    Log.i(TAG, "playback the recorded video for " + text + " from " + videoFilePath);
    Log.i(TAG, "assessmentFragment" + String.valueOf(assessmentFragment));
    assessmentFragment.playRecorded(
      signPromptList.get(currentSignIndex).getText(), videoFilePath);
  }

  public void exitWaitingConfirm(Transition<FSMState, FSMEvent> transition) {
    assessmentFragment.setVisibility(View.GONE);
  }

  public void entryStopped() {
    finishWithToastInfo(
        (currentSignIndex > 0) ? String.format(getString(R.string.thanks), currentSignIndex) : null);
  }
}
