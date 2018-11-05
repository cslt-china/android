package com.google.android.apps.signalong;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import java.util.ArrayList;
import java.util.List;

public class MyVideoPagerAdapter extends FragmentPagerAdapter {
  private final static String TAG = "MyVideopagerAdapter";

  private final List<Pair<CharSequence, MyVideoPerStatusFragment>> fragments;

  public MyVideoPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
    this.fragments = new ArrayList<>();
  }

  public void addFragment(CharSequence title, MyVideoPerStatusFragment fragment) {
    this.fragments.add(new Pair<>(title, fragment));
  }
  @Override
  public Fragment getItem(int position) {
    return fragments.get(position).second;
  }

  @Override
  public int getCount() {
    return fragments.size();
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return fragments.get(position).first;
  }

}
