package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;

/**
 * The VideoId class is the video id response body. This is autogenerated by running GSON format
 * plugin from Android Studio.
 */
public class VideoId {

  public VideoId(int glossId) {
    this.glossId = glossId;
  }

  @SerializedName("gloss_id")
  private int glossId;

  public int getGlossId() {
    return glossId;
  }

  public void setGlossId(int glossId) {
    this.glossId = glossId;
  }
}
