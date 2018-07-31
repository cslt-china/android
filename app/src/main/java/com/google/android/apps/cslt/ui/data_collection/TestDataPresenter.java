package com.google.android.apps.cslt.ui.data_collection;

import android.text.TextUtils;

import com.google.android.apps.cslt.R;
import com.google.android.apps.cslt.utils.FileUtils;


public class TestDataPresenter implements TestDataContract.Presenter {

    public TestDataContract.View mView;

    public TestDataPresenter(TestDataContract.View mview) {
        this.mView = mview;
    }

    @Override
    public void uploadFile() {

        if(TextUtils.isEmpty(mView.getVideoUrl()))
        {
            mView.showError(R.string.toast_url_not_empty);
            return;
        }
        if(!FileUtils.existFile(mView.getVideoUrl()))
        {
            mView.showError(R.string.toast_file_not_exist);
            return;
        }
        if(TextUtils.isEmpty(mView.getDataName()))
        {
            mView.showError(R.string.toast_name_not_empty);
            return;
        }
        //temporarily add the upload data code
        mView.showLoading();
        mView.updateShowLoading(10);
        mView.hideLoading();
        mView.clearFormData();
        mView.showUploadSuccess();
    }
}
