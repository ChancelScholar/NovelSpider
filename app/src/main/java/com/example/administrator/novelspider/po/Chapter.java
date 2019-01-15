package com.example.administrator.novelspider.po;

/**
 * Created by Administrator on 2018/11/7 0007.
 */

public class Chapter {
    private String id;        //章节号
    private String name;     //章节名
    private String url;      //章节获取地址

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
