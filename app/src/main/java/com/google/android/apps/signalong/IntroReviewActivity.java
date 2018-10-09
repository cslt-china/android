package com.google.android.apps.signalong;

import android.content.Intent;
import com.google.android.apps.signalong.IntroFragment.FragmentListener;
import com.google.android.apps.signalong.IntroFragment.IntroFragmentType;

/**
 * IntroReviewActivity shows review introduction to teach user how to review video.
 */
public class IntroReviewActivity extends BaseActivity {

  @Override
  public int getContentView() {
    return R.layout.activity_intro_review;
  }

  @Override
  public void init() {}

  @Override
  public void initViews() {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(
            R.id.container_layout,
            IntroFragment.newInstance(
                IntroFragmentType.REVIEW,
                new FragmentListener() {
                  @Override
                  public void onBack() {
                    finish();
                  }

                  @Override
                  public void onOk() {
                    startActivity(new Intent(getApplicationContext(), VideoReviewActivity.class));
                    finish();
                  }
                })).commit();
  }
}
