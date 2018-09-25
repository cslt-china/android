package com.google.android.apps.signalong.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import com.google.android.apps.signalong.db.dao.VideoUploadTaskDao;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;

/**
 * AppDatabase as the main access point for the underlying connection to your app's persisted,
 * relational data.
 */
@Database(entities = {VideoUploadTask.class},
    version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
  public abstract VideoUploadTaskDao videoUploadTaskDao();

  private static final String DATABASE = "/sdcard/videotask";
  private static volatile AppDatabase instance;

  public static AppDatabase getDatabase(final Context context) {
    if (instance == null) {
      synchronized (AppDatabase.class) {
        if (instance == null) {
          instance =
              Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE)
                  .build();
        }
      }
    }
    return instance;
  }
}
