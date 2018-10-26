package com.google.android.apps.signalong.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore.Video;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.apps.signalong.CameraActivity;
import com.google.android.apps.signalong.IntroRecordActivity;
import com.google.android.apps.signalong.IntroReviewActivity;
import com.google.android.apps.signalong.VideoReviewActivity;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.IntroSharedPreferences.IntroType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityUtils {
  private static final String TAG = "ActivityUtils";

  /* The code requesting record video permission is used to compare whether to permit or reject.*/
  public static final int REQUEST_RECORD_VIDEO_PERMISSIONS = 5;

  /* The code requesting review video permission is used to compare whether to permit or reject.*/
  public static final int REQUEST_REVIEW_VIDEO_PERMISSIONS = 6;


  public static final int REQUEST_UPGRADE_APP_PERMISSION_CODE = 7;

  public static final String[] UPGRADE_APP_PERMISSIONS = {
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  };

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

  /* Application permission map for the corresponding activities. */
  public static final HashMap<Integer, String[]> REQUEST_PERMISSION_MAP = new HashMap<>();

  static {
    REQUEST_PERMISSION_MAP.put(REQUEST_RECORD_VIDEO_PERMISSIONS, RECORD_VIDEO_PERMISSIONS);
    REQUEST_PERMISSION_MAP.put(REQUEST_REVIEW_VIDEO_PERMISSIONS, REVIEW_VIDEO_PERMISSIONS);
  }

  public static void startReviewActivity(
      Activity activity, @Nullable List<VideoListResponse.DataBeanList.DataBean> reviewTaskList) {
    Intent intent = new Intent(activity.getApplicationContext(),
        VideoReviewActivity.class);
    if (reviewTaskList != null) {
      intent.putParcelableArrayListExtra(
          VideoReviewActivity.REVIEW_TASK_PARAM,
          new ArrayList<>(reviewTaskList));
    }
    if (hasPermission(activity.getApplicationContext(), REVIEW_VIDEO_PERMISSIONS)) {
      triggerIntroOrTargetActivity(activity, IntroReviewActivity.class, VideoReviewActivity.class,
          IntroType.REVIEW, intent);
    } else {
      activity.requestPermissions(REVIEW_VIDEO_PERMISSIONS, REQUEST_REVIEW_VIDEO_PERMISSIONS);
    }
  }

  public static List<VideoListResponse.DataBeanList.DataBean> parseReviewTaskFromIntent(
      Activity activity) {
    Intent intent = activity.getIntent();
    ArrayList<VideoListResponse.DataBeanList.DataBean> videoListDatabeans =
        intent.getParcelableArrayListExtra(VideoReviewActivity.REVIEW_TASK_PARAM);
    Log.i(TAG, "parsed video list response databeans from intent " + videoListDatabeans);
    if (videoListDatabeans == null || videoListDatabeans.isEmpty()) {
      return null;
    } else {
      return videoListDatabeans;
    }
  }

  public static void startCameraActivity(
      Activity activity,
      @Nullable SignPromptBatchResponse recordTaskList) {
    Intent intent = new Intent(activity.getApplicationContext(), CameraActivity.class);
    if (recordTaskList != null) {
      intent.putParcelableArrayListExtra(CameraActivity.RECORDING_TASK_PARAM,
          new ArrayList<>(recordTaskList.getData()));
    }
    if (hasPermission(activity.getApplicationContext(), RECORD_VIDEO_PERMISSIONS)) {
      triggerIntroOrTargetActivity(activity, IntroRecordActivity.class, CameraActivity.class,
          IntroType.RECORD, intent);
    } else {
      activity.requestPermissions(RECORD_VIDEO_PERMISSIONS, REQUEST_RECORD_VIDEO_PERMISSIONS);
    }
  }

  public static List<SignPromptBatchResponse.DataBean> parseRecordingTaskFromIntent(
      Activity activity) {
    Intent intent = activity.getIntent();
    ArrayList<SignPromptBatchResponse.DataBean> promptDatabeans =
        intent.getParcelableArrayListExtra(CameraActivity.RECORDING_TASK_PARAM);
    Log.i(TAG, "parsed video list response databeans from intent " + promptDatabeans);
    if (promptDatabeans == null || promptDatabeans.isEmpty()) {
      return null;
    } else {
      return promptDatabeans;
    }
  }


  public static void triggerIntroOrTargetActivity(Activity activity,
      Class<?> introActivityClass, Class<?> targetActivityClass,
      IntroType introType, Intent targetActivityIntent) {
    Context context = activity.getApplicationContext();
    Intent targetIntent = null;
    if (!IntroSharedPreferences.getIntroValue(context, introType)) {
      IntroSharedPreferences.saveIntroValue(context, introType, true);
      targetIntent = new Intent(context, introActivityClass);
    } else {
      targetIntent = targetActivityIntent == null ?
          new Intent(context, targetActivityClass) : targetActivityIntent;
    }
    if (targetIntent != null) {
      activity.startActivity(targetIntent);
    }
  }

  public static boolean hasPermission(Context context, String[] permissions) {
    for (String permission : permissions) {
      if (ActivityCompat.checkSelfPermission(context, permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

}
