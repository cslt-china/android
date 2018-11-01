package com.google.android.apps.signalong;

import android.animation.Animator;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.transitions.Transition;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.android.apps.signalong.ReferenceVideoViewFragment.OnReferenceCompletionListerner;
import com.google.android.apps.signalong.SelfAssessRecordedVideoFragment.OnSelfAssessRecordedVideoListerner;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Response;

/**
 * CameraActivity implements video recording activity, reference code link
 * https://github.com/googlesamples/android-Camera2Video/blob/master/Application/src/main/java/com/example/android/camera2video/Camera2VideoFragment.java.
 */
public class CameraActivity extends BaseActivity implements
    CameraViewModel.SignPromptBatchResponseCallbacks,
    OnReferenceCompletionListerner,
    OnSelfAssessRecordedVideoListerner {
  private static final String TAG = "CameraActivity";

  private static String fsmTag = "SignAlongStateMachine";

  // Accepted intent param.
  public static final String RECORDING_TASK_PARAM = "recording_param";

  private static final int LARGE_WORD_PROMPT_WAIT_TIME = 1500;
  /* Suffix of video file.*/
  private static final String VIDEO_SUFFIX = ".mp4";

  private TopicFragment topicFragment;
  private ReferenceVideoViewFragment referenceFragment;
  private CountdownFragment countdownFragment;
  private RecordFragment2 recordFragment;
  private SelfAssessRecordedVideoFragment assessmentFragment;
  private CameraViewModel cameraViewModel;
  private List<SignPromptBatchResponse.DataBean> signPromptList;
  private int currentSignIndex;
  private String videoFilePath;
  private Timer showTopicTimer = new Timer();

  private boolean isShown = false;
  private boolean isTaskReady = false;

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
    initTopicFragment();
    initReferenceFragment();
    initCountdownFragment();
    initRecordFragment();
    initAssessmentFragment();
    initModel();
    hideAllFragment();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    isShown = true;
    tryStartFSM();
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

  private void initTopicFragment() {
    topicFragment = (TopicFragment)
        getSupportFragmentManager().findFragmentById((R.id.topic_fragment));
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
    signPromptList = ActivityUtils.parseRecordingTaskFromIntent(this);
    if (signPromptList != null && signPromptList.size() > 0) {
      Log.i(TAG, "parsed prompt data from intent.");
      startWithPromptData();
    } else {
      Log.i(TAG, "fetch prompt data from server");
      cameraViewModel.getSignPromptBatch(this);
    }
  }

  public void onSuccessSignPromptBatchResponse(Response<SignPromptBatchResponse> response) {
    if (response != null && response.body() != null && response.body().getCode() == 0) {
      signPromptList = response.body().getData();
      if (signPromptList == null || signPromptList.isEmpty()) {
        finishWithToastInfo(getString(R.string.no_new_task));
      } else {
        startWithPromptData();
      }
    } else {
      finishWithToastInfo(getString(R.string.tip_request_fail));
    }
  }

  public void onFailureResponse() {
  }

  private void startWithPromptData() {
    cameraViewModel.startUploadThread();
    initFSM();
    isTaskReady = true;
    tryStartFSM();
  }

  private void fireFsmEvent(FSMEvent event) {
    if (fsm == null || !fsm.canFire(event)) {
      return;
    }
    Log.i(fsmTag, "before fire " + event);
    fsm.fire(event);
    Log.i(fsmTag, "after fire " + event);
  }

  private void tryStartFSM() {
    if (isShown && isTaskReady) {
      fireFsmEvent(FSMEvent.Start);
    }
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
    showTopicTimer.cancel();
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
    ShowTopic,
    Learn,
    PrepareRecord,
    Record,
    RecordingEnd,
    Confirm,
    Retry,
    Stop
  }

  enum FSMState {
    Inited,
    ShowingTopic,
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
          .permit(FSMEvent.Start, FSMState.ShowingTopic)
          .permit(FSMEvent.Stop, FSMState.Stopped);

    config.configure(FSMState.ShowingTopic)
          .onEntry(this::entryShowingTopic)
          .onExit(this::exitShowingTopic)
          .permit(FSMEvent.Learn, FSMState.Learning)
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
            return currentSignIndex < signPromptList.size() ? FSMState.ShowingTopic : FSMState.Stopped;
          })
          .permit(FSMEvent.Stop, FSMState.Stopped);


    config.configure(FSMState.Stopped)
          .onEntry(this::entryStopped);

    fsm = new StateMachine<>(FSMState.Inited, config);
  }


  public void entryShowingTopic() {
    showFragment(topicFragment);
    topicFragment.setTopicText(
        currentSignIndex,
        signPromptList.get(currentSignIndex).getText());

    showTopicTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(()-> fireFsmEvent(FSMEvent.Learn) );
      }
    }, Config.TOPIC_SHOW_SECOND * 1000);
  }

  public void exitShowingTopic() {
    Log.i(fsmTag, "exitShowingTopic");
  }

  public void entryLearning() {
    Log.i(fsmTag, "entryLearning");
    showFragment(referenceFragment);

    SignPromptBatchResponse.DataBean current =
        signPromptList.get(currentSignIndex);

    referenceFragment.playReference(
        current.getText(),
        current.getSampleVideo().getVideoPath(),
        current.getSampleVideo().getThumbnailPath());
  }

  public void exitLearning() {
    Log.i(fsmTag, "exitLearning");
  }

  public void entryCountdowning() {
    Log.i(fsmTag, "entryCountdowning");
    showTowFragments(countdownFragment, recordFragment);
    startCountdownAnimation();
  }

  public void exitCountdowning(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitCountdowning");
    countdownFragment.cancelCountdown();
  }

  public void entryRecording() {
    Log.i(fsmTag, "entryRecording");
    showFragment(recordFragment);
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
    showFragment(assessmentFragment);
    String text = signPromptList.get(currentSignIndex).getText();
    Log.i(TAG, "playback the recorded video for " + text + " from " + videoFilePath);
    Log.i(TAG, "assessmentFragment" + String.valueOf(assessmentFragment));
    assessmentFragment.playRecorded(
      signPromptList.get(currentSignIndex).getText(), videoFilePath);
  }

  public void exitWaitingConfirm(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitWaitingConfirm");
  }

  public void entryStopped() {
    finishWithToastInfo(
        (currentSignIndex > 0) ? String.format(getString(R.string.thanks), currentSignIndex) : null);
  }

  private void hideAllFragment() {
    topicFragment.setVisibility(View.GONE);
    countdownFragment.setVisibility(View.GONE);
    recordFragment.setVisibility(View.GONE);
    assessmentFragment.setVisibility(View.GONE);
    referenceFragment.setVisibility(View.GONE);
  }

  private void showFragment(BaseFragment fragment) {
    hideAllFragment();
    fragment.setVisibility(View.VISIBLE);
  }

  private void showTowFragments(BaseFragment upperFragment,
                                BaseFragment lowerFragment) {
    hideAllFragment();
    lowerFragment.setVisibility(View.VISIBLE);
    upperFragment.setVisibility(View.VISIBLE);
    upperFragment.getView().bringToFront();
  }
}
