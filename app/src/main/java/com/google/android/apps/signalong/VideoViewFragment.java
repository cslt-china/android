package com.google.android.apps.signalong;

import android.arch.lifecycle.LiveData;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.service.DownloadFileService;
import com.google.android.apps.signalong.service.DownloadFileService.DownloadStatus;
import com.google.android.apps.signalong.service.DownloadFileServiceImpl;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

import java.io.File;
import java.io.IOException;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

public class VideoViewFragment extends BaseFragment {
  private static final String TAG = "[VideoViewFragment]";

  private View viewContainer;
  private VideoView videoView;
  private Button replayButton;
  private ProgressBar downloadProgressBar;
  private String tmpFileForPlayer;
  private File tmpDir;

  private DownloadFileService downloadVideoFileService;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    downloadVideoFileService = new DownloadFileServiceImpl(
        ApiHelper.getRetrofit().create(VideoApi.class));
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    viewContainer = inflater.inflate(R.layout.fragment_video_view, container, false);

    downloadProgressBar = viewContainer.findViewById(R.id.video_loading_progressbar);

    videoView = viewContainer.findViewById(R.id.video_view);

    videoView.setOnErrorListener((MediaPlayer mp, int what, int extra)->{
      Log.e(TAG, String.format("player error %d, %d", what, extra));
        return false;
    });

    videoView.setOnInfoListener((MediaPlayer mp, int what, int extra)->{
      if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
        //must remove background here, if in preparedListener, player will black
        //for a moment
        videoView.setBackground(null);
      }
      return false;
    });

    replayButton = viewContainer.findViewById(R.id.replay_button);
    replayButton.setOnClickListener((View v)-> replay());

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
    //downloadProgressBar.setVisibility(visibility);
    videoView.setVisibility(visibility);
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

  protected void replay() {
    if (videoView.isPlaying() && videoView.canSeekBackward()) {
      videoView.seekTo(0);
      return;
    } else {
      if (tmpFileForPlayer != null && FileUtils.isFileExist(tmpFileForPlayer)) {
        videoView.setVideoURI(FileUtils.buildUri(tmpFileForPlayer));
        videoView.start();
      }
    }
  }

  //TODO: extract common logic of downloadAndDisplayThumbnail
  //and downloadAndPlayVideo
  private void downloadAndDisplayThumbnail(Uri uri) {
    if (uri.getScheme() == "file") {
      videoView.setBackground(Drawable.createFromPath(uri.getPath()));
    } else {
      String localPath =
          new File(tmpDir, uri.getLastPathSegment()).getPath();

      downloadFile(uri.toString(), localPath)
          .observe(
              this,
              downloadStatusData -> {
                if (downloadStatusData.statusType == DownloadStatus.Type.SUCCESS) {
                  if (!videoView.isPlaying()) {
                    videoView.setBackground(Drawable.createFromPath(localPath));
                  }
                }
              });
    }
  }

  private void downloadAndPlayVideo(Uri uri) {
    if (uri.getScheme() == "file") {
      Log.i(TAG, "file already exists, no need to download.");
      downloadProgressBar.setVisibility(View.GONE);
      startPlayback(uri.getPath());
    } else {
      Log.i(TAG, "file is not loaded to local disk, start downloading");
      String localPath =
          new File(tmpDir, uri.getLastPathSegment()).getPath();

      downloadProgressBar.setMax(100);
      downloadProgressBar.setProgress(0);
      downloadProgressBar.setVisibility(View.VISIBLE);
      downloadProgressBar.bringToFront();

      this.downloadFile(uri.toString(), localPath)
          .observe(
              this,
              downloadStatusData -> {
                switch (downloadStatusData.statusType) {
                  case SUCCESS:
                    downloadProgressBar.setVisibility(View.GONE);
                    startPlayback(localPath);
                    break;
                  case LOADING:
                    downloadProgressBar.setProgress(downloadStatusData.percent);
                    break;
                  case FAILED:
                    ToastUtils.show(getActivity().getApplicationContext(),
                                    getString(R.string.tip_video_download_failure));
                    Log.e("Failed downloading %s", uri.toString());
                    break;
                }
              });
    }
  }

  public void viewVideo(Uri videoUri, Uri thumbnailUri) {
    if (videoView == null) {
      Log.e(TAG, "No VideoView element!");
      return;
    }
    stopPlayback();

    Log.i(TAG, "view video from " + videoUri);

    if (thumbnailUri != null) {
      downloadAndDisplayThumbnail(thumbnailUri);
    }

    downloadAndPlayVideo(videoUri);
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
    videoView.start();
  }

  private LiveData<DownloadFileService.DownloadStatus> downloadFile(
      String downloadUrl, String outputFilepath) {
    return downloadVideoFileService.downloadFile(downloadUrl, outputFilepath);
  }
}
