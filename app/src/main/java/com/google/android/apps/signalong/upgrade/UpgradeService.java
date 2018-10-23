package com.google.android.apps.signalong.upgrade;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by yonglew@ on 10/22/18
 */
public interface UpgradeService {

  @GET
  Observable<Integer> checkVersion(@Url String url);

  @Streaming
  @GET
  Observable<ResponseBody> downloadApk(@Url String url);
}
