package com.google.android.apps.signalong.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The ApiHelper class provides an api helper in singleton mode. */
public class ApiHelper {
  public static final String PROTOCOL = "https";
  // Change to 140.143.180.224 once the Qcloud https host is setup;
  public static final String DOMAIN_NAME = "cslt-211408.appspot.com";
  public static final String API_URL = String.format("%s://%s", PROTOCOL, DOMAIN_NAME);

  public static final String MEDIA_BASE_URL = API_URL;

  // Change to  String.format("%s://%s/%s", PROTOCOL, DOMAIN_NAME, "agreements"); once
  // Qcloud https host is setup;
  public static final String AGREEMENTS_BASE_URL = "https://storage.googleapis.com/cslt-211408.appspot.com/static/agreements";
  
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
