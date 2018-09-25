package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * IntroSharedPreferences is used to save the value of the first startup. The default is false,
 * indicating the first startup. Otherwise, it is not the first startup.
 */
public class IntroSharedPreferences {
  /** The type of IntroType that this object specifies. */
  public enum IntroType {
    // Review introduction type.
    REVIEW,
    // Record introduction type.
    RECORD,
  }

  private static final String PACKAGE_NAME = "IntroSharedPreferences";

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
  }

  public static void saveIntroValue(Context context, IntroType introType, Boolean value) {
    getSharedPreferences(context).edit().putBoolean(introType.name(), value).commit();
  }

  public static boolean getIntroValue(Context context, IntroType introType) {
    return getSharedPreferences(context).getBoolean(introType.name(), false);
  }
}
