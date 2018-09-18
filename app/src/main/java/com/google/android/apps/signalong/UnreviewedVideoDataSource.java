package com.google.android.apps.signalong;

import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * UnreviewedVideoDataSource is based on the Paging library implement for paging, loading a part of
 * the data.
 */
public class UnreviewedVideoDataSource extends PositionalDataSource<DataBean> {

  private static final String TAG = "UnreviewedVideoDataSource";
  /* Load size per page.*/
  private static final Integer LIMIT_SIZE = 10;
  private final VideoApi videoApi;
  private final String token;
  private Integer totalCount;

  public UnreviewedVideoDataSource(VideoApi videoApi, String token) {
    this.videoApi = videoApi;
    this.token = token;
  }

  @Override
  public void loadInitial(
      @NonNull LoadInitialParams loadInitialParams,
      @NonNull LoadInitialCallback<DataBean> loadInitialCallback) {
    videoApi
        .getUnreviewedVideoList(token, 0, LIMIT_SIZE)
        .enqueue(
            new Callback<VideoListResponse>() {
              @Override
              public void onResponse(
                  Call<VideoListResponse> call, Response<VideoListResponse> response) {
                if (response.isSuccessful()
                    && response.body() != null
                    && response.body().getCode() == 0) {
                  totalCount = response.body().getDataBeanList().getTotal();
                  loadInitialCallback.onResult(response.body().getDataBeanList().getData(), 0);
                }
              }

              @Override
              public void onFailure(Call<VideoListResponse> call, Throwable t) {}
            });
  }

  @Override
  public void loadRange(
      @NonNull LoadRangeParams loadRangeParams,
      @NonNull LoadRangeCallback<DataBean> loadRangeCallback) {

    if (loadRangeParams.startPosition > totalCount) {
      return;
    }
    videoApi
        .getUnreviewedVideoList(token, loadRangeParams.startPosition, LIMIT_SIZE)
        .enqueue(
            new Callback<VideoListResponse>() {
              @Override
              public void onResponse(
                  Call<VideoListResponse> call, Response<VideoListResponse> response) {
                if (response.isSuccessful()
                    && response.body() != null
                    && response.body().getCode() == 0) {
                  totalCount = response.body().getDataBeanList().getTotal();
                  loadRangeCallback.onResult(response.body().getDataBeanList().getData());
                }
              }

              @Override
              public void onFailure(Call<VideoListResponse> call, Throwable t) {}
            });
  }
}
