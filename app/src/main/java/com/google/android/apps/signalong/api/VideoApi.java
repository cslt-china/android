package com.google.android.apps.signalong.api;

import com.google.android.apps.signalong.jsonentities.CreateVideoResponse;
import com.google.android.apps.signalong.jsonentities.ReviewVideoResponse;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse;
import com.google.android.apps.signalong.jsonentities.UploadVideoResponse;
import com.google.android.apps.signalong.jsonentities.VideoId;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/** The VideoApi interface provides upload video and get a batch of unreview videos service. */
public interface VideoApi {

  /**
   * Upload video file and image file.
   *
   * @param authorization access jsonTokenId.
   * @param videoKey identifying the video key associated image file and video file.
   * @param videoFile video file.
   * @param imageFile image file.
   * @return upload status code.
   */
  @Multipart
  @POST("/api/videos/{videoId}/upload")
  Call<UploadVideoResponse> updateVideo(
      @Header("Authorization") String authorization,
      @Path("videoId") String videoKey,
      @Part MultipartBody.Part videoFile,
      @Part MultipartBody.Part imageFile);

  /**
   * Request to create a new video to upload a file.
   *
   * @param authorization access jsonTokenId.
   * @param videoId identify the id for the word.
   * @return the key to uploading a video. if the access jsonTokenId is invalid, return no.
   */
  @POST("/api/videos")
  Call<CreateVideoResponse> createVideo(
      @Header("Authorization") String authorization, @Body VideoId videoId);

  /**
   * Get a batch of words.
   *
   * @param authorization access jsonTokenId.
   * @param limit is used to specify how many data to load per query.
   * @return a batch of data. if the access jsonTokenId is invalid, return no.
   */
  @GET("/api/bunch/")
  Call<SignPromptBatchResponse> getSignPromptBatch(
      @Header("Authorization") String authorization,
      @Query("limit") Integer limit);

  /**
   * Get a batch of unreviewed video.
   *
   * @param authorization access jsonTokenId.
   * @param offset is used to get the data from which to start, and also for paging. For example,
   *     offset=10, then get from the 10th to the 20th data (specify limit=10 load 10 data per
   *     page).
   * @param limit is used to specify how many data to load per page.
   * @return a batch of data according to the given parameters. if the access jsonTokenId is
   *     invalid, return no.
   */
  @GET("/api/videos/unreviewed")
  Call<VideoListResponse> getUnreviewedVideoList(
      @Header("Authorization") String authorization,
      @Query("offset") Integer offset,
      @Query("limit") Integer limit);
  /**
   * Get a batch of personal video.
   *
   * @param authorization access jsonTokenId.
   * @param status is used to obtain a type of video such as approving approved rejected.
   * @param limit is used to specify how many data to load per query.
   * @return a batch of personal video data.
   */
  @GET("/api/videos/self")
  Call<VideoListResponse> getPersonalVideoList(
      @Header("Authorization") String authorization,
      @Query("status") String status,
      @Query("limit") Integer limit);
  /**
   * reviewVideo is used to review for the video.
   *
   * @param authorization access jsonTokenId.
   * @param uuId is identify which video.
   * @param status is review reject or approve.
   * @return whether the review is successful.
   */
  @POST("/api/review/{uuid}/{status}")
  Call<ReviewVideoResponse> reviewVideo(
      @Header("Authorization") String authorization,
      @Path("uuid") String uuId,
      @Path("status") String status);
  /**
   * Download file.
   *
   * @param fileUrl is download by the network.
   * @return already download file.
   */
  @GET
  Call<ResponseBody> downloadFile(@Url String fileUrl);
}
