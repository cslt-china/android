package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
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
  ImageView pdfView; //合同显示

  @BindView(R.id.btn_reject_agreement)
  Button btnReject; //退出

  @BindView(R.id.btn_confirm_agreement)
  Button btnConfirm; //下一步

  @BindView(R.id.cb_agree)
  CheckBox cbAgree; //已确认合同是本人签署

  private String username;//用户登录账号（即用户名）


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

  @Override //合同下载成功
  public void onImageDownloaded(Bitmap bitmap) {
    pdfView.setImageBitmap(bitmap);
  }

  @Override //合同下载失败
  public void onDownloadFailure(String error) {
    ToastUtils.show(getApplicationContext(), error);
  }

  @Override
  public void onBackPressed() {
    LoginSharedPreferences.clearUserData(getApplicationContext());
    this.sendBroadcast(new Intent(EXIT_ACTION));//发送退出应用广播
  }

  /**
   * 是否已确认合同
   */
  private void toggleConfirmEnabled(boolean b) {
    btnConfirm.setEnabled(b);
    btnConfirm.setBackgroundColor(getColor(b ? R.color.green : R.color.disabled_green));

  }
}
