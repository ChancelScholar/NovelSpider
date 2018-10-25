package com.example.administrator.novelspider.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import junit.runner.Version;

/**
 * Created by Administrator on 2018/10/24 0024.
 */

public class BookDatabaseHelper extends SQLiteOpenHelper{
    public static final String CREATE_BOOK = "create table Book("
            + "id integer primary key,"
            + "chapterNum text,"
            + "name text,"
            + "bookImage text,"
            +"imageUrl text)";

    private Context mContext;

    public BookDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
