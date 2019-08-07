package com.google.android.apps.signalong.db.dbentities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * The VideoUploadTask class is treated as an entity with video id and path of the image and path of
 * the video.
 */
@Entity
public class VideoUploadTask {

  /**
   * Id identifies each word, and uuid identifies each video's corresponding uploader. User can only
   * upload videos of at most one of the same words, so use id as the primary key of the video
   * upload task.
   */
  @PrimaryKey() private Integer id;

  @ColumnInfo(name = "video_path")
  private String videoPath;

  @ColumnInfo(name = "image_path")
  private String imagePath;

  @ColumnInfo(name = "video_key")
  private String uploadKey;

  public String getUploadKey() {
    return uploadKey;
  }

  public void setUploadKey(String uploadKey) {
    this.uploadKey = uploadKey;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getImagePath() {
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public String getVideoPath() {
    return videoPath;
  }

  public void setVideoPath(String videoPath) {
    this.videoPath = videoPath;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj || (
        obj instanceof VideoUploadTask &&
        this.id.equals(((VideoUploadTask)obj).id) &&
        this.videoPath.equals(((VideoUploadTask)obj).videoPath));
  }

  @Override
  public String toString() {
    return String.format("<UploadTask: %d-%s>", id, videoPath);
  }
}
