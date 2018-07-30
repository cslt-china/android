package com.example.zhichongh.collectsignlanguage.ui.main;

public class MainPresenter implements MainContract.Presenter {

    MainContract.View mView;

    public MainPresenter(MainContract.View mView) {
        this.mView = mView;
    }

    @Override
    public boolean isLogin() {
        return true;
    }
}
