package com.google.android.apps.signalong.utils;

import com.google.android.apps.signalong.BuildConfig;

/**
 * Created by yonglew@ on 10/19/18
 */
public class ConfigUtils {

  public static final String BUILD_TYPE_RELEASE = "release";
  public static final String BUILD_TYPE_STAGING = "staging";

  private static final String PROTOCOL_HTTPS = "https";
  private static final String PROTOCOL_HTTP = "http";

  private static final String PROD_DOMAIN_NAME = "signalong.googleminiapps.cn";
  private static final String STAGING_DOMAIN_NAME = "cslt-211408.appspot.com";
  private static final String DEV_DOMAIN_NAME = "cslt-211408.appspot.com";

  private static final String PROD_UPDATE_APK_BASE_URL = "https://cslt-211408.firebaseapp.com/";
  private static final String PROD_UPDATE_APK_CHECK_VERSION_URL = "/release-apk/cslt-release-version.txt";
  private static final String PROD_UPDATE_APK_DOWNLOAD_APK_URL = "/release-apk/cslt-release.apk";

  private static final String DEV_UPDATE_APK_BASE_URL = "https://cslt-211408.firebaseapp.com/";
  private static final String DEV_UPDATE_APK_CHECK_VERSION_URL = "/staging-apk/cslt-staging-version.txt";
  private static final String DEV_UPDATE_APK_DOWNLOAD_APK_URL = "/staging-apk/cslt-staging.apk";

  public static String getProtocol() {
    if (BuildConfig.DEBUG) {
      return PROTOCOL_HTTP;
    } else {
      return PROTOCOL_HTTPS;
    }
  }

  public static String getDomainName() {
    if (BUILD_TYPE_RELEASE.equals(BuildConfig.BUILD_TYPE)) {
      return PROD_DOMAIN_NAME;
    } else if (BUILD_TYPE_STAGING.equals(BuildConfig.BUILD_TYPE)) {
      return STAGING_DOMAIN_NAME;
    } else {
      return DEV_DOMAIN_NAME;
    }
  }

  public static String getAgreementsUrl() {
    if (BUILD_TYPE_RELEASE.equals(BuildConfig.BUILD_TYPE)) {
      // "https://signalong.googleminiapps.cn/media/agreements"
      return String.format("%s://%s/media/agreements", getProtocol(), getDomainName());
    } else if (BUILD_TYPE_STAGING.equals(BuildConfig.BUILD_TYPE)) {
      // "https://storage.googleapis.com/cslt-211408.appspot.com/static/agreements"
      return String.format("%s://storage.googleapis.com/%s/static/agreements", getProtocol(), getDomainName());
    } else {
      return String.format("%s://storage.googleapis.com/%s/static/agreements", getProtocol(), getDomainName());
    }
  }

  public static String getUpdateApkBaseUrl() {
    if (BUILD_TYPE_RELEASE.equals(BuildConfig.BUILD_TYPE)) {
      return PROD_UPDATE_APK_BASE_URL;
    } else {
      return DEV_UPDATE_APK_BASE_URL;
    }
  }

  public static String getUpdateApkCheckVersionUrl() {
    if (BUILD_TYPE_RELEASE.equals(BuildConfig.BUILD_TYPE)) {
      return PROD_UPDATE_APK_CHECK_VERSION_URL;
    } else {
      return DEV_UPDATE_APK_CHECK_VERSION_URL;
    }
  }

  public static String getUpdateApkDownloadApkUrl() {
    if (BUILD_TYPE_RELEASE.equals(BuildConfig.BUILD_TYPE)) {
      return PROD_UPDATE_APK_DOWNLOAD_APK_URL;
    } else {
      return DEV_UPDATE_APK_DOWNLOAD_APK_URL;
    }
  }
}
