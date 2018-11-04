package com.google.android.apps.signalong.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.jsonentities.SignPromptBatchResponse.DataBean;
import java.security.InvalidParameterException;

public class PromptDataView extends TaskView<DataBean> {
  private static final String TAG = "PromptDataView";

  public PromptDataView(Context context, AttributeSet attrs) {
    super(context, attrs, R.layout.item_prompt_data_view);
  }

  public DataBean getData() {
    if (taskType == TaskType.NEW_RECORDING) {
      return data;
    } else {
      return null;
    }
  }

  public void setData(DataBean data, TaskType taskType) {
    if (taskType != TaskType.NEW_RECORDING) {
      throw new InvalidParameterException(
          "task type must be NEW_RECORDING for SignPromptBatchResponse.DataBean!!");
    }
    this.taskType = TaskType.NEW_RECORDING;
    this.data = data;
    ((TextView) layout.findViewById(R.id.gloss_text_textview)).setText(data.getText());
  }

}
