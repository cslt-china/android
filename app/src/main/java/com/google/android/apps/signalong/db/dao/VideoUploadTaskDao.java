package com.google.android.apps.signalong.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;
import java.util.List;

/** VideoUploadTaskDao provides insert and delete and get all operate. */
@Dao
public interface VideoUploadTaskDao {

  @Query("SELECT * FROM videouploadtask")
  List<VideoUploadTask> getAll();

  @Insert
  void insert(VideoUploadTask videoUploadTask);

  @Delete
  void delete(VideoUploadTask videoUploadTask);

  @Update
  void update(VideoUploadTask videoUploadTask);

  @Query("select * from videouploadtask where id=:id")
  VideoUploadTask get(Integer id);
}
