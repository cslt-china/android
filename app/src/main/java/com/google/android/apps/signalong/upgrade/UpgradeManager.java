package com.google.android.apps.signalong.upgrade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.apps.signalong.BuildConfig;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.utils.ConfigUtils;
import com.google.android.apps.signalong.utils.NetworkUtils;
import com.google.android.apps.signalong.utils.ToastUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yonglew@ on 10/23/18
 */
public class UpgradeManager {

  public static final String BASE_URL = ConfigUtils.getUpdateApkBaseUrl();
  public static final String CHECK_VERSION_URL = ConfigUtils.getUpdateApkCheckVersionUrl();
  public static final String DOWNLOAD_URL = ConfigUtils.getUpdateApkDownloadApkUrl();

  public static final String FOLDER_DIR = String.format("%s/cslt/apk/", Environment.getExternalStorageDirectory().getAbsoluteFile());
  public static final String DOWNLOADED_APK_NAME = "latest.apk";
  public static final String PROVIDER_PACKAGE_NAME = ".fileprovider";
  private static final int DEFAULT_TIMEOUT = 15;
  private static final String PACKAGE_NAME = UpgradeManager.class.getSimpleName();
  private static final String LAST_CHECK_DATE = "LAST_CHECK_DATE";

  private Retrofit retrofit;
  private DownloadListener downloadListener;

