package com.google.android.apps.signalong;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TopicFragment extends BaseFragment {
  private TextView indexView;
  private TextView topicTextView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_sign_topic, container,
                                 false);
    indexView = view.findViewById(R.id.topic_counter_text_view);
    topicTextView = view.findViewById(R.id.topic_large_word_text_view);
    return view;
  }

  public void setTopicText(int index, String text) {
    topicTextView.setText(text);
    indexView.setText(String.format(getString(R.string.label_counter), index+1));
  }
}
