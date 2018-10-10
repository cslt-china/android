package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;

/**
 * The CreateVideoResponse class is the create video response body. This is autogenerated by running
 * GSON format plugin from Android Studio.
 */
public class CreateVideoResponse extends BaseResponse {

  private DataBean data;

  public DataBean getData() {
    return data;
  }

  public void setData(DataBean data) {
    this.data = data;
  }

  /** The DataBean class is real data, that contains upload key. */
  public static class DataBean {

    @SerializedName("upload_key")
    private String uploadKey;

    public String getUploadKey() {
      return uploadKey;
    }

    public void setUploadKey(String uploadKey) {
      this.uploadKey = uploadKey;
    }
  }
}
