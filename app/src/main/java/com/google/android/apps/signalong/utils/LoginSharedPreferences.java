package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.google.android.apps.signalong.jsonentities.Token;
import com.google.common.base.Splitter;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** LoginSharedPreferences class manages token information for authenticated user data access. */
public class LoginSharedPreferences {

  private static final String ACCESS_TOKEN_EXPIRATION_TIMESTAMP = "access_exp";
  private static final String REFRESH_TOKEN_EXPIRATION_TIMESTAMP = "refresh_exp";
  /* Token user ID.*/
  private static final String ACCESS_USER_ID = "access_user";
  /* ACCESS_TOKEN is used to access the api.*/
  private static final String ACCESS_TOKEN = "access_token";
  /* REFRESH_TOKEN is used to renew the access time.*/
  private static final String REFRESH_TOKEN = "refresh_token";

  /* CONFIRMED_AGREEMENT is used to flag the user has confirmed the agreement doc. */
  private static final String CONFIRMED_AGREEMENT = "confirmed_agreement";

  /* CURRENT_USERNAME is used to identity current username info. */
  private static final String CURRENT_USERNAME = "current_username";

  /* Package names.*/
  private static final String PACKAGE_NAME = "LoginSharedPreferences";
  /* Used to maintain this format for normal request verification.*/
  private static final String HEADER_TOKEN_PREFIX = "Bearer ";

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(LoginSharedPreferences.PACKAGE_NAME, Context.MODE_PRIVATE);
  }

  /**
   * Extract payload section from a JSON web token.
   *
   * @param jsonWebToken a String of JSON web token in the pattern of HEADER.PAYLOAD.SIGNATURE.
   * @return The PAYLOAD section from the input JSON web token.
   */
  private static String getPayloadContent(String jsonWebToken) {
    List<String> pieces = Splitter.on('.').splitToList(jsonWebToken);
    if (pieces.size() == 3) {
      return pieces.get(1);
    }
    return null;
  }

  private static Token extractToken(String content) {
    String decodeResult =
        new String(Base64.decode(getPayloadContent(content), android.util.Base64.DEFAULT));
    Gson gson = new Gson();
    return gson.fromJson(decodeResult, Token.class);
  }

  public static void saveAccessUserData(Context context, String content) {
    Token token = extractToken(content);
    if (token == null) {
      return;
    }
    getSharedPreferences(context)
        .edit()
        .putString(ACCESS_TOKEN, HEADER_TOKEN_PREFIX + content)
        .putLong(ACCESS_TOKEN_EXPIRATION_TIMESTAMP, token.getTokenExpirationTimestamp())
        .putInt(ACCESS_USER_ID, token.getUserId())
        .apply();
  }

  public static void saveCurrentUserName(Context context, String username) {
    getSharedPreferences(context)
      .edit()
      .putString(CURRENT_USERNAME, username)
      .apply();
  }

  public static void saveRefreshUserData(Context context, String content) {
    Token token = extractToken(content);
    if (token == null) {
      return;
    }
    getSharedPreferences(context)
        .edit()
        .putString(REFRESH_TOKEN, HEADER_TOKEN_PREFIX + content)
        .putLong(REFRESH_TOKEN_EXPIRATION_TIMESTAMP, token.getTokenExpirationTimestamp())
        .apply();
  }


  public static void saveConfirmedAgreement(Context context, String value) {
    Set<String> agreements = getSharedPreferences(context).getStringSet(CONFIRMED_AGREEMENT, new HashSet<>());
    agreements.add(value);
    getSharedPreferences(context)
      .edit()
      .putStringSet(CONFIRMED_AGREEMENT, agreements)
      .apply();
  }

  public static long getAccessExp(Context context) {
    return getSharedPreferences(context).getLong(ACCESS_TOKEN_EXPIRATION_TIMESTAMP, 0);
  }

  public static long getRefreshExp(Context context) {
    return getSharedPreferences(context).getLong(REFRESH_TOKEN_EXPIRATION_TIMESTAMP, 0);
  }

  public static Integer getAccessUserId(Context context) {
    return getSharedPreferences(context).getInt(ACCESS_USER_ID, -1);
  }

  public static String getCurrentUsername(Context context) {
    return getSharedPreferences(context).getString(CURRENT_USERNAME, "");
  }

  public static String getAccessToken(Context context) {
    return getSharedPreferences(context).getString(ACCESS_TOKEN, "");
  }

  public static String getRefreshToken(Context context) {
    return getSharedPreferences(context).getString(REFRESH_TOKEN, "");
  }

  public static boolean getConfirmedAgreement(Context context, String value) {
    return getSharedPreferences(context).getStringSet(CONFIRMED_AGREEMENT, new HashSet<>()).contains(value);
  }

  public static void clearUserData(Context context) {
    Set<String> agreements = getSharedPreferences(context).getStringSet(CONFIRMED_AGREEMENT, new HashSet<>());
    getSharedPreferences(context).edit().clear().apply();
    getSharedPreferences(context)
      .edit()
      .putStringSet(CONFIRMED_AGREEMENT, agreements)
      .apply();
  }
}
