package com.google.android.apps.signalong;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.Pair;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MyVideoPagerAdapter extends FragmentPagerAdapter {
  private final static String TAG = "MyVideopagerAdapter";

  private final List<Pair<CharSequence, MyVideoPerStatusFragment>> fragments;

  public MyVideoPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
    this.fragments = new ArrayList<>();
  }

  public void addFragment(String title, Drawable titleIcon, MyVideoPerStatusFragment fragment) {
    this.fragments.add(new Pair<>(buildTitle(title, titleIcon), fragment));
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

  private CharSequence buildTitle(String title, Drawable titleIcon) {
    // TODO(jxue): Find a non-hacky way to avoid overlap between the icon image and the string.
    // The current solution is to add space before text for convenience
    SpannableStringBuilder sb = new SpannableStringBuilder("    " + title);
    try {
       titleIcon.setBounds(
           5, 5, titleIcon.getIntrinsicWidth(), titleIcon.getIntrinsicHeight());
       ImageSpan span = new ImageSpan(titleIcon, DynamicDrawableSpan.ALIGN_BASELINE);
       sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    } catch (Exception e) {
      Log.e(TAG, "Cannot create spannable string for page title!!!");
    }
    return sb;
  }
}
