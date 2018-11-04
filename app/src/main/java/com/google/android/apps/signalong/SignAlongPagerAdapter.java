package com.google.android.apps.signalong;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SignAlongPagerAdapter extends FragmentPagerAdapter {

  private final List<Pair<String, Fragment>> fragments;

  public SignAlongPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
    this.fragments = new ArrayList<>();
  }

  public void addFragment(String pageTitle, Fragment fragment) {
    this.fragments.add(new Pair(pageTitle, fragment));
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
