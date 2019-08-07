package com.google.android.apps.signalong.api;

import android.util.Log;

import com.google.android.apps.signalong.BuildConfig;
import com.google.android.apps.signalong.utils.ConfigUtils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** The ApiHelper class provides an api helper in singleton mode. */
public class ApiHelper {
  public static final String PROTOCOL = ConfigUtils.getProtocol();
  // Change to 140.143.180.224 once the Qcloud https host is setup;
  public static final String DOMAIN_NAME = ConfigUtils.getDomainName();
  public static final String API_URL = String.format("%s://%s", PROTOCOL, DOMAIN_NAME);

  public static final String MEDIA_BASE_URL = API_URL;

  // Change to  String.format("%s://%s/%s", PROTOCOL, DOMAIN_NAME, "agreements"); once
  // Qcloud https host is setup;
  public static final String AGREEMENTS_BASE_URL = ConfigUtils.getAgreementsUrl();//合同获取接口

  static {
    new ApiHelper();
  }

  private static Retrofit retrofit;

  {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.e("jk","retrofitBack = "+message));
    logging.setLevel(Level.BODY);


    OkHttpClient.Builder builder = getHttpClientBuilder(!BuildConfig.DEBUG);
    builder.addInterceptor(logging);

    retrofit =
        new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(builder.build())
            .build();
  }

  public static Retrofit getRetrofit() {
    return retrofit;
  }

  private static OkHttpClient.Builder getHttpClientBuilder(boolean isHttps) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if(isHttps) {

      final TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          @Override
          public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
          }

          @Override
          public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
          }

          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
          }
        }
      };

      final SSLContext sslContext;
      try {
        sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      } catch (KeyManagementException e) {
        e.printStackTrace();
      }
    }
    return builder;
  }
}
