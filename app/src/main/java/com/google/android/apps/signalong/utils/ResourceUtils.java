package com.google.android.apps.signalong.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import com.google.android.apps.signalong.R;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.VideoStatus;
import java.util.HashMap;
import java.util.Map;

public class ResourceUtils {

  private static final String TAG = "ResourceUtils";

  private static final Map<VideoStatus, Integer> VIDEO_STATUS_LABEL_ID_MAP = new HashMap<>();

  static {
    VIDEO_STATUS_LABEL_ID_MAP.put(VideoStatus.APPROVED, R.string.label_video_status_approved);
    VIDEO_STATUS_LABEL_ID_MAP
        .put(VideoStatus.PENDING_APPROVAL, R.string.label_video_status_pending);
    VIDEO_STATUS_LABEL_ID_MAP.put(VideoStatus.REJECTED, R.string.label_video_status_rejected);
  }

  private static final Map<VideoStatus, Integer> VIDEO_STATUS_ICON_ID_MAP = new HashMap<>();

  static {
    VIDEO_STATUS_ICON_ID_MAP.put(VideoStatus.APPROVED,
        R.drawable.video_status_approved_filled_24px);
    VIDEO_STATUS_ICON_ID_MAP.put(VideoStatus.PENDING_APPROVAL,
        R.drawable.video_status_pending_filled_24px);
    VIDEO_STATUS_ICON_ID_MAP.put(VideoStatus.REJECTED,
        R.drawable.video_status_rejected_filled_24px);
  }

  public static Drawable getVideoStatusIcon(Resources resource, VideoStatus status) {
    return ResourcesCompat.getDrawable(resource, VIDEO_STATUS_ICON_ID_MAP.get(status), null);
  }

  public static CharSequence getVideoStatusPageTitle(Resources resource, VideoStatus status) {
    return buildLabeledImageSpan(
        resource.getString(VIDEO_STATUS_LABEL_ID_MAP.get(status)),
        getVideoStatusIcon(resource, status));
  }

  private static CharSequence buildLabeledImageSpan(@Nullable String label, Drawable icon) {
    // TODO(jxue): Find a non-hacky way to avoid overlap between the icon image and the string.
    // The current solution is to add space before text for convenience
    String spacedLabel = (label == null || label.isEmpty()) ? "" : "    " + label;
    SpannableStringBuilder stringBuilder = new SpannableStringBuilder(spacedLabel);
    try {
      icon.setBounds(
          5, 5, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
      ImageSpan span = new ImageSpan(icon, DynamicDrawableSpan.ALIGN_BASELINE);
      stringBuilder.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    } catch (Exception e) {
      Log.e(TAG, "Cannot create spannable string for page title!!!");
    }
    return stringBuilder;
  }
}
