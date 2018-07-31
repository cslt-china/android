package com.google.android.apps.cslt.ui.main;

public interface MainContract {


    interface View {
        public void showError(String msg);
    }

    interface Presenter {
        public boolean isLogin();
    }
}
