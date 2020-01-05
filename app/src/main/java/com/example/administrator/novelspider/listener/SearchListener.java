package com.example.administrator.novelspider.listener;

import com.example.administrator.novelspider.po.Book;

import java.util.List;

/**
 * Created by Administrator on 2019/6/23 0023.
 */

public interface SearchListener{
    void success(List<Book> books);

    void fail(Exception e);
}
