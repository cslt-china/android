package com.google.android.apps.signalong.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** VideoScreenUtils used to take a screenshot from the video. */
public class VideoScreenUtils {

  private static final String TAG = "VideoScreenUtils";
  /* Suffix of the screenshot file.*/
  private static final String IMAGE_SUFFIX = ".png";
  /* Screenshot quality.*/
  private static final Integer QUALITY = 100;
  /* mediaMetadataRetriever provides metadata obtained from video file.*/
  private static final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

  public static boolean screenFromVideo(String videoPath, String imagePath) {
    assert(videoPath != null && imagePath != null);

    try {
      mediaMetadataRetriever.setDataSource(videoPath);
      String duration =
          mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
      Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(Integer.parseInt(duration) / 2);
      FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
      if (!bitmap.compress(CompressFormat.PNG, QUALITY, fileOutputStream)) {
        return false;
      }
      fileOutputStream.close();
    } catch (IllegalArgumentException | IOException e) {
      Log.d(TAG, e.getMessage());
      return false;
    }
    return true;
  }

  //For compatibility with other code
  public static String screenFromVideo(String videoPath) {
    String imagePath = getImagePath();
    if (screenFromVideo(videoPath, imagePath)) {
      return imagePath;
    } else {
      return null;
    }
  }

  private static String getImagePath() {

    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()
        + File.separator
        + System.currentTimeMillis()
        + IMAGE_SUFFIX;
  }
}
