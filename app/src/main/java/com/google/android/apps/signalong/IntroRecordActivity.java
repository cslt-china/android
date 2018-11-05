package com.google.android.apps.signalong;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import com.google.android.apps.signalong.IntroFragment.FragmentListener;
import com.google.android.apps.signalong.IntroFragment.IntroFragmentType;
import java.util.ArrayList;
import java.util.List;

/**
 * IntroRecordActivity shows record introduction to teach user how to record video.
 */
public class IntroRecordActivity extends BaseActivity {

  @Override
  public int getContentView() {
    return R.layout.activity_intro_record;
  }

  @Override
  public void init() {}

  @Override
  public void initViews() {
    FragmentListener fragmentListener =
        new FragmentListener() {
          @Override
          public void onBack() {
            finish();
          }

          @Override
          public void onOk() {
            startActivity(new Intent(getApplicationContext(), CameraActivity.class));
            finish();
          }
        };
    List<Fragment> fragmentList = new ArrayList<>();
    fragmentList.add(
        IntroFragment.newInstance(IntroFragmentType.RECORDING_LIST, fragmentListener));
    fragmentList.add(
        IntroFragment.newInstance(IntroFragmentType.ALIGN_BODY, fragmentListener));
    ((ViewPager) findViewById(R.id.intro_view_pager))
        .setAdapter(new IntroRecordFragmentAdapter(getSupportFragmentManager(), fragmentList));
  }
}
