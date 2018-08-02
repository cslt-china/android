package com.google.android.apps.cslt.ui.data_score;



import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.apps.cslt.R;
import com.google.android.apps.cslt.bean.ScoreDataItem;

import java.util.Collection;
import java.util.List;

public class ScoreListAdapter extends BaseQuickAdapter<ScoreDataItem,BaseViewHolder>{

    public ScoreListAdapter(int layoutResId, @Nullable List<ScoreDataItem> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ScoreDataItem item) {
        helper.setText(R.id.id_tv,String.valueOf(item.getId()));
        helper.setText(R.id.title_tv,item.getTitle());
    }
}
