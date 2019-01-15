package com.example.administrator.novelspider.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.administrator.novelspider.dbhelper.BookDatabaseHelper;
import com.example.administrator.novelspider.po.Book;

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

    //插入书籍信息
    public void insertBook(BookDatabaseHelper helper, Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", Integer.valueOf(book.getBookNum()));
        values.put("name", book.getName());
        values.put("chapterNum", book.getChapterNum());
        values.put("bookImage", book.getBookImage());
        values.put("imageUrl", book.getImageUrl());
        db.insert("Book", null, values);
    }

    //更新书籍信息
    public int updateBook(BookDatabaseHelper helper, Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Book", null, "id = ?", new String[]{book.getBookNum()}, null, null, null);
        if(cursor.getCount() <= 0){
            return NOT_EXIST;
        }
        ContentValues values = new ContentValues();
        values.put("chapterNum", book.getChapterNum());
        db.update("Book", values, "id = ?", new String[]{book.getBookNum()});
        return SUCCESS;
    }

    //获取书目
    public List<Book> getAllBooks(BookDatabaseHelper helper){
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Book", null, null, null, null, null, null);
        if(cursor.moveToFirst()){    //判断有无记录
            cursor.moveToLast();     //因返回的数据与插入顺序是相反的，故从尾端遍历
            do {
                Book book = new Book();
                book.setBookNum(String.valueOf(cursor.getInt(cursor.getColumnIndex("id"))));
                book.setName(cursor.getString(cursor.getColumnIndex("name")));
                book.setChapterNum(cursor.getString(cursor.getColumnIndex("chapterNum")));
                book.setBookImage(cursor.getString(cursor.getColumnIndex("bookImage")));
                //因数据库无法存储bitmap对象，故需对每一本书根据图像名重新加载封面
                book.setBitmap(imageHandle.getImageBitmap(book.getBookImage()));
                books.add(book);
            }while(cursor.moveToPrevious());
        }
        return books;
    }

    //删除书籍信息
    public void deleteBook(BookDatabaseHelper helper, Book book){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("Book", "id = ?", new String[]{book.getBookNum()});
    }

    //根据id获取书籍信息
    public Book getBookById(BookDatabaseHelper helper, String id){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("Book", null, "id = ?", new String[]{id}, null, null, null);
        Book book = new Book();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            book.setBookNum(String.valueOf(cursor.getInt(cursor.getColumnIndex("id"))));
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
