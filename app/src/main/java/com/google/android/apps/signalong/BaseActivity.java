package com.google.android.apps.signalong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/** The parent activity for activities. */
public abstract class BaseActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getContentView());
    IntentFilter filter = new IntentFilter();
    filter.addAction(EXIT_ACTION);
    registerReceiver(exitReceiver, filter);
    init();
    initViews();
  }

  /**
   * Provides content view resource ID.
   *
   * @return An integer of the layout resource ID.
   */
  public abstract int getContentView();

  /** The initialization after a SignAlong activity is started. */
  public abstract void init();
  /** The initialization after a SignAlong view is started. */
  public abstract void initViews();

  protected static final String EXIT_ACTION = "action.exit";

  private ExitReceiver exitReceiver = new ExitReceiver();

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(exitReceiver);
  }

  class ExitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      BaseActivity.this.finish();
    }

  }
}
