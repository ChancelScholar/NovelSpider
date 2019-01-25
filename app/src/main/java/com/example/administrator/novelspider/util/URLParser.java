package com.example.administrator.novelspider.util;

import android.graphics.Bitmap;

import com.example.administrator.novelspider.po.Content;

/**
 * 网页url解析工具
 * Created by Administrator on 2019/1/14 0014.
 */

public class URLParser {
    //获取书号
    public static String getBookId(String url){
        String bookId = url.split("/")[4];
        return bookId;
    }

    //获取章节号
    public static String getChapterId(String url){
        String chapterId = url.split("/")[5];
        chapterId = chapterId.substring(0, chapterId.length() - ".html".length());
        return chapterId;
    }

    //判断是最后一章
    public static boolean isLastChapter(Content content){
        if(content.getNextChapterLink().split("/").length != 6){
            return true;
        }
        return false;
    }

    //判断这个url的上一章是最后一章
    public static boolean isLastChapter(String url){
        if(url.split("/").length != 6){
            return true;
        }
        return false;
    }
}
