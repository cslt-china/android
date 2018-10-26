package com.google.android.apps.signalong.upgrade;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by yonglew@ on 10/22/18
 */
public class DownloadInterceptor implements Interceptor {

  private DownloadListener downloadListener;

  public DownloadInterceptor(DownloadListener downloadListener) {
    this.downloadListener = downloadListener;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request original = chain.request();

    // Request customization: add request headers
    Request.Builder requestBuilder = original.newBuilder()
        .header("Accept-Encoding", "identity"); // <-- this is the important line

    Request request = requestBuilder.build();
    Response response = chain.proceed(request);
    ResponseBody body = new DownloadResponseBody(response.body(), downloadListener);
    return response.newBuilder().body(body).build();
  }
}
