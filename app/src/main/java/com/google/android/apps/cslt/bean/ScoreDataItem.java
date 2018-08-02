package com.google.android.apps.cslt.bean;

import java.io.Serializable;

public class ScoreDataItem implements Serializable{
    String title;
    Integer id;

    public ScoreDataItem(String title, Integer id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
