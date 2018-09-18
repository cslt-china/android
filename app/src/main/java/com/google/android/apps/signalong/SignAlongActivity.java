package com.google.android.apps.signalong;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.ProfileResponse.DataBean.ScoresBean;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.ToastUtils;
import java.util.ArrayList;

/** Android signalong App. */
public class SignAlongActivity extends BaseActivity {
  /* This provides parameters for communication between activities for videoReviewActivity. */
  public static final String UNREVIEW_PARAM = "unreview_param";
  private static final int SPAN_COUNT = 4;
  /* Application permission list contains camera, recording voice and write Sd card permissions.*/
  private static final String[] RECORD_VIDEO_PERMISSIONS = {
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO
  };
  /* Application permission list contains watch video to write Sd card permissions.*/
  private static final String[] REVIEW_VIDEO_PERMISSIONS = {
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
  };
  /* The code requesting record video permission is used to compare whether to permit or reject.*/
  private static final int REQUEST_RECORD_VIDEO_PERMISSIONS = 5;
  /* The code requesting review video permission is used to compare whether to permit or reject.*/
  private static final int REQUEST_REVIEW_VIDEO_PERMISSIONS = 6;
  private SignAlongViewModel signAlongViewModel;
  private UnreviewedVideoGridAdapter unreviewedVideoGridAdapter;
  private Intent intentToVideoReview;

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
              } else {
                startLoginActivity();
              }
            });
  }

  @Override
  public void initViews() {
    RecyclerView unreviewedVideoListRecyclerView =
        (RecyclerView) findViewById(R.id.unvote_video_list_recyclerview);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    unreviewedVideoGridAdapter = new UnreviewedVideoGridAdapter();
    unreviewedVideoListRecyclerView.setLayoutManager(
        new GridLayoutManager(getApplicationContext(), SPAN_COUNT));
    unreviewedVideoListRecyclerView.setAdapter(unreviewedVideoGridAdapter);
    findViewById(R.id.setting_button)
        .setOnClickListener(
            view -> {
              startActivityForResult(new Intent(getApplicationContext(), SettingActivity.class), 0);
            });
    findViewById(R.id.record_video_button)
        .setOnClickListener(
            view -> {
              startCameraActivity();
            });
    findViewById(R.id.floatingactionbutton_record_activty)
        .setOnClickListener(
            view -> {
              startCameraActivity();
            });
    unreviewedVideoGridAdapter.setItemListener(
        (position, pagedList) -> {
          ArrayList<DataBean> unreviewedVideoList = new ArrayList<>();
          for (int i = 0; i <= position; i++) {
            unreviewedVideoList.add(pagedList.get(i));
          }
          intentToVideoReview = new Intent(getApplicationContext(), VideoReviewActivity.class);
          intentToVideoReview.putParcelableArrayListExtra(UNREVIEW_PARAM, unreviewedVideoList);
          startVideoReviewActivity();
        });
    findViewById(R.id.points_textview)
        .setOnClickListener(
            view -> startActivity(new Intent(getApplicationContext(), MyVideoActivity.class)));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
    if (resultCode == LoginActivity.LOGIN_SUCCESS) {
      initUserData();
    } else if (resultCode == LoginActivity.LOGIN_FAIL) {
      finish();
    } else if (resultCode == SettingActivity.LOGOUT_SUCCESS) {
      startLoginActivity();
    }
  }

  private void startCameraActivity() {
    if (isHavePermission(RECORD_VIDEO_PERMISSIONS)) {
      startActivity(new Intent(getApplicationContext(), CameraActivity.class));
    } else {
      requestPermissions(RECORD_VIDEO_PERMISSIONS, REQUEST_RECORD_VIDEO_PERMISSIONS);
    }
  }

  private void startVideoReviewActivity() {
    if (isHavePermission(REVIEW_VIDEO_PERMISSIONS)) {
      startActivity(intentToVideoReview);
    } else {
      requestPermissions(REVIEW_VIDEO_PERMISSIONS, REQUEST_REVIEW_VIDEO_PERMISSIONS);
    }
  }

  private void initUserData() {
    signAlongViewModel
        .getCurrentPointAndUsernameLiveData()
        .observe(
            this,
            currentPointAndUsername -> {
              if (currentPointAndUsername == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                return;
              }
              ((TextView) findViewById(R.id.username_textview))
                  .setText(
                      String.format(
                          getString(R.string.label_current_points),
                          currentPointAndUsername.getData().getUsername()));
              ScoresBean scoresBean = currentPointAndUsername.getData().getScores();
              ((TextView) findViewById(R.id.points_textview))
                  .setText(
                      String.valueOf(
                          scoresBean.getVideoQualityScore()
                              + scoresBean.getGlossCreationScore()
                              + scoresBean.getVideoCreationScore()
                              + scoresBean.getVideoReviewScore()));
            });
    signAlongViewModel
        .getPersonalUnapprovedVideoCount()
        .observe(
            this,
            personalNotApproveVideoCount -> {
              if (personalNotApproveVideoCount == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                return;
              }
              ((TextView) findViewById(R.id.personal_unapproved_video_count_textview))
                  .setText(
                      String.format(
                          getString(R.string.label_personal_unapproved_video_count),
                          personalNotApproveVideoCount));
            });
    signAlongViewModel
        .getPersonalVideoCount()
        .observe(
            this,
            personalVideoCount -> {
              if (personalVideoCount == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                return;
              }
              ((TextView) findViewById(R.id.personal_video_count_textview))
                  .setText(
                      String.format(
                          getString(R.string.label_personal_video_count), personalVideoCount));
            });
    signAlongViewModel
        .getUnreviewVideoCount()
        .observe(
            this,
            unreviewedVideoCount -> {
              if (unreviewedVideoCount == null) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
                return;
              }
              ((TextView) findViewById(R.id.unreview_video_count_textview))
                  .setText(
                      String.format(
                          getString(R.string.label_unreview_video_count), unreviewedVideoCount));
            });
    signAlongViewModel
        .getUnreviewedVideoList()
        .observe(
            this,
            unreviewedVideoList -> {
              unreviewedVideoGridAdapter.submitList(unreviewedVideoList);
            });
  }

  private void startLoginActivity() {
    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
    startActivityForResult(intent, 0);
  }

  @Override
  protected void onResume() {
    super.onResume();
    initUserData();
  }

  private boolean isHavePermission(String[] permissions) {
    for (String permission : permissions) {
      if (ActivityCompat.checkSelfPermission(this, permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] permissionsStatus) {

    if (requestCode == REQUEST_RECORD_VIDEO_PERMISSIONS) {
      if (isHavePermission(permissions)) {
        startActivity(new Intent(getApplicationContext(), CameraActivity.class));
      } else {
        ToastUtils.show(getApplicationContext(), getString(R.string.tip_refuse_permission));
      }
    } else if (requestCode == REQUEST_REVIEW_VIDEO_PERMISSIONS) {
      if (isHavePermission(permissions)) {
        startActivity(intentToVideoReview);
      } else {
        ToastUtils.show(getApplicationContext(), getString(R.string.tip_refuse_permission));
      }
    }
    super.onRequestPermissionsResult(requestCode, permissions, permissionsStatus);
  }
}
