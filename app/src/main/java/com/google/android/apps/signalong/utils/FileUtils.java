package com.google.android.apps.signalong.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

  public static String buildLocalTempFilePath(Context context, String fileName) {
    return TextUtils.isEmpty(fileName)
           ? null
           : context.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath()
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

  public static long getFileSize(String filePath) {
    File file = new File(filePath);
    return file.length();
  }

  public static void copy(String srcPath, String dstPath) throws IOException {
    try (InputStream in = new FileInputStream(srcPath)) {
      try (OutputStream out = new FileOutputStream(dstPath)) {
        byte[] buf = new byte[1024 * 4];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      }
    }
  }

  public static boolean clearDir(String path) {
    File dir = new File(path);

    if (!dir.exists()) {
      return false;
    }

    File[] files = dir.listFiles();
    if(files != null) {
      for(File file : files) {
        if(file.isDirectory()) {
          clearDir(file.getPath());
        }
        else {
          file.delete();
        }
      }
    }

    return(dir.delete());
  }
}
