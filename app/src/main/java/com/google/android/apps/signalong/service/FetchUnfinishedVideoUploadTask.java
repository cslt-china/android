package com.google.android.apps.signalong.service;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import com.google.android.apps.signalong.db.AppDatabase;
import com.google.android.apps.signalong.db.dao.VideoUploadTaskDao;
import com.google.android.apps.signalong.db.dbentities.VideoUploadTask;
import java.util.HashSet;
import java.util.Set;

public class FetchUnfinishedVideoUploadTask extends AsyncTask<Void, Void, Set<Integer>> {

  private Activity activity;
  private Callbacks callbacks;

  public FetchUnfinishedVideoUploadTask(Activity activity,
      Callbacks callbacks) {
    this.activity = activity;
    this.callbacks = callbacks;
  }

  @Override
  protected Set<Integer> doInBackground(Void... prams) {
    VideoUploadTaskDao videoUploadTaskDao = AppDatabase.getDatabase(
        activity.getApplication()).videoUploadTaskDao();

    Set<Integer> uploadingUnfinishedPromptIds = new HashSet<>();
    for (VideoUploadTask task : videoUploadTaskDao.getAll()) {
      uploadingUnfinishedPromptIds.add(task.getId());
    }
    return uploadingUnfinishedPromptIds;
  }

  @Override
  protected void onPostExecute(Set<Integer> uploadingUnfinishedPromptIds) {
    callbacks.onUnfinishedVideoUploadFetched(uploadingUnfinishedPromptIds);
  }

  public interface Callbacks {
    void onUnfinishedVideoUploadFetched(Set<Integer> uploadingUnfinishedPromptIds);
  }
}
