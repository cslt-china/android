package com.google.android.apps.signalong.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/** NetworkReceiver is used to monitor whether the network is connected. */
public class NetworkReceiver extends BroadcastReceiver {

  /** Call this method when the connection is successful. */
  public interface ListenerNetwork {
    void onConnect();
  }

  private final ListenerNetwork listenerNetwork;

  public NetworkReceiver(ListenerNetwork listenerNetwork) {
    this.listenerNetwork = listenerNetwork;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) {
      throw new NullPointerException("ConnectivityManager must not be NULL.");
    }
    @SuppressLint("MissingPermission")
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
    if (isConnected) {
      listenerNetwork.onConnect();
    }
  }
}
