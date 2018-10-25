package com.example.administrator.novelspider.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.RandomAccess;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/10/21 0021.
 */

public class ImageFileHandleUtil {
    public static final int SUCCESS = 0;
    public static final int FAIL = -1;

    //保存图像文件
    public static int saveImageFile(String url){
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try{
            String fileName = url.substring(url.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if(file.exists()){
                return SUCCESS;
            }else{
                file.createNewFile();
            }
            //已下载长度
            long downloadedLength = 0;
            OkHttpClient client = new OkHttpClient();
            //获取返回内容
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            if(response != null){
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                byte[] b = new byte[1024];
                int len = 0;
                //保存文件
                while((len = is.read(b)) != -1){
                    saveFile.write(b, 0, len);
                }
            }
            response.body().close();
            return SUCCESS;
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            try{
                if(is != null||saveFile != null){
                    is.close();
                    saveFile.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return FAIL;
    }

    //读取图像文件
    public static Bitmap getImageBitmap(String fileName){
        File file = null;
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + fileName;
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeFile(directory);
        return bitmap;
    }
}
