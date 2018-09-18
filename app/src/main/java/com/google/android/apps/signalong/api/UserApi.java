package com.google.android.apps.signalong.api;

import com.google.android.apps.signalong.jsonentities.AuthResponse;
import com.google.android.apps.signalong.jsonentities.ProfileResponse;
import com.google.android.apps.signalong.jsonentities.RefreshRequest;
import com.google.android.apps.signalong.jsonentities.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/** The UserApi interface provides login service. */
public interface UserApi {

  @POST("/api/auth/obtain/")
  Call<AuthResponse> login(@Body User user);

  @POST("/api/auth/refresh/")
  Call<AuthResponse> refresh(@Body RefreshRequest refreshRequest);

  /**
   * getProfile is be used to get the user details include the score of reviews video and the score
   * of the quality recorded video etc. to understand how myself perform.
   *
   * @param authenticate access jsonTokenId.
   * @return the user details data. if the access jsonTokenId is invalid, return no.
   */
  @GET("/api/profile/")
  Call<ProfileResponse> getProfile(@Header("Authorization") String authenticate);
}
