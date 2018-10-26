package com.google.android.apps.signalong;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.upgrade.UpgradeManager;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.utils.IntroSharedPreferences.IntroType;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.NetworkUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

import com.google.android.apps.signalong.widget.TaskView.TaskType;
import com.google.android.apps.signalong.widget.TaskViewAdapter;
import java.util.List;
import retrofit2.Response;

/**
 * Android signalong App.
 */
public class SignAlongActivity extends BaseActivity implements
    VideoReviewViewModel.UnreviewedVideoListResponseCallbacks,
    CameraViewModel.SignPromptBatchResponseCallbacks {
  private static final int SPAN_COUNT = 4;
  private static final String[] UPGRADE_APP_PERMISSIONS = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  public static final int REQUEST_UPGRADE_APP_PERMISSION_CODE = 7;

  private SignAlongViewModel signAlongViewModel;
  private CameraViewModel cameraViewModel;
  private VideoReviewViewModel videoReviewViewModel;
  private TaskViewAdapter recordingTaskViewAdapter;
  private TaskViewAdapter reviewTaskViewAdapter;
  private List<DataBean> unreviewedVideoList;
  private SignPromptBatchResponse promptList;

  @Override
  public int getContentView() {
    return R.layout.activity_signalong;
  }

  @Override
  public void init() {
    cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
    videoReviewViewModel = ViewModelProviders.of(this).get(VideoReviewViewModel.class);
    signAlongViewModel = ViewModelProviders.of(this).get(SignAlongViewModel.class);
    signAlongViewModel
        .checkLogin()
        .observe(
            this,
            isLogin -> {
              if (isLogin != null && isLogin) {
                initUserData();
                String username = LoginSharedPreferences.getCurrentUsername(getApplicationContext());
                if(!LoginSharedPreferences.getConfirmedAgreement(getApplicationContext(), username)) {
                  startConfirmAgreementActivity();
                } else {
                  checkUpdate();
                }
              } else {
                startLoginActivity();
              }
            });
  }

  @Override
  public void initViews() {
    Toolbar toolbar = findViewById(R.id.home_toolbar);
    setSupportActionBar(toolbar);
    findViewById(R.id.btn_home_setting)
        .setOnClickListener(
            view -> {
              startActivityForResult(new Intent(getApplicationContext(), SettingActivity.class), 0);
            });
    findViewById(R.id.record_video_button)
        .setOnClickListener(
            view -> {
              ActivityUtils.startCameraActivity(this, promptList);
            });
    findViewById(R.id.review_video_button)
        .setOnClickListener(
            view -> {
              ActivityUtils.startReviewActivity(this, unreviewedVideoList);
            }
        );

    recordingTaskViewAdapter = new TaskViewAdapter();
    setRecyclerView(R.id.recording_task_recyclerview, recordingTaskViewAdapter, SPAN_COUNT);

    reviewTaskViewAdapter = new TaskViewAdapter();
    setRecyclerView(R.id.review_task_recyclerview, reviewTaskViewAdapter, SPAN_COUNT);
  }

  @Override
  protected void onResume() {
    super.onResume();
    initUserData();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] permissionsStatus) {

    Context context = getApplicationContext();
    if (ActivityUtils.hasPermission(context, permissions)) {
      switch (requestCode) {
        case ActivityUtils.REQUEST_REVIEW_VIDEO_PERMISSIONS:
          ActivityUtils.triggerIntroOrTargetActivity(this, IntroReviewActivity.class,
              VideoReviewActivity.class, IntroType.REVIEW, null);
          break;
        case ActivityUtils.REQUEST_RECORD_VIDEO_PERMISSIONS:
          ActivityUtils.triggerIntroOrTargetActivity(this, IntroRecordActivity.class,
              CameraActivity.class, IntroType.RECORD, null);
          break;
        case ActivityUtils.REQUEST_UPGRADE_APP_PERMISSION_CODE:
          if (ActivityUtils.hasPermission(getApplicationContext(), UPGRADE_APP_PERMISSIONS)) {
            checkUpdate();
          } else {
            ToastUtils.show(getApplicationContext(),
                getString(R.string.upgrade_request_permission_hint));
          }
        default:
          ToastUtils.show(context, "Unknown request code " + requestCode);
      }
    } else {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_refuse_permission));
    }
    super.onRequestPermissionsResult(requestCode, permissions, permissionsStatus);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
    if (resultCode == LoginActivity.LOGIN_SUCCESS) {
      initUserData();
    } else if (resultCode == LoginActivity.LOGIN_FAIL) {
      finish();
    } else if (resultCode == LoginActivity.CONFIRM_AGREEMENT) {
      startConfirmAgreementActivity();
    } else if (resultCode == SettingActivity.LOGOUT_SUCCESS) {
      startLoginActivity();
    }
  }

  private RecyclerView setRecyclerView(int viewId, TaskViewAdapter adapter, int columnSpan) {
    RecyclerView recyclerView = (RecyclerView) findViewById(viewId);
    recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), columnSpan));
    recyclerView.setAdapter(adapter);
    return recyclerView;
  }

  private void startConfirmAgreementActivity() {
    Intent intent = new Intent(getApplicationContext(), AgreementActivity.class);
    startActivity(intent);
  }

  private void startLoginActivity() {
    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivityForResult(intent, 0);
  }

  private void initUserData() {
    signAlongViewModel
        .getCurrentPointAndUsernameLiveData()
        .observe(
            this,
            currentPointAndUsername -> {
              if (currentPointAndUsername == null) {
                ((TextView) findViewById(R.id.app_title_textview))
                    .setText(getString(R.string.label_loading));
                return;
              } else {
                ((TextView) findViewById(R.id.app_title_textview))
                    .setText(getString(R.string.app_name));
              }
              ToastUtils.show(getApplicationContext(),
                  String.format(
                      getString(R.string.tip_welcome),
                      currentPointAndUsername.getData().getUsername()));
            });

    videoReviewViewModel.getUnreviewedVideoList(this);
    cameraViewModel.getSignPromptBatch(this);
  }

  public void onSuccessUnreviewedVideoListResponse(Response<VideoListResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0) {
      ((TextView) findViewById(R.id.review_task_count_textview))
          .setText(
              String.format(
                  getString(R.string.label_review_task_count),
                  response.body().getDataBeanList().getTotal()));
      this.unreviewedVideoList = response.body().getDataBeanList().getData();
      reviewTaskViewAdapter.setVideoList(
          getApplicationContext(), response.body().getDataBeanList(), TaskType.NEW_REVIEW);
    } else {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
    }
  }

  public void onFailureResponse() {
    ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
  }

  public void onSuccessSignPromptBatchResponse(Response<SignPromptBatchResponse> response) {
    if (response.isSuccessful() && response.body().getCode() == 0) {
      this.promptList = response.body();
      ((TextView) findViewById(R.id.recording_task_count_textview))
          .setText(String.format(getString(R.string.label_recording_task_count),
              promptList.getData() == null ? 0 : promptList.getData().size()));
      recordingTaskViewAdapter.setSignPromptList(getApplicationContext(), response.body());
    } else {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_request_fail));
    }
  }

  private void checkUpdate() {
    // Check permission
    if (!ActivityUtils.hasPermission(getApplicationContext(), UPGRADE_APP_PERMISSIONS)) {
      // Request permission.
      requestPermissions(UPGRADE_APP_PERMISSIONS, REQUEST_UPGRADE_APP_PERMISSION_CODE);
      return;
    }
    // Check SharedPreference
    if (UpgradeManager.isTodayChecked(this)) return;

    NetworkUtils.NetworkType networkType = NetworkUtils.checkNetworkStatus(this);
    // Get version code and compare.
    new UpgradeManager(this).checkVersion(networkType);
  }


}
