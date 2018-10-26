package com.google.android.apps.signalong.upgrade;

/**
 * Created by yonglew@ on 10/22/18
 */
public interface DownloadListener {

  void onStartDownload();

  void onProgress(int progress);

  void onFinishDownload();

  void onError(String error);

}
