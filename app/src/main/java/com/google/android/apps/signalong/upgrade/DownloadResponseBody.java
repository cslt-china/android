package com.google.android.apps.signalong.upgrade;


import java.io.IOException;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by yonglew@ on 10/22/18
 */
public class DownloadResponseBody extends ResponseBody {

  private ResponseBody responseBody;

  private DownloadListener downloadListener;

  private BufferedSource bufferedSource;

  public DownloadResponseBody(ResponseBody responseBody, DownloadListener listener) {
    this.responseBody = responseBody;
    this.downloadListener = listener;
  }

  @Nullable
  @Override
  public MediaType contentType() {
    return responseBody.contentType();
  }

  @Override
  public long contentLength() {
    return responseBody.contentLength();
  }

  @Override
  public BufferedSource source() {
    if (bufferedSource == null) {
      bufferedSource = Okio.buffer(source(responseBody.source()));
    }
    return bufferedSource;
  }

  private Source source(Source source) {
    return new ForwardingSource(source) {
      long totalBytesRead = 0;

      @Override
      public long read(Buffer sink, long byteCount) throws IOException {
        long bytesRead = super.read(sink, byteCount);
        totalBytesRead += bytesRead == -1 ? 0 : bytesRead;
        if (null != downloadListener) {
          if (bytesRead != -1) {
            int progress = (int) (totalBytesRead * 100 / responseBody.contentLength());
            downloadListener.onProgress(progress);
          }
        }
        return bytesRead;
      }
    };
  }
}
