package com.google.android.apps.signalong;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.os.Environment;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.service.DownloadFileService;
import com.google.android.apps.signalong.service.DownloadFileService.DownloadStatusType;
import com.google.android.apps.signalong.service.DownloadFileServiceImpl;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

import java.io.File;
import java.io.IOException;

public class VideoViewFragment extends BaseFragment {
  private static final String TAG = "[VideoViewFragment]";

  private View viewContainer;
  private VideoView videoView;
  private ProgressBar downloadProgressBar;
  private MediaController mediaController;
  private String tmpFileForPlayer;
  private File tmpDir;

  private DownloadFileService downloadFileService;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    downloadFileService = new DownloadFileServiceImpl(
        ApiHelper.getRetrofit().create(VideoApi.class));
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_video_view, container, false);

    downloadProgressBar = viewContainer.findViewById(R.id.video_loading_progressbar);

    videoView = viewContainer.findViewById(R.id.video_view);
    mediaController = new MediaController(getActivity());
    mediaController.setAnchorView(videoView);

    videoView.setOnErrorListener((MediaPlayer mp, int what, int extra)->{
      Log.e(TAG, String.format("player error %d, %d", what, extra));
        return false;
    });

    tmpDir = new File(this.getActivity().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "temp_videos");
    tmpDir.mkdirs();
    tmpFileForPlayer = new File(tmpDir, String.format("player_tmp_file_%d", hashCode())).getPath();

    return viewContainer;
  }

  @Override
  public void onDestroy() {
    if (tmpDir != null) {
      FileUtils.clearDir(tmpDir.getPath());
    }
    super.onDestroy();
  }

  public void setVideoViewCompletionListener(MediaPlayer.OnCompletionListener listener) {
    videoView.setOnCompletionListener(listener);
  }

  public void setVisibility(int visibility) {
    videoView.setVisibility(visibility);
    downloadProgressBar.setVisibility(visibility);
    super.setVisibility(visibility);
  }

  public void stopPlayback() {
    if (videoView.isPlaying()) {
      videoView.stopPlayback();
      // This is necessary for the video view to play out a new video without displaying
      // the stopped one.
      videoView.setVisibility(View.GONE);
      videoView.setVisibility(View.VISIBLE);
    }
  }

  public void viewVideo(String videoPath) {
      if (videoView == null) {
        Log.e(TAG, "No VideoView element!");
        return;
      }
      stopPlayback();

      Log.i(TAG, "view video from " + videoPath);

      if (FileUtils.isFileExist(videoPath)) {
        Log.i(TAG, "file already exists, no need to download.");
        startPlayback(videoPath);
      } else {
        Log.i(TAG, "file is not loaded to local disk, start downloading");
        String localVideoPath =
            new File(tmpDir, FileUtils.extractFileName(videoPath)).getPath();
        //TODO: remove the file after playback
        this.getVideoFile(videoPath, localVideoPath)
            .observe(
                this,
                downloadStatusData -> {
                  switch (downloadStatusData) {
                    case SUCCESS:
                      downloadProgressBar.setVisibility(View.GONE);
                      startPlayback(localVideoPath);
                      break;
                    case LOADING:
                      downloadProgressBar.setVisibility(View.VISIBLE);
                      break;
                    case FAIL:
                      ToastUtils.show(getActivity().getApplicationContext(),
                          getString(R.string.tip_video_download_failure));
                      Log.e("Failed downloading %s", videoPath);
                      break;
                  }
                });
      }
  }

  private void startPlayback(String localVideoPath) {
    if (videoView.isPlaying()) {
      videoView.stopPlayback();
    }

    try {
      //copy to a const tmp file is to prevent videoView play a removed file,
      //when its visibility is changed.
      FileUtils.copy(localVideoPath, tmpFileForPlayer);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    videoView.setVideoURI(FileUtils.buildUri(tmpFileForPlayer));
    videoView.setMediaController(mediaController);
    videoView.start();
  }

  private LiveData<DownloadStatusType> getVideoFile(String downloadUrl, String outputFilepath) {
    return downloadFileService.downloadFile(downloadUrl, outputFilepath);
  }
}
