package com.google.android.apps.signalong;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/** The parent activity for activities. */
public abstract class BaseActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getContentView());
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
}
