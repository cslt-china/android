package com.google.android.apps.signalong;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

/** IntroRecordFragmentAdapter used to render the introduction fragment page. */
class IntroRecordFragmentAdapter extends FragmentPagerAdapter {

  private final List<Fragment> fragmentList;

  public IntroRecordFragmentAdapter(FragmentManager fragmentManager, List<Fragment> fragmentList) {
    super(fragmentManager);
    this.fragmentList = fragmentList;
  }

  @Override
  public Fragment getItem(int i) {
    return fragmentList.get(i);
  }

  @Override
  public int getCount() {
    return fragmentList.size();
  }
}
