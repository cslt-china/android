package com.google.android.apps.signalong;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebActivity extends BaseActivity {
    private WebView mWebView;

    @Override
    public int getContentView() {
        return R.layout.activity_web;
    }

    @Override
    public void init() {
        mWebView = findViewById(R.id.activity_web_webview);
        initWebView();
    }

    @Override
    public void initViews() {
        int from = getIntent().getIntExtra("from",-1);
        if (from == 1){
            mWebView.loadUrl("http://114.115.205.129:8081/help/help.html");
        } else {
            mWebView.loadUrl("http://114.115.205.129:8081/register/register.html");
        }
    }



    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setLoadsImagesAutomatically(true);//支持自动加载图片
        webSettings.setDomStorageEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new BaseWebViewClient());
        mWebView.canGoBack();
        mWebView.canGoForward();
    }

    class BaseWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.i("xiaoyu", "shouldOverrideUrlLoading ==> request: "+request.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(String.valueOf(request.getUrl()));
            } else {
                view.loadUrl(request.toString());
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i("xiaoyu","onPageStarted  ==>  url: "+url+"    favicon: "+favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i("xiaoyu","onPageFinished  ==>  url: "+url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Log.i("xiaoyu","onReceivedError    code = "+error+"    request: "+request.toString());
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("xiaoyu","------------onDestroy----------------");
//        mWebView.clearCache(true);
//        mWebView.clearHistory();
//        mWebView.clearFormData();
//        WebStorage.getInstance().deleteAllData(); //清空WebView的localStorage
        mWebView.destroy();
    }
}

