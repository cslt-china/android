package com.google.android.apps.cslt.ui.data_collection;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.apps.cslt.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_data_menu);
        ButterKnife.bind(this);
//        getSupportFragmentManager().
//                beginTransaction().
//                add(R.id.fragment_container,StandardDataFragment.newInstance()).
//                commit();
    }


    @OnClick({R.id.back_btn, R.id.back_title_tv, R.id.standard_data_btn, R.id.test_data_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                break;
            case R.id.standard_data_btn:
                break;
            case R.id.test_data_btn:
                break;
                default:
        }
    }
    public void switchFragment()
    {

    }
}
