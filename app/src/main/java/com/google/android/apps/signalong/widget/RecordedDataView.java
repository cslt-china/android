package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.VideoStatus;
import com.google.android.apps.signalong.utils.ResourceUtils;
import com.google.android.apps.signalong.utils.TimerUtils;

public class RecordedDataView extends TaskView<DataBean> {
  private static final String TAG = "RecordedDataView";

  public RecordedDataView(Context context, AttributeSet attrs) {
    super(context, attrs, R.layout.item_recording_data_view);
  }

  public VideoListResponse.DataBeanList.DataBean getData() {
    if (taskType == TaskType.NEW_REVIEW || taskType == TaskType.MY_RECORDING) {
      return data;
    } else {
      return null;
    }
  }

  public void setData(DataBean data, TaskType taskType, int position) {
    Log.i(TAG, "setData with task type " + taskType);
    this.data = data;
    this.taskType = taskType;
    this.position = position;
    ((TextView) layout.findViewById(R.id.gloss_text_textview)).setText(data.getGlossText());
    ((TextView) layout.findViewById(R.id.gloss_creation_time_textview))
        .setText(TimerUtils.convertTimestamp(data.getCreatedTime()));
    ((ImageButton) layout.findViewById(R.id.gloss_status_button))
        .setImageDrawable(
            ResourceUtils.getVideoStatusIcon(
                getResources(), VideoStatus.valueOf(data.getStatus())));
    ((TextView) layout.findViewById(R.id.gloss_approved_counter_textview))
        .setText(String.valueOf(data.getApprovedReviewCounter()));
    ((TextView) layout.findViewById(R.id.gloss_rejected_counter_textview))
        .setText(String.valueOf(data.getRejectedReviewCounter()));

    this.setVisibility(View.VISIBLE);
  }

}
