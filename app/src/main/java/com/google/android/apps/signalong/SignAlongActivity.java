package com.google.android.apps.signalong;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.google.android.apps.signalong.upgrade.UpgradeManager;
import com.google.android.apps.signalong.utils.ActivityUtils;
import com.google.android.apps.signalong.utils.IntroSharedPreferences.IntroType;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.NetworkUtils;
import com.google.android.apps.signalong.utils.ToastUtils;
import com.google.android.apps.signalong.widget.AbortUploadingDialog;

/**
 * Android signalong App.
 */
public class SignAlongActivity extends BaseActivity {
  private static final String[] UPGRADE_APP_PERMISSIONS = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  public static final int REQUEST_UPGRADE_APP_PERMISSION_CODE = 7;

  private SignAlongViewModel signAlongViewModel;
  private CameraViewModel cameraViewModel;

  @Override
  public int getContentView() {
    return R.layout.activity_signalong;
  }

  @Override
  public void init() {
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
                  // Show welcome toast
                  ToastUtils.show(getApplicationContext(),
                      String.format(getString(R.string.tip_welcome), username));
                  // Check for update.
                  checkUpdate();
                }
              } else {
                startLoginActivity();
              }
            });

    cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
  }

  @Override
  public void initViews() {
    Toolbar toolbar = findViewById(R.id.home_toolbar);
    setSupportActionBar(toolbar);
    findViewById(R.id.btn_setting)
        .setOnClickListener(
            view -> {
              startActivityForResult(new Intent(getApplicationContext(), SettingActivity.class), 0);
            });

    SignAlongPagerAdapter adapter = new SignAlongPagerAdapter(getSupportFragmentManager());
    adapter.addFragment(
        getString(R.string.label_recording_task_page_title), new RecordingTaskFragment());
    adapter.addFragment(
        getString(R.string.label_review_task_page_title), new ReviewTaskFragment());
    adapter.addFragment(
        getString(R.string.label_my_recording_page_title), new MyVideoFragment());

    ViewPager viewPager = findViewById(R.id.signalong_viewpager);
    viewPager.setAdapter(adapter);

    ((TabLayout) findViewById(R.id.signalong_tabs)).setupWithViewPager(viewPager);
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
          break;
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

  @Override
  public void finish() {
    AbortUploadingDialog.check(this, getString(R.string.enforce_exit), getString(R.string.cancel),
        cameraViewModel,
        ()->super.finish(), null);
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
              cameraViewModel.startUploadThread();
              if (currentPointAndUsername == null) {
                ((TextView) findViewById(R.id.app_title_textview))
                    .setText(getString(R.string.label_loading));
                return;
              } else {
                ((TextView) findViewById(R.id.app_title_textview))
                    .setText(getString(R.string.app_name));
              }
            });
  }

  public void onFailureResponse() {
    ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
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
