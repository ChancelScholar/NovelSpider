package com.example.administrator.novelspider.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.example.administrator.novelspider.listener.ProcessListener;
import com.example.administrator.novelspider.task.SpiderTask;

public class SpiderService extends Service {
    private SpiderTask spiderTask;    //爬虫线程
    private String downloadUrl;      //爬取的地址

    public SpiderService() {
    }



    private SpiderBinder mBinder = new SpiderBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class SpiderBinder extends Binder {
        //开始派取网页，processListener为完成页面分析后的回调接口
        public void startSpider(String url, ProcessListener processListener){
            //为提高效率，避免重复创建
            downloadUrl = url;
            if(spiderTask == null){
                spiderTask = new SpiderTask(processListener);
                spiderTask.execute(downloadUrl);
            }
        }

        //释放线程资源
        public void releaseTask(){
            spiderTask = null;
        }
    }
}
