package com.google.android.apps.cslt.ui.data_collection;

public interface TestDataContract {
    interface View {
        public void showError(int msg);
        public void showUploadSuccess();
        public String getVideoUrl();
        public String getDataName();
        public void clearFormData();
        public void showLoading();
        public void hideLoading();
        public void updateShowLoading(int current);
    }

    interface Presenter {
        public void uploadFile();
    }
}
