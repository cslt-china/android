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
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences;
import com.google.android.apps.signalong.utils.VideoRecordingSharedPreferences.TimingType;
import com.google.android.apps.signalong.widget.CameraView;
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
  private RecordFragment recordFragment;
  private String videoFilePath;
  private CameraViewModel cameraViewModel;
  private List<SignPromptBatchResponse.DataBean> signPromptList;
  private int currentSignIndex;
  private LearnVideoDialog pausingDialog;

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
    pausingDialog = new LearnVideoDialog();
    initCountdownFragment();
    initRecordFragment();
    initModel();
  }
  void initRecordFragment() {
    recordFragment = (RecordFragment)
        getSupportFragmentManager().findFragmentById(R.id.record_fragment);

    recordFragment.setCameraCallBack(new CameraView.CallBack() {
      @Override
      public void onCameraOpened() {

      }
      @Override
      public void onCameraError() {
        finishWithToastInfo(getString(R.string.open_camera_failed));
      }
    });

    recordFragment.setRecordCallback(new CancelOrEndAnimatorListener() {
      @Override
      public void onStart(Animator animator) {
        Log.i(fsmTag, "progress animation start");
      }
      @Override
      public void onEnd(Animator animator) {
        Log.i(fsmTag, "progress animation end");
        fireFsmEvent(CameraActivity.FSMEvent.RecordingEnd);
      }
      @Override
      public void onCancel(Animator animator) {
        Log.i("CameraActiviy", "progress animation canceled");
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
  protected void onDestroy() {
    recordFragment.closeCamera();
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
    recordFragment.startPreview();
    recordFragment.setVisibility(View.GONE);
    countdownFragment.setVisibility(View.VISIBLE);
    updateViewContent();
    startCountdownAnimation();
  }

  public void exitCountdowning(Transition<FSMState, FSMEvent> transition) {
    Log.i(fsmTag, "exitCountdowning");
    countdownFragment.setVisibility(View.GONE);
    countdownFragment.cancelCountdown();
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
    recordFragment.stopRecording();

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
