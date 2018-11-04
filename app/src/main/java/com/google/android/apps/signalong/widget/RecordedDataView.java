package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.jsonentities.VideoListResponse;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;
import com.google.android.apps.signalong.utils.TimerUtils;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class RecordedDataView extends TaskView<DataBean> {
  private static final String TAG = "RecordedDataView";

  private static final Map<String, Integer> GLOSS_STATUS_LABEL_ID_MAP = new HashMap<>();
  static {
    GLOSS_STATUS_LABEL_ID_MAP.put("APPROVED", R.string.label_gloss_status_approved);
    GLOSS_STATUS_LABEL_ID_MAP.put("PENDING_APPROVAL", R.string.label_gloss_status_pending_approval);
    GLOSS_STATUS_LABEL_ID_MAP.put("REJECTED", R.string.label_gloss_status_rejected);
  }

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

  public void setData(DataBean data, TaskType taskType) {
    Log.i(TAG, "setData with task type " + taskType);
    this.data = data;
    this.taskType = taskType;
    ((TextView) layout.findViewById(R.id.gloss_text_textview)).setText(data.getGlossText());
    ((TextView) layout.findViewById(R.id.gloss_creation_time_textview))
        .setText(TimerUtils.convertTimestamp(data.getCreatedTime()));
    ((TextView) findViewById(R.id.gloss_status_textview))
        .setText(layout.getResources().getText(
            GLOSS_STATUS_LABEL_ID_MAP.get(data.getStatus())));
    ((TextView) layout.findViewById(R.id.gloss_approved_counter_textview))
        .setText(String.valueOf(data.getApprovedReviewCounter()));
    ((TextView) layout.findViewById(R.id.gloss_rejected_counter_textview))
        .setText(String.valueOf(data.getRejectedReviewCounter()));

    updateView();
  }

  private void updateView() {
    CardView card = layout.findViewById(R.id.recorded_data_cardview);
    switch (taskType) {
      case NEW_REVIEW:
        break;
      case MY_RECORDING:
        switch (data.getStatus()) {
          case "APPROVED":
            card.setCardBackgroundColor(getResources().getColor(R.color.green, null));
            break;
          case "PENDING_APPROVAL":
            card.setCardBackgroundColor(getResources().getColor(R.color.yellow, null));
            break;
          case "REJECTED":
            card.setCardBackgroundColor(getResources().getColor(R.color.red, null));
            break;
        }
        break;
      default:
        Log.e(TAG, "updateView cannot update task type " + this.taskType);
        throw new InvalidParameterException(
            "Invalid AttributeSet of TaskTExtView_taskType=" + this.taskType);
    }
    this.setVisibility(View.VISIBLE);
  }

}
