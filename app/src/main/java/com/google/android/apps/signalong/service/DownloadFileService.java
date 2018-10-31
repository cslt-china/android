package com.google.android.apps.signalong.service;

import android.arch.lifecycle.LiveData;

/** DownloadFileService provides download service. */
public interface DownloadFileService {

  /** The type of statusType that this object specifies. */

  public class DownloadStatus {
    public enum Type {
      /* Download fail code.*/
      FAILED,
      /* Download success code.*/
      SUCCESS,
      /* Downloading code.*/
      LOADING
    }

    public final Type statusType;
    public final int percent;
    public final String errorMessage;

    public DownloadStatus(Type statusType,
                          int percent,
                          String errorMessage) {
      this.statusType = statusType;
      this.percent = percent;
      this.errorMessage = errorMessage;
    }

    static DownloadStatus sucessStatus() {
      return new DownloadStatus(Type.SUCCESS, 100, null);
    }

    static DownloadStatus loadingStatus(int percent) {
      return new DownloadStatus(Type.LOADING, percent, null);
    }

    static DownloadStatus failedStatus(String errorMessage, int percent) {
      return new DownloadStatus(Type.FAILED, percent, errorMessage);
    }

    static DownloadStatus failedStatus(String errorMessage) {
      return failedStatus(errorMessage, 0);
    }
  }
  /**
   * Download file and save file.
   *
   * @param downloadUrl
   * @param outputFilepath save the downloaded file as a path.
   * @return used to observe the download status.
   */
  LiveData<DownloadStatus> downloadFile(String downloadUrl, String outputFilepath);
}
