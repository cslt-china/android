package com.google.android.apps.signalong;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/** IntroFragment shows three introductions. */
public class IntroFragment extends android.support.v4.app.Fragment {
  private static final String ARG_PARAM = "param";

  public enum IntroFragmentType {
    RECORDING_LIST,
    PAUSE_RECORDING,
    ALIGN_BODY,
    REVIEW
  }

  private IntroFragmentType param;
  private FragmentListener listener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      param = IntroFragmentType.values()[(int)getArguments().get(ARG_PARAM)];
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View viewContainer = inflater.inflate(R.layout.fragment_intro, container, false);
    viewContainer.findViewById(R.id.ok_button).setOnClickListener(view -> listener.onOk());
    viewContainer.findViewById(R.id.back_button).setOnClickListener(view -> listener.onBack());
    TextView contentTextView = (TextView) viewContainer.findViewById(R.id.content_text_view);
    TextView titleTextView = (TextView) viewContainer.findViewById(R.id.title_text_view);
    ImageView centerImageView = (ImageView) viewContainer.findViewById(R.id.center_image_view);
    switch (param) {
      case RECORDING_LIST:
        ((ImageView) viewContainer.findViewById(R.id.left_circle_image_view))
            .setImageResource(R.drawable.select_circle);
        contentTextView.setText(getString(R.string.label_content_intro_check_list));
        titleTextView.setText(getString(R.string.label_title_intro_check_list));
        centerImageView.setImageResource(R.drawable.illustration_checklist);
        viewContainer.findViewById(R.id.ok_button).setVisibility(View.GONE);
        break;
      case ALIGN_BODY:
        ((ImageView) viewContainer.findViewById(R.id.right_circle_image_view))
            .setImageResource(R.drawable.select_circle);
        contentTextView.setText(getString(R.string.label_content_intro_align_body));
        titleTextView.setText(getString(R.string.label_title_intro_align_body));
        centerImageView.setImageResource(R.drawable.illustration_align_body);
        break;
      case REVIEW:
        viewContainer.findViewById(R.id.circles_layout).setVisibility(View.GONE);
        contentTextView.setText(getString(R.string.label_content_intro_review));
        titleTextView.setText(getString(R.string.label_title_intro_review));
        centerImageView.setImageResource(R.drawable.illustration_review);
        break;
      default:
        throw new AssertionError("Did not match this parameter: " + param);
    }
    return viewContainer;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    listener = null;
  }

  public static IntroFragment newInstance(IntroFragmentType param, FragmentListener listener) {
    IntroFragment fragment = new IntroFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_PARAM, param.ordinal());
    fragment.setArguments(args);
    fragment.setListener(listener);
    return fragment;
  }

  private void setListener(FragmentListener listener) {
    this.listener = listener;
  }

  /** FragmentListener is called when click ok or back buttons. */
  public interface FragmentListener {
    void onBack();

    void onOk();
  }
}
