package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** VideoRecordingSharedPreferences class manages record video config. */
public class VideoRecordingSharedPreferences {

  /** The type of Timings that this object specifies. */
  public enum TimingType {
    // The count down timing for user to get ready for the next sign recording.
    PREPARE_TIME,
    // The designated recording time per sign video.
    RECORD_TIME_SCALE,
  }

  private static final String PACKAGE_NAME = "RecordVideoConfig";

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(
        VideoRecordingSharedPreferences.PACKAGE_NAME, Context.MODE_PRIVATE);
  }

  public static void saveTiming(Context context, TimingType timingType, Integer value) {
    getSharedPreferences(context).edit().putInt(timingType.name(), value).commit();
  }

  public static Integer getTiming(Context context, TimingType timingType) {
    if (timingType == TimingType.RECORD_TIME_SCALE) {
      return getSharedPreferences(context).getInt(timingType.name(), 100);
    } else {
      return getSharedPreferences(context).getInt(timingType.name(), 1);
    }
  }
}
