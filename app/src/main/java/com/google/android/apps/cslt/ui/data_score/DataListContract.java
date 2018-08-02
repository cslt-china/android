package com.google.android.apps.cslt.ui.data_score;

import com.google.android.apps.cslt.bean.ScoreDataItem;

import java.util.List;

public interface DataListContract {
    interface View {
        public void showLoading();
        public void hideLoading();
        public void updateData(List<ScoreDataItem> scoreDataItemList);
    }

    interface Presenter {
        public void LoadData(Integer page);
    }

}
