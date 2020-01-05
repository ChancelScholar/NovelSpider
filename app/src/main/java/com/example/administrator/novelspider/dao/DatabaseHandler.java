package com.example.administrator.novelspider.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.example.administrator.novelspider.dbhelper.BookDatabaseHelper;
import com.example.administrator.novelspider.po.Book;
import com.example.administrator.novelspider.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/1/14 0014.
 */

public class DatabaseHandler {
    public static final int SUCCESS = 1;       //操作成功
    public static final int FAIL = 2;          //操作失败
    public static final int NOT_EXIST = 3;    //查找的数据不存在
    private ImageHandle imageHandle = new ImageHandle();    //图像处理类
    private BookDatabaseHelper helper;

    public DatabaseHandler(BookDatabaseHelper helper) {
        this.helper = helper;
    }

    //插入书籍信息
    public void insertBook(Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("bookNum", book.getBookNum());
        values.put("name", book.getName());
        values.put("chapterNum", book.getChapterNum());
        values.put("bookImage", book.getBookImage());
        values.put("imageUrl", book.getImageUrl());
        db.insert("Book", null, values);
    }

    //更新书籍信息
    public int updateBook(Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Book", null, "bookNum = ?", new String[]{book.getBookNum()}, null, null, null);
        if(cursor.getCount() <= 0){
            return NOT_EXIST;
        }
        ContentValues values = new ContentValues();
        values.put("chapterNum", book.getChapterNum());
        db.update("Book", values, "bookNum = ?", new String[]{book.getBookNum()});
        return SUCCESS;
    }

    //获取书目
    public List<Book> getAllBooks(){
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Book", null, null, null, null, null, "id");
        if(cursor.moveToFirst()){    //判断有无记录
            do {
                Book book = new Book();
                book.setId(cursor.getInt(cursor.getColumnIndex("id")));
                book.setBookNum(cursor.getString(cursor.getColumnIndex("bookNum")));
                book.setName(cursor.getString(cursor.getColumnIndex("name")));
                book.setChapterNum(cursor.getString(cursor.getColumnIndex("chapterNum")));
                book.setBookImage(cursor.getString(cursor.getColumnIndex("bookImage")));
                //因数据库无法存储bitmap对象，故需对每一本书根据图像名重新加载封面
                book.setBitmap(imageHandle.getImageBitmap(book.getBookImage()));
                books.add(book);
            }while(cursor.moveToNext());
        }
        return books;
    }

    //删除书籍信息
    public void deleteBook(Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("Book", "bookNum = ?", new String[]{book.getBookNum()});
        //删除缓存
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File imageFile = new File(directory + "/" + book.getBookImage());
        File fileData = new File(directory + "/novelspider/" + book.getBookNum());
        if(fileData.exists() || imageFile.exists()){
            FileUtil.deleteAllFile(fileData);
            imageFile.delete();
        }
    }

    //根据bookNum获取书籍信息
    public Book getBookById(String bookNum){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("Book", null, "bookNum = ?", new String[]{bookNum}, null, null, null);
        Book book = new Book();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            book.setId(cursor.getInt(cursor.getColumnIndex("id")));
            book.setBookNum(cursor.getString(cursor.getColumnIndex("bookNum")));
            book.setName(cursor.getString(cursor.getColumnIndex("name")));
            book.setChapterNum(cursor.getString(cursor.getColumnIndex("chapterNum")));
            book.setBookImage(cursor.getString(cursor.getColumnIndex("bookImage")));
            //因数据库无法存储bitmap对象，故需对每一本书根据图像名重新加载封面
            book.setBitmap(imageHandle.getImageBitmap(book.getBookImage()));
            return book;
        }
        return null;
    }
}
