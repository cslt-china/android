package com.google.android.apps.signalong;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList;
import java.util.List;

/** VideoGridAdapter adaptes for recyclerView to display list data. */
class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.VideoHolder> {

  private List<DataBeanList.DataBean> videoList;
  private ItemListener itemListener;

  @NonNull
  @Override
  public VideoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view =
        LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.item_grid_video, viewGroup, false);
    return new VideoHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull VideoHolder videoHolder, int position) {
    videoHolder.toBindData(videoList.get(position));
    videoHolder.imageView.setOnClickListener(
        view -> {
          if (itemListener != null) {
            itemListener.onItemClick(videoList.get(position));
          }
        });
  }

  @Override
  public int getItemCount() {
    return videoList == null ? 0 : videoList.size();
  }

  public void addItems(List<DataBeanList.DataBean> list) {
    videoList = list;
    notifyDataSetChanged();
  }

  public VideoGridAdapter setItemListener(ItemListener itemListener) {
    this.itemListener = itemListener;
    return this;
  }

  /** VideoHolder stores multiple views, mainly to optimize performance. */
  public static class VideoHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final TextView textView;

    public VideoHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView.findViewById(R.id.video_img_imageview);
      textView = (TextView) itemView.findViewById(R.id.video_title_textview);
    }

    public void toBindData(DataBeanList.DataBean dataBean) {
      textView.setText(dataBean.getGlossText());
      Glide.with(itemView.getContext()).load(dataBean.getThumbnail()).into(imageView);
    }
  }

  /** ItemListener is called when the item is clicked. */
  public interface ItemListener {
    void onItemClick(DataBeanList.DataBean videoData);
  }
}
