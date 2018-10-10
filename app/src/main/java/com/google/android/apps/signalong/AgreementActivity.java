package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.UserApi;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.google.android.apps.signalong.api.ApiHelper.STATIC_URL;

public class AgreementActivity extends BaseActivity {

  @BindView(R.id.agreementPdf)
  ImageView pdfView;

  @BindView(R.id.btn_reject_agreement)
  Button btnReject;

  @BindView(R.id.btn_confirm_agreement)
  Button btnConfirm;

  private String username;


  private LoginViewModel loginViewModel;

  @Override
  public int getContentView() {
    return R.layout.activity_agreement;
  }

  @Override
  public void init() {
    username = LoginSharedPreferences.getCurrentUsername(getApplicationContext());
    loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
  }

  @Override
  public void initViews() {
    ButterKnife.bind(this);
    pdfView.setScaleType(ImageView.ScaleType.FIT_XY);

    String pdfUrl = String.format("http://cslt-211408.appspot.com.storage.googleapis.com/static/agreements/%s-agreement.png", username);
    Glide.with(this).load(pdfUrl).into(pdfView);

    btnReject.setOnClickListener(view -> {
      LoginSharedPreferences.clearUserData(getApplicationContext());
      this.sendBroadcast(new Intent(EXIT_ACTION));
    });

    btnConfirm.setOnClickListener(view -> {
      LoginSharedPreferences.saveConfirmedAgreement(getApplicationContext(), username);
      loginViewModel.confirmAgreement().observe(this, result -> {
        if (result) {
          finish();
        } else {
          ToastUtils.show(getApplicationContext(), getString(R.string.tip_connect_fail));
        }
      });
    });
  }
}
