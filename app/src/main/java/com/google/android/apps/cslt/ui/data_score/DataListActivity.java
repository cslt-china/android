package com.google.android.apps.cslt.ui.data_score;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.animation.BaseAnimation;
import com.google.android.apps.cslt.R;
import com.google.android.apps.cslt.bean.ScoreDataItem;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DataListActivity extends AppCompatActivity implements DataListContract.View {


    @BindView(R.id.back_btn)
    TextView backBtn;
    @BindView(R.id.back_title_tv)
    TextView backTitleTv;
    @BindView(R.id.data_score_list)
    RecyclerView dataScoreList;

    List<ScoreDataItem> scoreDataItemList=new ArrayList<>();

    DataListContract.Presenter mPresenter;

    BaseQuickAdapter scoreListAdapter;
    Integer pageNum=1;

    final String TAG=DataListActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_list);
        ButterKnife.bind(this);
        initAdapter();
        mPresenter=new DataListPresenter(this);
        initData();
    }

    @OnClick({R.id.back_btn, R.id.data_score_list})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_btn:
                break;
            case R.id.data_score_list:
                break;
                default:
        }
    }
    public void initData()
    {
        mPresenter.LoadData(pageNum);
        scoreListAdapter.loadMoreComplete();
    }
    public void initAdapter()
    {

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        dataScoreList.setLayoutManager(linearLayoutManager);
        scoreListAdapter=new ScoreListAdapter(R.layout.item_data_score,scoreDataItemList);
        scoreListAdapter.openLoadAnimation(new BaseAnimation() {
            @Override
            public Animator[] getAnimators(View view) {
                return new Animator[]{
                        ObjectAnimator.ofFloat(view, "scaleY", 1, 1.1f, 1),
                        ObjectAnimator.ofFloat(view, "scaleX", 1, 1.1f, 1)
                };
            }
        });
        scoreListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                startWatchVideoActivity(position);
            }
        });
        scoreListAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mPresenter.LoadData(pageNum++);
            }
        },dataScoreList);
        scoreListAdapter.disableLoadMoreIfNotFullPage();
        dataScoreList.setAdapter(scoreListAdapter);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {
        scoreListAdapter.loadMoreComplete();
    }

    @Override
    public void updateData(List<ScoreDataItem> scoreDataItemList) {
        scoreListAdapter.addData(scoreDataItemList);
    }

    public void startWatchVideoActivity(Integer position)
    {
        ScoreDataItem scoreDataItem;
        if(position>-1&&position<scoreDataItemList.size()){
            scoreDataItem=scoreDataItemList.get(position);
            if(scoreDataItem!=null)
            {
                Intent intent=new Intent(getApplicationContext(),WatchVideoActivity.class);
                intent.putExtra("score_item",scoreDataItem);
                startActivity(intent);
            }
        }
    }
}
