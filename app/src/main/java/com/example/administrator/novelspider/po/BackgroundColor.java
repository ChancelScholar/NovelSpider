package com.example.administrator.novelspider.po;

/**
 * Created by Administrator on 2018/9/26 0026.
 * 背景颜色实体类
 */

public class BackgroundColor {
    private String code;    //十六进制代码
    private int imageId;

    public BackgroundColor(String code, int imageId){
        this.code = code;
        this.imageId = imageId;
    }

    public String getCode(){
        return code;
    }

    public int getImageId(){
        return imageId;
    }

    public void setCode(String code){
        this.code = code;
    }

    public void setImageId(int imageId){
        this.imageId = imageId;
    }
}
