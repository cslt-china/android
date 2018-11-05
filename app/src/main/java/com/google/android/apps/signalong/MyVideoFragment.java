package com.google.android.apps.signalong;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.VideoStatus;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MyVideoFragment extends BaseFragment {
  private static final String TAG = "MyVideoFragment";

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewContainer = inflater.inflate(R.layout.fragment_my_video, container, false);
    Resources res = getResources();
    MyVideoPagerAdapter adapter = new MyVideoPagerAdapter(getChildFragmentManager());
    adapter.addFragment(
        getString(R.string.label_approved_video_title),
        ResourcesCompat.getDrawable(res, R.drawable.video_status_approved_filled_24px, null),
        MyVideoPerStatusFragment.newInstance(VideoStatus.APPROVED));
    adapter.addFragment(
        getString(R.string.label_pending_video_title),
        ResourcesCompat.getDrawable(res, R.drawable.video_status_pending_filled_24px, null),
        MyVideoPerStatusFragment.newInstance(VideoStatus.PENDING_APPROVAL));
    adapter.addFragment(
        getString(R.string.label_rejected_video_title),
        ResourcesCompat.getDrawable(res, R.drawable.video_status_rejected_filled_24px, null),
        MyVideoPerStatusFragment.newInstance(VideoStatus.REJECTED));

    ViewPager viewPager = viewContainer.findViewById(R.id.my_video_viewpager);
    viewPager.setAdapter(adapter);

    ((TabLayout) viewContainer.findViewById(R.id.my_video_tabs)).setupWithViewPager(viewPager);

    return viewContainer;
  }

}