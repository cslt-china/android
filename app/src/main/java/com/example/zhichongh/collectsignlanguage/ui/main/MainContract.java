package com.example.zhichongh.collectsignlanguage.ui.main;

public interface MainContract {


    interface View {
        public void showError(String msg);
    }

    interface Presenter {
        public boolean isLogin();
    }
}
