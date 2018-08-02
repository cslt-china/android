package com.google.android.apps.cslt.ui.data_score;

import android.app.Activity;

import com.google.android.apps.cslt.bean.ScoreDataItem;

import java.util.ArrayList;
import java.util.List;

public class DataListPresenter implements DataListContract.Presenter {

    DataListContract.View mView;

    final static Integer PAGE_SIZE=10;
    List<ScoreDataItem> scoreDataItemList=new ArrayList<>();

    public DataListPresenter(DataListContract.View mView) {
        initData();
        this.mView = mView;
    }

    public void initData()
    {

        for (int i=0;i<100;i++)
        {
            scoreDataItemList.add(new ScoreDataItem(String.valueOf(i),i));
        }
    }

    @Override
    public void LoadData(final Integer page) {


        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                List<ScoreDataItem> newScoreDataItemList=new ArrayList<>();
                mView.showLoading();
                try {
                    Thread.sleep(3000);
                    for(int i=(page-1)*PAGE_SIZE;i<page*PAGE_SIZE;i++)
                    {
                        newScoreDataItemList.add(scoreDataItemList.get(i));
                    }

                    mView.hideLoading();
                    mView.updateData(newScoreDataItemList);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }
}
