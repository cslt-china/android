package com.google.android.apps.signalong.service;

import android.arch.lifecycle.LiveData;

/** DownloadFileService provides download service. */
public interface DownloadFileService {

  /** The type of status that this object specifies. */
  public enum DownloadStatusType {
    /* Download fail code.*/
    FAIL,
    /* Download success code.*/
    SUCCESS,
    /* Downloading code.*/
    LOADING
  }
  /**
   * Download file and save file.
   *
   * @param downloadUrl
   * @param outputFilepath save the downloaded file as a path.
   * @return used to observe the download status.
   */
  LiveData<DownloadStatusType> downloadFile(String downloadUrl, String outputFilepath);
}
