package com.google.android.apps.signalong;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.service.DownloadFileService;
import com.google.android.apps.signalong.service.DownloadFileService.DownloadStatusType;
import com.google.android.apps.signalong.service.DownloadFileServiceImpl;
import com.google.android.apps.signalong.utils.FileUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

public class VideoViewFragment extends Fragment {
  private static final String TAG = "[VideoViewFragment]";
  private View viewContainer;
  private VideoView videoView;
  private ProgressBar progressBar;

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
    videoView = viewContainer.findViewById(R.id.video_view);
    videoView = viewContainer.findViewById(R.id.video_view);
    progressBar = viewContainer.findViewById(R.id.video_loading_progressbar);
    return viewContainer;
  }

  public void setVideoViewCompletionListener(MediaPlayer.OnCompletionListener listener) {
    videoView.setOnCompletionListener(listener);
  }

  public void stopPlayback() {
    if (videoView.isPlaying()) {
        videoView.stopPlayback();
      }
  }

  public void viewVideo(String videoPath) {
      if (videoView == null) {
        Log.e(TAG, "No VideoView element!");
        return;
      }

      stopPlayback();
      String localVideoPath =
          FileUtils.buildLocalVideoFilePath(FileUtils.extractFileName(videoPath));
      this.getVideoFile(videoPath, localVideoPath)
          .observe(
              this,
              downloadStatusData -> {
                switch (downloadStatusData) {
                  case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    videoView.setVideoPath(localVideoPath);
                    videoView.start();
                    break;
                  case LOADING:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                  case FAIL:
                    ToastUtils.show(null, getString(R.string.tip_video_download_failure));
                    Log.e("Failed downloading %s", videoPath);
                    break;
                }
              });
  }

  private LiveData<DownloadStatusType> getVideoFile(String downloadUrl, String outputFilepath) {
    return downloadFileService.downloadFile(downloadUrl, outputFilepath);
  }


}
