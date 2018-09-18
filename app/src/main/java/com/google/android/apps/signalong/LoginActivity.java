package com.google.android.apps.signalong;

import android.arch.lifecycle.ViewModelProviders;
import android.text.TextUtils;
import android.widget.TextView;
import com.google.android.apps.signalong.jsonentities.User;
import com.google.android.apps.signalong.utils.ToastUtils;

/** LoginActivity implements the login UI. */
public class LoginActivity extends BaseActivity {

  public static final Integer LOGIN_FAIL = 1;
  public static final Integer LOGIN_SUCCESS = 2;
  private LoginViewModel loginViewModel;
  @Override
  public int getContentView() {
    return R.layout.activity_login;
  }

  @Override
  public void init() {
    loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
  }

  @Override
  public void initViews() {
    findViewById(R.id.login_button)
        .setOnClickListener(
            view -> {
              String username =
                  ((TextView) findViewById(R.id.username_edit_text)).getText().toString();
              String password =
                  ((TextView) findViewById(R.id.password_edit_text)).getText().toString();
              if (TextUtils.isEmpty(username)) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_username_empty));
                return;
              }
              if (TextUtils.isEmpty(password)) {
                ToastUtils.show(getApplicationContext(), getString(R.string.tip_password_empty));
                return;
              }
              loginViewModel
                  .login(new User(username.trim(), password.trim()))
                  .observe(
                      this,
                      authResponseResponse -> {
                        if (authResponseResponse == null) {
                          ToastUtils.show(
                              getApplicationContext(), getString(R.string.tip_login_fail));
                          return;
                        }
                        ToastUtils.show(
                            getApplicationContext(), getString(R.string.tip_login_success));
                        this.setResult(LOGIN_SUCCESS);
                        finish();
                      });
            });
  }

  @Override
  public void onBackPressed() {
    this.setResult(LOGIN_FAIL);
    super.onBackPressed();
  }
}
