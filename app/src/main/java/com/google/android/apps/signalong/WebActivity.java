package com.google.android.apps.signalong;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


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
        mWebView.loadUrl("http://114.115.205.129:8081/register/register.html");
    }



    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        //支持缩放，默认为true。
        webSettings.setSupportZoom(false);
        //调整图片至适合webview的大小
        webSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小
        webSettings.setLoadWithOverviewMode(true);
        //设置默认编码
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setJavaScriptEnabled(true);
        //设置自动加载图片
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.i("TAG", "shouldOverrideUrlLoading ==> request: "+request.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(String.valueOf(request.getUrl()));
                } else {
                    view.loadUrl(request.toString());
                }
                return true;
            }
        });
        mWebView.setWebViewClient(new BaseWebViewClient());
    }

    class BaseWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i("TAG","onPageFinished  ==>  url: "+url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.i("TAG","onReceivedError    code = "+errorCode+"    descri: "+description);
        }
    }
}

