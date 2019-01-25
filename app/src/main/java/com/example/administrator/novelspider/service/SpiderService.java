package com.example.administrator.novelspider.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.example.administrator.novelspider.ReadingActivity;
import com.example.administrator.novelspider.dao.JsonHandler;
import com.example.administrator.novelspider.listener.ProcessListener;
import com.example.administrator.novelspider.po.Content;
import com.example.administrator.novelspider.task.SpiderTask;
import com.example.administrator.novelspider.util.URLParser;

import java.io.IOException;

public class SpiderService extends Service {
    public static final int START = 1;
    public static final int STOP = 2;

    private SpiderTask spiderTask;       //爬虫线程
    private String downloadUrl;          //爬取的地址
    private boolean isFirstRequest = true;    //是否是第一次发出请求，用于获取最新章节信息
    private JsonHandler jsonHandler = new JsonHandler();    //数据保存类
    private boolean isPause = false;    //是否暂停爬取
    //爬虫接口，完成网页解析后保存章节数据，并继续启动爬虫获取数据，实现缓存的效果
    private ProcessListener listener = new ProcessListener() {
        @Override
        public void success(Content content) {
            try {
                jsonHandler.saveChapter(content);
            } catch (IOException e) {
                Log.d("SaveChapter:", "保存章节失败");
            }
            //把该章节添加到章节数据
            ReadingActivity.addContent(content);
            mBinder.releaseTask();
            if(!isPause && !URLParser.isLastChapter(content.getNextChapterLink())){
                mBinder.startSpider(content.getNextChapterLink());
            }
        }

        @Override
        public void fail() {
            Log.d("GetChapter:", "获取章节失败");
        }
    };

    public SpiderService() {
    }

    private SpiderBinder mBinder = new SpiderBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class SpiderBinder extends Binder {
        //开始派取网页，processListener为完成页面分析后的回调接口
        public void startSpider(String url){
            isPause = false;
            //查看本地章节列表，是否已存在该章节,存在则跳过，直至到保存的最后一章
            Content content = null;
            while(ReadingActivity.contentIsExist(URLParser.getChapterId(url))){
                content = ReadingActivity.getContent(URLParser.getChapterId(url));
                if(URLParser.isLastChapter(content.getNextChapterLink())){
                    break;
                }else{
                    url = content.getNextChapterLink();
                }
            }
            //避免循环获取最后一章，用canRequest控制
            boolean canRequest = true;
            //当content不为空时才检查是否是最后一章
            if(content != null && URLParser.isLastChapter(content)){
                if(isFirstRequest){
                    isFirstRequest = false;
                }else{
                    canRequest = false;
                }
            }
            //为提高效率，避免重复创建并判断是否进行获取页面申请
            if(canRequest) {
                downloadUrl = url;
                if (spiderTask == null) {
                    spiderTask = new SpiderTask(listener);
                    spiderTask.execute(downloadUrl);
                }
            }
        }

        //释放线程资源
        public void releaseTask(){
            spiderTask = null;
        }

        //暂停爬取
        public void pause(){
            isPause = true;
        }

        //获取缓存状态
        public int getCacheStatus(){
            return !isPause?START:STOP;
        }
    }
}
