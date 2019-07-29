package com.google.android.apps.signalong;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.android.apps.signalong.api.ApiHelper;
import com.google.android.apps.signalong.api.VideoApi;
import com.google.android.apps.signalong.service.DownloadImageTask;
import com.google.android.apps.signalong.utils.LoginSharedPreferences;
import com.google.android.apps.signalong.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AgreementActivity extends BaseActivity implements DownloadImageTask.DownloadImageCallbacks {

  @BindView(R.id.agreementPdf)
  ImageView pdfView;

  @BindView(R.id.btn_reject_agreement)
  Button btnReject;

  @BindView(R.id.btn_confirm_agreement)
  Button btnConfirm;

  @BindView(R.id.cb_agree)
  CheckBox cbAgree;

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

    String pdfUrl = String.format("%s/%s-agreement.png", ApiHelper.AGREEMENTS_BASE_URL, username);

    new DownloadImageTask(ApiHelper.getRetrofit().create(VideoApi.class), this).execute(pdfUrl);

    toggleConfirmEnabled(false);

    cbAgree.setOnCheckedChangeListener((compoundButton, b) -> toggleConfirmEnabled(b));

    btnReject.setOnClickListener(view -> onBackPressed());

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

  @Override
  public void onImageDownloaded(Bitmap bitmap) {
    pdfView.setImageBitmap(bitmap);
  }

  @Override
  public void onDownloadFailure(String error) {
    ToastUtils.show(getApplicationContext(), error);
  }

  @Override
  public void onBackPressed() {
    LoginSharedPreferences.clearUserData(getApplicationContext());
    this.sendBroadcast(new Intent(EXIT_ACTION));
  }

  private void toggleConfirmEnabled(boolean b) {
    btnConfirm.setEnabled(b);
    btnConfirm.setBackgroundColor(getColor(b ? R.color.green : R.color.disabled_green));

  }
}
