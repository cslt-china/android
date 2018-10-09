package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.apps.signalong.jsonentities.ChangePasswords;
import com.google.android.apps.signalong.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePasswordActivity extends BaseActivity {

  @BindView(R.id.et_old_password)
  EditText etOld;

  @BindView(R.id.et_new_password)
  EditText etNew;

  @BindView(R.id.et_confirm_password)
  EditText etConfirm;

  @Override
  public int getContentView() {
    return R.layout.activity_change_password;
  }

  private ChangePasswordViewModel model;

  @Override
  public void init() {
    model = ViewModelProviders.of(this).get(ChangePasswordViewModel.class);
  }

  @Override
  public void initViews() {
    Toolbar toolbar = findViewById(R.id.change_password_toolbar);
    setSupportActionBar(toolbar);
    final Drawable upArrow = getResources().getDrawable(R.drawable.back);

    getSupportActionBar().setHomeAsUpIndicator(upArrow);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    toolbar.setNavigationOnClickListener(v -> finish());
    ButterKnife.bind(this);
  }

  @OnClick(R.id.btn_update_password)
  public void update() {
    if (TextUtils.isEmpty(etOld.getText()) ||
      TextUtils.isEmpty(etNew.getText()) ||
      TextUtils.isEmpty(etConfirm.getText())) {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_password_empty));
      return;
    }
    String oldPwd = etOld.getText().toString(),
      newPwd = etNew.getText().toString(),
      confirmPwd = etConfirm.getText().toString();
    // 两次新密码要一样
    if (!newPwd.equals(confirmPwd)) {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_passwords_not_equal));
      return;
    }
    // 旧密码和新密码不能一样
    if (oldPwd.equals(newPwd)) {
      ToastUtils.show(getApplicationContext(), getString(R.string.tip_new_password_is_equal_to_old));
      return;
    }
    // 新密码大于六位
    if (newPwd.length() < 6) {
      ToastUtils.show(getApplicationContext(), getString(R.string.password_is_less_6_chars));
      return;
    }

    model.changePassword(new ChangePasswords(oldPwd, newPwd))
      .observe(this, result -> {
        String msg;
        if (!result && model.getErrorMessage() != null) {
          msg = model.getErrorMessage();
        } else {
          msg = getString(result
            ? R.string.tip_change_password_successful
            : R.string.tip_change_password_failed);
        }
        ToastUtils.show(getApplicationContext(), msg);
      });
  }
}