  private Context mContext;
  private MaterialDialog upgradeProgressDialog;

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
  }

  public static void saveCheckDate(Context context) {
    String today = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
    getSharedPreferences(context).edit().putString(LAST_CHECK_DATE, today).apply();
  }

  public static boolean isTodayChecked(Context context) {
    String last = getSharedPreferences(context).getString(LAST_CHECK_DATE, "1970-01-01");
    String today = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
    return today.equals(last);
  }

  public UpgradeManager(Context context) {
    this.mContext = context;
  }

  private static OkHttpClient.Builder getUnsafeOkHttpClientBuilder() {
    try {
      // Create a trust manager that does not validate certificate chains
      final TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                           String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                           String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }
          }
      };

      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      return new OkHttpClient.Builder()
          .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
          .hostnameVerifier((hostname, session) -> true);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Retrofit getBasicRetrofit() {
    // Create a http logging interceptor
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY);
    // Create httpClient
    OkHttpClient.Builder builder = getUnsafeOkHttpClientBuilder();
    OkHttpClient httpClient = builder
        .addNetworkInterceptor(loggingInterceptor)
        .build();
    httpClient.newBuilder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    // Create retrofit
    retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build();

    return retrofit;
  }

  private Retrofit getRetrofitWithDownloadListener() {
    // Define downloadListener, this is for updating progress bar while we're downloading apk.
    downloadListener = new DownloadListener() {
      @Override
      public void onStartDownload() {
        // When we start download.
        upgradeProgressDialog = new MaterialDialog.Builder(mContext)
            .title(R.string.upgrade_downloading)
            .content(R.string.upgrade_downloading)
            .contentGravity(GravityEnum.CENTER)
            .cancelable(false)
            .progress(false, 100, false)
            .show();
      }

      @Override
      public void onProgress(int progress) {
        // When we are downloading, progress tells us how many percentage we've downloaded.
        upgradeProgressDialog.setProgress(progress);
      }

      @Override
      public void onFinishDownload() {
        // Download is finished, set the dialog message to DONE.
        upgradeProgressDialog.setContent(mContext.getString(R.string.upgrade_done));
      }

      @Override
      public void onError(String error) {
        // Encounter error while we're downloading.
        upgradeProgressDialog.setContent(mContext.getString(R.string.upgrade_error));
        ToastUtils.show(mContext, error);
      }
    };

    // Create httpClient
    OkHttpClient.Builder builder = getUnsafeOkHttpClientBuilder();
    OkHttpClient httpClient = builder
        .addInterceptor(new DownloadInterceptor(downloadListener))
        .build();
    httpClient.newBuilder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

    // Create retrofit
    retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build();

    return retrofit;
  }

  private void download() {
    Observer<InputStream> observer = new Observer<InputStream>() {
      @Override
      public void onSubscribe(Disposable d) {
        downloadListener.onStartDownload();
      }

      @Override
      public void onNext(InputStream stream) {
        // Install APK.
        File apkfile = new File(FOLDER_DIR, DOWNLOADED_APK_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          Uri apkUri = FileProvider
              .getUriForFile(mContext, BuildConfig.APPLICATION_ID +
                      UpgradeManager.PROVIDER_PACKAGE_NAME,
                  apkfile);
          Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
          intent.setData(apkUri);
          intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(intent);
        } else {
          Uri apkUri = Uri.fromFile(apkfile);
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(intent);
        }
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
        upgradeProgressDialog.dismiss();
        ToastUtils.show(mContext, e.getMessage());
      }

      @Override
      public void onComplete() {
        downloadListener.onFinishDownload();
        upgradeProgressDialog.dismiss();
      }
    };

    getRetrofitWithDownloadListener().create(UpgradeService.class)
        .downloadApk(DOWNLOAD_URL)
        .subscribeOn(Schedulers.io())
        .unsubscribeOn(Schedulers.io())
        .map(responseBody -> responseBody.byteStream())
        .observeOn(Schedulers.computation())
        // Save stream to file.
        .doOnNext(stream -> writeFile(stream))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  public void checkVersion(NetworkUtils.NetworkType networkType) {
    Observer<Integer> observer = new Observer<Integer>() {
      @Override
      public void onSubscribe(Disposable d) {

      }

      @Override
      public void onNext(Integer version) {
        if (version > BuildConfig.VERSION_CODE) {
          // New version code, show upgrade notice dialog.
          new MaterialDialog.Builder(mContext)
              .title(R.string.upgrade_title)
              .content(R.string.upgrade_message, true)
              .positiveText(R.string.upgrade_yes)
              .negativeText(R.string.upgrade_no)
              .onPositive((dialog, which) -> {
                dialog.dismiss();
                switch (networkType) {
                  case NETWORK_MOBILE:
                    // Tell user whether to use mobile network to download.
                    new MaterialDialog.Builder(mContext)
                        .title(R.string.upgrade_title)
                        .content(R.string.upgrade_under_mobile_hint, true)
                        .positiveText(R.string.upgrade_yes)
                        .negativeText(R.string.upgrade_no)
                        .onPositive((confirmDialog, confirmWhich) -> {
                          confirmDialog.dismiss();
                          // Start download.
                          download();
                        })
                        .show();
                    break;
                  case NETWORK_WIFI:
                    download();
                    break;
                  case NETWORK_NONE:
                    // Tell user no network is available.
                    new MaterialDialog.Builder(mContext)
                        .title(R.string.upgrade_title)
                        .content(R.string.upgrade_no_network, true)
                        .negativeText(R.string.upgrade_cancel)
                        .show();
                    break;
                }
              })
              .show();
        }
        // Mark as checked.
        UpgradeManager.saveCheckDate(mContext);
      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
        ToastUtils.show(mContext, e.getMessage());
      }

      @Override
      public void onComplete() {

      }
    };
    getBasicRetrofit().create(UpgradeService.class)
        .checkVersion(CHECK_VERSION_URL)
        .subscribeOn(Schedulers.io())
        .unsubscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer);
  }

  private void writeFile(InputStream stream) {
    File folder = new File(FOLDER_DIR);
    if (!folder.exists()) {
      if (!folder.mkdirs()) {
        downloadListener.onError("mkdirs failed.");
        return;
      }
    }
    File apk = new File(folder, DOWNLOADED_APK_NAME);
    if (apk.exists()) {
      apk.delete();
    }
    apk.deleteOnExit();

    try {
      FileOutputStream fos = new FileOutputStream(apk);
      byte[] b = new byte[4096];
      int len;
      while ((len = stream.read(b)) != -1) {
        fos.write(b, 0, len);
      }
      stream.close();
      fos.close();

    } catch (FileNotFoundException e) {
      downloadListener.onError("FileNotFoundException");
    } catch (IOException e) {
      downloadListener.onError("IOException");
    }
  }
}
