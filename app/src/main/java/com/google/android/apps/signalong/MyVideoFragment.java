package com.google.android.apps.signalong;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.VideoStatus;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.apps.signalong.utils.ResourceUtils;

public class MyVideoFragment extends BaseFragment {
  private static final String TAG = "MyVideoFragment";

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View viewContainer = inflater.inflate(R.layout.fragment_my_video, container, false);
    Resources res = getResources();
    MyVideoPagerAdapter adapter = new MyVideoPagerAdapter(getChildFragmentManager());
    Resources resource = getResources();
    adapter.addFragment(
        ResourceUtils.getVideoStatusPageTitle(resource, VideoStatus.PENDING_APPROVAL),
        MyVideoPerStatusFragment.newInstance(VideoStatus.PENDING_APPROVAL));
    adapter.addFragment(
        ResourceUtils.getVideoStatusPageTitle(resource, VideoStatus.REJECTED),
        MyVideoPerStatusFragment.newInstance(VideoStatus.REJECTED));
    adapter.addFragment(
        ResourceUtils.getVideoStatusPageTitle(resource, VideoStatus.APPROVED),
        MyVideoPerStatusFragment.newInstance(VideoStatus.APPROVED));

    ViewPager viewPager = viewContainer.findViewById(R.id.my_video_viewpager);
    viewPager.setAdapter(adapter);

    ((TabLayout) viewContainer.findViewById(R.id.my_video_tabs)).setupWithViewPager(viewPager);

    return viewContainer;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

}