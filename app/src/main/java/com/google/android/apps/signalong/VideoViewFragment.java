package com.google.android.apps.signalong;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.media.MediaPlayer;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.service.DownloadFileTask;
import com.google.android.apps.signalong.service.DownloadImageTask;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

import java.io.File;
import java.io.IOException;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

public class VideoViewFragment extends BaseFragment implements
    DownloadFileTask.DownloadFileCallbacks, DownloadImageTask.DownloadImageCallbacks {
  private static final String TAG = "[VideoViewFragment]";

  private View viewContainer;
  private VideoView videoView;
  private Button replayButton;
  private ProgressBar downloadProgressBar;
  private String tmpFileForPlayer;
  private File tmpDir;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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

  @Override
  public void onResume() {
    videoView.setVisibility(View.VISIBLE);
    super.onResume();
  }

  public void setVideoViewCompletionListener(VideoPlayCompletionCallback callback) {
    videoView.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        replayButton.setVisibility(View.VISIBLE);
        callback.onVideoPlayCompletion();
      }
    });
  }

  public void setVisibility(int visibility) {
    videoView.setVisibility(visibility);
    super.setVisibility(visibility);
  }

  public void stopPlayback() {
    if (videoView.isPlaying()) {
      videoView.stopPlayback();
      videoView.setVisibility(View.GONE);
      videoView.setVisibility(View.VISIBLE);
    }
  }

  public void onImageDownloaded(Bitmap bitmap) {
    if (!videoView.isPlaying()) {
      videoView.setBackground(new BitmapDrawable(videoView.getResources(), bitmap));
    }
  }

  public void onDownloadFailure(String errorMessage) {
    ToastUtils.show(getContext(),
        getString(R.string.tip_video_download_failure) + errorMessage);
  }

  public void onProgressUpdate(int progress) {
    downloadProgressBar.setProgress(progress);
  }

  public void onDownloadSuccess(String downloadedFilePath) {
    downloadProgressBar.setVisibility(View.GONE);
    startPlayback(downloadedFilePath);
  }

  protected void replay() {
    replayButton.setVisibility(View.GONE);
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
    if (uri.getScheme() == "file" ) {
      videoView.setBackground(Drawable.createFromPath(uri.getPath()));
    } else {
      String localPath =
          new File(tmpDir, uri.getLastPathSegment()).getPath();

      if (FileUtils.isFileExist(localPath)) {
        videoView.setBackground(Drawable.createFromPath(localPath));
      } else {
        new DownloadImageTask(ApiHelper.getRetrofit().create(VideoApi.class), this).execute(uri.toString());
      }
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

      new DownloadFileTask(ApiHelper.getRetrofit().create(VideoApi.class), this)
          .execute(uri.toString(), localPath);
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
    replayButton.setVisibility(View.GONE);
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

  public interface VideoPlayCompletionCallback {
    void onVideoPlayCompletion();
  }
}
