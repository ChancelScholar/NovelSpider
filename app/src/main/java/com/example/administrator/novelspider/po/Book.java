package com.example.administrator.novelspider.po;

import android.graphics.Bitmap;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/10/9 0009.
 *记录书目当前阅读的位置表
 */

public class Book extends DataSupport{
    private int id;
    private String bookNum;     //爬取的网站的书号
    private String name;         //书名
    private String chapterNum;  //当前的爬取的网站的章节号
    private String bookImage;   //书籍封面图片文件名
    private String imageUrl;    //封面路径
    private Bitmap bitmap;      //图像文件
    private List<Content> chapters;     //章节目录
    private String introduction;               //简介
    private String author;      //作者

    public Book() {
    }

    public Book(String bookNum, String name, String author, String introduction, String imageUrl, List<Content> chapters) {
        this.bookNum = bookNum;
        this.name = name;
        this.imageUrl = imageUrl;
        this.chapters = chapters;
        this.introduction = introduction;
        this.author = author;
    }

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

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public List<Content> getChapters() {
        return chapters;
    }

    public void setChapters(List<Content> chapters) {
        this.chapters = chapters;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
