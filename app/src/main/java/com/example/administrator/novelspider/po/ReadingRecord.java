package com.example.administrator.novelspider.po;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2018/10/9 0009.
 *记录书目当前阅读的位置表
 */

public class ReadingRecord extends DataSupport{
    private int id;
    private String bookNum;    //爬取的网站的书号
    private String name;         //书名
    private String chapterNum;  //当前的爬取的网站的章节号

    public int getId(){
        return id;
    }

    public String getBookNum(){
        return bookNum;
    }

    public String getName(){
        return name;
    }

    public String getChapterNum(){
        return chapterNum;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setBookNum(String bookNum){
        this.bookNum = bookNum;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setChapterNum(String chapterNum){
        this.chapterNum = chapterNum;
    }
}
