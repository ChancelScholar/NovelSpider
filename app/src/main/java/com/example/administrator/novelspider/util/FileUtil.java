package com.example.administrator.novelspider.util;

import java.io.File;

/**
 * Created by Administrator on 2019/3/15 0015.
 */

public class FileUtil {

    public static void deleteAllFile(File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(int i=0; i<files.length; i++){
                deleteAllFile(files[i]);
            }
            file.delete();
            return;
        }else if(file.isFile()){
            file.delete();
            return;
        }
    }
}
