package com.google.android.apps.signalong;

import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil.ItemCallback;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.apps.signalong.UnreviewedVideoGridAdapter.UnreviewedVideoHolder;
import com.google.android.apps.signalong.jsonentities.VideoListResponse.DataBeanList.DataBean;

/** UnreviewedVideoGridAdapter adaptes for recyclerView to display list data. */
public class UnreviewedVideoGridAdapter extends PagedListAdapter<DataBean, UnreviewedVideoHolder> {
  private ItemListener itemListener;

  public UnreviewedVideoGridAdapter() {
    super(
        new ItemCallback<DataBean>() {
          @Override
          public boolean areItemsTheSame(@NonNull DataBean t1, @NonNull DataBean t2) {
            return t1.getUuid().equals(t2.getUuid());
          }

          @Override
          public boolean areContentsTheSame(@NonNull DataBean t1, @NonNull DataBean t2) {
            return t1 == t2;
          }
        });
  }

  public void setItemListener(ItemListener itemListener) {
    this.itemListener = itemListener;
  }

  @NonNull
  @Override
  public UnreviewedVideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_video, parent, false);
    return new UnreviewedVideoHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull UnreviewedVideoHolder holder, int position) {
    DataBean dataBean = getItem(position);
    if (dataBean == null) {
      return;
    }
    holder.toBindData(dataBean);
    holder.imageView.setOnClickListener(
        view -> {
          if (itemListener != null) {
            itemListener.onItemClick(position, getCurrentList());
          }
        });
  }

  /** UnreviewedVideoHolder stores multiple views, mainly to optimize performance. */
  public static class UnreviewedVideoHolder extends RecyclerView.ViewHolder {
    private final ImageView imageView;
    private final TextView textView;

    public UnreviewedVideoHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView.findViewById(R.id.video_img_imageview);
      textView = (TextView) itemView.findViewById(R.id.video_title_textview);
    }

    public void toBindData(DataBean dataBean) {
      Glide.with(itemView.getContext()).load(dataBean.getThumbnail()).into(imageView);
      textView.setText(dataBean.getGlossText());
    }
  }
  /** This ItemListener is called when the item is clicked. */
  public interface ItemListener {
    void onItemClick(int position, PagedList<DataBean> pagedList);
  }
}
