package com.google.android.apps.signalong.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The ApiHelper class provides an api helper in singleton mode. */
public class ApiHelper {

  private static final String API_URL = "https://cslt-211408.appspot.com/";

  public static final String STATIC_URL = "https://storage.googleapis.com/cslt-211408.appspot.com/static/agreements";

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
