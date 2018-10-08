package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;

/**
 * The UploadVideoResponse class is the upload video response body. This is autogenerated by running
 * GSON format plugin from Android Studio.
 */
public class UploadVideoResponse extends BaseResponse {

  private DataBean data;

  public DataBean getData() {
    return data;
  }

  public void setData(DataBean data) {
    this.data = data;
  }

  /** The DataBean class is real data, that contains user id, video and thumbnail. */
  public static class DataBean {

    private String uuid;

    @SerializedName("user_id")
    private int userId;

    private String video;

    public int getUserId() {
      return userId;
    }

    public void setUserId(int userId) {
      this.userId = userId;
    }

    private String thumbnail;

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }

    public String getVideo() {
      return video;
    }

    public void setVideo(String video) {
      this.video = video;
    }

    public String getThumbnail() {
      return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
      this.thumbnail = thumbnail;
    }
  }
}
