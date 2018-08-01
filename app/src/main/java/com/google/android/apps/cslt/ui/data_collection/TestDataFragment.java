package com.google.android.apps.cslt.ui.data_collection;


import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.android.apps.cslt.R;
import com.google.android.apps.cslt.utils.DialogUtils;
import com.google.android.apps.cslt.utils.FileUtils;
import com.google.android.apps.cslt.utils.ToastUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class TestDataFragment extends Fragment implements TestDataContract.View {


    public TestDataContract.Presenter mPresenter;

    @BindView(R.id.show_video_vv)
    VideoView showVideoVv;
    @BindView(R.id.add_video_btn)
    Button addVideoBtn;
    @BindView(R.id.data_name_et)
    EditText dataNameEt;
    @BindView(R.id.upload_btn)
    Button uploadBtn;
    @BindView(R.id.upload_progress_bar)
    ProgressBar uploadProgressBar;

    String videoUrl;

    Unbinder unbinder;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void updateShowLoading(int current) {
        uploadProgressBar.setProgress(current);
    }

    @Override
    public void showLoading() {
        uploadProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        uploadProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new TestDataPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_test_data, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void showError(int msg) {
        ToastUtils.show(getContext(), getString(msg));
    }

    @Override
    public void showUploadSuccess() {
        DialogUtils.showAlertDialog(getContext(), getResources().getString(R.string.dialog_upload_success));
    }


    @Override
    public String getVideoUrl() {
        return videoUrl;
    }

    @Override
    public String getDataName() {
        return dataNameEt.getText().toString();
    }

    @Override
    public void clearFormData() {
        FileUtils.removeFile(videoUrl);
        dataNameEt.setText("");
        videoUrl = "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }



    @OnClick({R.id.show_video_vv, R.id.add_video_btn, R.id.data_name_et, R.id.upload_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_video_btn:
                startActivity(CameraActivity.class);
                break;
            case R.id.upload_btn:
                mPresenter.uploadFile();
                break;
            default:
        }
    }

    public void startActivity(Class target)
    {
        if(target!=null)
        {
            Intent intent=new Intent(getContext(),target);
            startActivity(intent);
        }
    }


}
