package com.google.android.apps.cslt.ui.data_collection;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.apps.cslt.R;


import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CollectDataMenuActivity extends AppCompatActivity {

    @BindView(R.id.back_btn)
    TextView backBtn;
    @BindView(R.id.back_title_tv)
    TextView backTitleTv;
    @BindView(R.id.standard_data_btn)
    Button standardDataBtn;
    @BindView(R.id.test_data_btn)
    Button testDataBtn;
    Fragment standardDataFragment;
    Fragment testDataFragment;

    static final int REQUEST_VIDEO_CAPTURE = 1;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data_menu);
        ButterKnife.bind(this);
        initFragment();
        initData();
        switchFragment(StandardDataFragment.class);
    }
    @OnClick({R.id.back_btn, R.id.back_title_tv, R.id.standard_data_btn, R.id.test_data_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                break;
            case R.id.standard_data_btn:
                switchFragment(StandardDataFragment.class);
                break;
            case R.id.test_data_btn:
                switchFragment(TestDataFragment.class);
                break;
                default:
        }
    }

    public void initData()
    {


        backTitleTv.setText(getString(R.string.title_data_collect));
    }

    public void initFragment()
    {
        standardDataFragment=new StandardDataFragment();
        testDataFragment=new TestDataFragment();
    }

    public void switchFragment(Class fragmentClass)
    {
        Fragment targetFragment;
        if(fragmentClass==StandardDataFragment.class)
        {
            targetFragment=standardDataFragment;
            standardDataBtn.setBackgroundColor(getColor(R.color.selectFragment));
            testDataBtn.setBackgroundColor(getColor(R.color.notSelectFragment));
        }
        else
        {
            targetFragment=testDataFragment;
            testDataBtn.setBackgroundColor(getColor(R.color.selectFragment));
            standardDataBtn.setBackgroundColor(getColor(R.color.notSelectFragment));
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,targetFragment).commit();
    }

}
