package com.example.administrator.novelspider.listener;

import com.example.administrator.novelspider.po.Content;

/**
 * Created by Administrator on 2018/10/18 0018.
 */

public interface ProcessListener {

    public void success(Content content);    //页面解析完成
    public void fail();     //页面解析失败
}
