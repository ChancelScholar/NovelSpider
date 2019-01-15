package com.example.administrator.novelspider.task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.administrator.novelspider.listener.ProcessListener;
import com.example.administrator.novelspider.po.Content;
import com.example.administrator.novelspider.util.URLParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Created by Administrator on 2018/10/18 0018.
 */

public class SpiderTask extends AsyncTask<String, Integer, Integer> {
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAIL = 1;

    private ProcessListener processListener;     //页面解析完成的回调接口
    Content content = new Content();    //页面解析完成后存储该页面资料的类

    public SpiderTask(ProcessListener processListener){
        this.processListener = processListener;
    }

    @Override
    protected Integer doInBackground(String... params){
        String downloadUrl = params[0];
        try {
            Document page = Jsoup.connect(downloadUrl).timeout(100000).get();     //设置等待时间最大为10秒
            Element body = page.body();
            //抽取小说名，小说章节标题、内容、上一章链接，下一章链接
            String novelName = body.getElementsByClass("con_top").get(0).child(2).text();
            String chapterTitle = body.getElementsByTag("h1").text();
            //使用first和last截取小说内容
            int first = "    天才一秒记住本站地址：[博看小说网]\nhttp://www.bkxs.net/最快更新！无广告！".length();
            int last = "章节错误,点此报送(免注册),\n报送后维护人员会在两分钟内校正章节内容,请耐心等待。".length();
            String novelContent = body.getElementById("content").text();
            novelContent = novelContent.replace(" ","\n");
            novelContent = novelContent.substring(first, novelContent.length() - last);
            String lastChapterLink = "http://www.bkxs.net"+body.getElementsByClass("pre").get(0).attr("href");
            String nextChapterLink = "http://www.bkxs.net"+body.getElementsByClass("next").get(0).attr("href");
            //保存书号、章节号、章节名、章节内容、上一章链接、下一章链接、书名
            content.setBookId(URLParser.getBookId(downloadUrl));
            content.setChapterId(URLParser.getChapterId(downloadUrl));
            content.setChapterName(chapterTitle);
            content.setContent(novelContent);
            content.setLastChapterLink(lastChapterLink);
            content.setNextChapterLink(nextChapterLink);
            content.setBookName(novelName);
            //休眠5秒后继续下个爬取任务
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e) {
                Log.d("ThreadBug:", "线程无响应");
            }
            return TYPE_SUCCESS;
        }catch (IOException e){
            e.printStackTrace();
            return TYPE_FAIL;
        }
    }

    @Override
    protected void onPostExecute(Integer status){
        switch (status){
            case TYPE_SUCCESS:
                processListener.success(content);
                break;
            case TYPE_FAIL:
                processListener.fail();
                break;
            default:
                break;
        }
    }
}
