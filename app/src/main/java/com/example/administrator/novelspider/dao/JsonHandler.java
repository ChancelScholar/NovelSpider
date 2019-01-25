package com.example.administrator.novelspider.dao;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.example.administrator.novelspider.po.Chapter;
import com.example.administrator.novelspider.po.Content;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2019/1/14 0014.
 */

public class JsonHandler {
    //保存章节为Json数据
    public void saveChapter(Content content) throws IOException{
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory + "/novelspider/" + content.getBookId() + "/chapters/" + content.getChapterId() + ".json");
        if(!file.exists()){    //文件不存在则创建新文件
            boolean parentResult= file.getParentFile().mkdirs();
            boolean fileResult = file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        Gson gson = new Gson();
        //把章节列表对象转换为json数据
        String jsonStr = gson.toJson(content);
        writer.write(jsonStr);
        writer.flush();
    }

    //删除章节数据
    public void deleteChapter(Content content) throws IOException{
        if(content == null) return;
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory + "/novelspider/" + content.getBookId() + "/chapters/" + content.getChapterId() + ".json");
        if(file.exists()){    //文件存在则删除新文件
            file.delete();
        }
    }

    //获取章节数据
    public Map<String, Content> getChapters(String bookId) throws IOException{
        Map<String, Content> contents = new HashMap<>();
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String filePath = directory + "/" + "novelspider/" + bookId + "/chapters";
        File fileDirectory = new File(filePath);
        if(fileDirectory.isDirectory()) {
            String[] fileList = fileDirectory.list();
            for (int i = 0; i < fileList.length; i++) {
                File file = new File(filePath + "/" + fileList[i]);
                if (!file.isDirectory()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    //读取json数据
                    String line = null;
                    StringBuilder jsonBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                    String jsonStr = jsonBuilder.toString();
                    //解析json数据
                    Gson gson = new Gson();
                    Content content = gson.fromJson(jsonStr, new TypeToken<Content>() {}.getType());
                    contents.put(content.getChapterId(), content);
                }
            }
        }
        return contents.size() > 0?contents:null;
    }

    //保存章节列表为Json数据
    public void saveChapterList(List<Chapter> chapterList, String fileName) throws IOException {
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory + "/"+ "novelspider/" + fileName);
        if(!file.exists()){    //文件不存在则创建新文件
            boolean parentResult= file.getParentFile().mkdirs();
            boolean fileResult = file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        Gson gson = new Gson();
        //把章节列表对象转换为json数据
        String jsonStr = gson.toJson(chapterList);
        writer.write(jsonStr);
        writer.flush();
    }

    //读取章节列表
    public List<Chapter> getChapterList(String fileName) throws FileNotFoundException, IOException{
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory + "/" + "novelspider/" +  fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        //读取json数据
        String line = null;
        StringBuilder jsonBuilder = new StringBuilder();
        while((line = reader.readLine()) != null){
            jsonBuilder.append(line);
        }
        String jsonStr = jsonBuilder.toString();
        //解析json数据
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, new TypeToken<List<Chapter>>(){}.getType());
    }

    //保存章节列表序列号
    public void saveChapterSeriesNum(Map<String, Integer> chapterSeriesNum, String fileName) throws IOException {
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory + "/" + "novelspider/" + fileName);
        if(!file.exists()){    //文件不存在则创建新文件
            boolean parentResult = file.getParentFile().mkdirs();
            boolean fileResult = file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        Gson gson = new Gson();
        //把章节列表对象转换为json数据
        String jsonStr = gson.toJson(chapterSeriesNum);
        writer.write(jsonStr);
        writer.flush();
    }

    //获取章节列表序列号
    public Map<String, Integer> getChapterSeriesNum(String fileName) throws IOException{
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory + "/" + "novelspider/" +  fileName);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        //读取json数据
        String line = null;
        StringBuilder jsonBuilder = new StringBuilder();
        while((line = reader.readLine()) != null){
            jsonBuilder.append(line);
        }
        String jsonStr = jsonBuilder.toString();
        //解析json数据
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, new TypeToken<Map<String, Integer>>(){}.getType());
    }
}
