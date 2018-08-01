package com.google.android.apps.cslt.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.apps.cslt.R;
import com.google.android.apps.cslt.ui.data_collection.CollectDataMenuActivity;
import com.google.android.apps.cslt.ui.data_score.DataListActivity;
import com.google.android.apps.cslt.ui.user.LoginActivity;
import com.google.android.apps.cslt.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainContract.View {


    @BindView(R.id.data_collection_btn)
    Button dataCollectionBtn;
    @BindView(R.id.data_socre_btn)
    Button dataScoreBtn;


    MainContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPresenter=new MainPresenter(this);
    }

    @OnClick({R.id.data_collection_btn, R.id.data_socre_btn})
    public void onViewClicked(View view) {

        if(mPresenter.isLogin())
        {
            switch (view.getId()) {
                case R.id.data_collection_btn:
                    startActivity(CollectDataMenuActivity.class);
                    break;
                case R.id.data_socre_btn:
                    startActivity(DataListActivity.class);
                    break;
                default:
            }
        }
        else
        {
            startActivity(LoginActivity.class);
        }
    }

    @Override
    public void showError(String msg) {
        ToastUtils.show(getApplicationContext(),msg);
    }

    public void startActivity(Class target)
    {
        if(target!=null)
        {
            Intent intent=new Intent(getApplicationContext(),target);
            startActivity(intent);
        }
    }
}
