package com.baima.mediaplayer.entities;

import org.litepal.crud.LitePalSupport;

public class Music extends LitePalSupport {

    private long id;
    private String path;
    private long addDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getAddDate() {
        return addDate;
    }

    public void setAddDate(long addDate) {
        this.addDate = addDate;
    }
}
