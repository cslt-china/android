package com.google.android.apps.signalong;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

  public void setVisibility(int visibility) {
    getView().setVisibility(visibility);
  }
}
