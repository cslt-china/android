package com.google.android.apps.signalong.jsonentities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * The SignPromptBatchResponse class is the create bunch response body. This is autogenerated by
 * running GSON format plugin from Android Studio.
 */
public class SignPromptBatchResponse extends BaseResponse {

  private List<DataBean> data;

  public List<DataBean> getData() {
    return data;
  }

  public void setData(List<DataBean> data) {
    this.data = data;
  }
  /** The DataBean class is real data, that contains id and sample text. */
  public static class DataBean {

    private int id;
    private String text;

    @SerializedName("gloss_type")
    private int glossType;

    @SerializedName("video_count")
    private Object videoCount;

    @SerializedName("sample_video")
    private SampleVideoBean sampleVideo;

    private int duration;

    public int getId() {
      return id;
    }
    public void setId(int id) {
      this.id = id;
    }

    public String getText() {
      return text;
    }
    public void setText(String text) {
      this.text = text;
    }

    public int getGlossType() {
      return glossType;
    }
    public void setGlossType(int glossType) {
      this.glossType = glossType;
    }

    public Object getVideoCount() {
      return videoCount;
    }
    public void setVideoCount(Object videoCount) {
      this.videoCount = videoCount;
    }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public SampleVideoBean getSampleVideo() { return sampleVideo; }
    public void setSampleVideo(SampleVideoBean sampleVideo) { this.sampleVideo = sampleVideo; }

    /** The SampleVideoBean class is real data, that contains video path. */
    public static class SampleVideoBean {

      @SerializedName("video_path")
      private String videoPath;

      public String getVideoPath() {
        return videoPath;
      }

      public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
      }
    }
  }
}
