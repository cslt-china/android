package com.google.android.apps.signalong.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The ApiHelper class provides an api helper in singleton mode. */
public class ApiHelper {
  public static final String PROTOCOL = "http";
  public static final String DOMAIN_NAME = "140.143.180.224";

  public static final String API_URL = String.format("%s://%s", PROTOCOL, DOMAIN_NAME);

  public static final String MEDIA_BASE_URL = API_URL;

  public static final String AGREEMENTS_BASE_URL = String.format("%s://%s/%s", PROTOCOL, DOMAIN_NAME, "agreements");

  static {
    new ApiHelper();
  }

  private static Retrofit retrofit;

  {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(Level.BASIC);
    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    httpClient.addInterceptor(logging);
    retrofit =
        new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build();
  }

  public static Retrofit getRetrofit() {
    return retrofit;
  }
}
