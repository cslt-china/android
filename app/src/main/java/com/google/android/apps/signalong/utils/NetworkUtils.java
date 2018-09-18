package com.google.android.apps.signalong.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/** NetworkUtils is be used to detect network conditions such as whether it is in wifi or mobile. */
public class NetworkUtils {
  /** The type of network that this object specifies. */
  public enum NetworkType {
    /** In wifi network state. */
    NETWORK_WIFI,
    /* In no network state.*/
    NETWORK_NONE,
    /* In mobile network state.*/
    NETWORK_MOBILE
  }

  @SuppressLint("MissingPermission")
  public static NetworkType checkNetworkStatus(Context context) {
    ConnectivityManager connManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (null != connManager
        && null != connManager.getActiveNetworkInfo()
        && connManager.getActiveNetworkInfo().isAvailable()) {
      NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      if (null != wifiInfo) {
        NetworkInfo.State state = wifiInfo.getState();
        if (null != state && state == NetworkInfo.State.CONNECTED) {
          return NetworkType.NETWORK_WIFI;
        }
      }
      NetworkInfo mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      if (mobileInfo != null) {
        NetworkInfo.State state = mobileInfo.getState();
        if (null != state && state == NetworkInfo.State.CONNECTED) {
          return NetworkType.NETWORK_MOBILE;
        }
      }
    }
    return NetworkType.NETWORK_NONE;
  }
}
