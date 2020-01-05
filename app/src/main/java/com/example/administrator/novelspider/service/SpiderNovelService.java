package com.example.administrator.novelspider.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.administrator.novelspider.listener.SearchListener;
import com.example.administrator.novelspider.po.Book;
import com.example.administrator.novelspider.po.Content;
import com.example.administrator.novelspider.util.URLParser;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 爬取小说服务
 * Created by Administrator on 2019/6/22 0022.
 */

public class SpiderNovelService {
    private SearchListener listener;

    private boolean isCancel = false;

    public void setCancel(boolean cancel){
        isCancel = cancel;
    }

    public SpiderNovelService(SearchListener listener) {
        this.listener = listener;
    }

    public Book novelSpider(String url){
        Document novelDoc = null;

        try{
            novelDoc = Jsoup.connect(url).get();
        }catch (IOException e){
            System.out.println("获取内容失败");
            listener.fail(e);
            return null;
        }

        //进行解析
        Element body = novelDoc.body();
        Elements chapterList = body.getElementById("list").getAllElements().get(0).getAllElements().get(1).children();
        //找到多余的章节并移除
        int dtcount = 0;
        List<Element> redundantChapters = new ArrayList<Element>();
        for(Element chapter:chapterList){
            if(chapter.tagName().equalsIgnoreCase("dt")){
                dtcount++;
                redundantChapters.add(chapter);
                if(dtcount == 2){
                    break;
                }
            }else{
                redundantChapters.add(chapter);
            }
        }
        Iterator<Element> it = redundantChapters.iterator();
        while(it.hasNext()){
            Element redundantChapter = (Element)it.next();
            chapterList.remove(redundantChapter);
        }

        //生成章节列表
        List<Content> chapters = new ArrayList<>();
        for(Element element: chapterList){
            Content chapter = new Content();
            chapter.setChapterId(URLParser.getChapterId(element.child(0).attr("href")));
            chapter.setChapterName(element.text());
            chapters.add(chapter);
        }

        //生成书籍
        Element novelInfo = body.getElementById("info");
        String id = URLParser.getBookId(url);
        String name = novelInfo.child(0).text();
        String imageUrl = URLParser.HOME_URL + body.getElementById("fmimg").getAllElements().get(1).attr("src");
        String author = novelInfo.child(1).child(0).text();
        String introduction = body.getElementById("intro").text();
        Book novel = new Book(id, name, author, introduction, imageUrl, chapters);

        return novel;
    }

    public void searchNovel(String name){
        Connection con;
        Document text = null;
        String url = URLParser.HOME_URL + "/index.php?s=/web/index/search";
        try{
            con = Jsoup.connect(url);
            con.data("name", name);
            text = con.post();
        }catch (IOException e){
            System.out.println("获取内容失败");
        }

        Element body = text.body();
        //解析结果
        Elements main = body.getElementsByClass("novelslist2");
        if(main.size() <= 0){
            //无搜索结果
            listener.success(null);
            return;
        }
        Elements novelElements = main.get(0).child(1).children();
        //去表头
        novelElements.remove(0);
        //解析所有书籍
        List<Book> novels = new ArrayList<Book>();
        for(Element element: novelElements){
            if(isCancel) break;
            String novelUrl = URLParser.HOME_URL + element.getElementsByClass("s2").first().child(0).attr("href");
            //获取解析结果
            Book novel = novelSpider(novelUrl);
            if(novel != null){
                novels.add(novel);
            }else{
                break;
            }

            //隔两秒请求一次
            try{
                Thread.sleep(2000);
            }catch (InterruptedException e){
                Log.i("SpiderNovel:", "休眠打断");
            }

        }
        if(novels.size() > 0 && !isCancel){
            listener.success(novels);
        }
    }
}
