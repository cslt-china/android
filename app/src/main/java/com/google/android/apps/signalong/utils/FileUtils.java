package com.google.android.apps.signalong.utils;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;

/** FileUtils wraps a variety of useful file processing. */
public class FileUtils {

  /* Suffix of video file.*/
  private static final String VIDEO_SUFFIX = ".mp4";

  public static String extractFileName(String path) {
    if (!TextUtils.isEmpty(path) && path.endsWith(VIDEO_SUFFIX)) {
      return new File(path).getName();
    }
    return null;
  }

  public static boolean isFileExist(String filePath) {
    return !TextUtils.isEmpty(filePath) && new File(filePath).exists();
  }

  /**
   * Used to build a local video file path for the file name.
   *
   * @param fileName is file name such as ***.mp4.
   * @return build local video file path such as /storage/emulated/***.mp4.
   */
  public static String buildLocalVideoFilePath(String fileName) {
    return TextUtils.isEmpty(fileName)
        ? null
        : Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath()
           + File.separator + fileName;
  }

  public static Uri buildUri(String filename) {
    return Uri.fromFile((new File(filename)));
  }

  public static Uri buildLocalVideoUri(String filename) {
    return Uri.fromFile(new File(buildLocalVideoFilePath(filename)));
  }

  public static void clearFile(String filePath) {
    File file = new File(filePath);
    if (!TextUtils.isEmpty(filePath) && file.exists() && file.isFile()) {
      file.delete();
    }
  }
}
