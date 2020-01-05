package com.example.administrator.novelspider.listener;

import android.view.View;

/**
 * Created by Administrator on 2019/6/23 0023.
 */

public interface AddBookListener {
    void success(View view);

    void fail(Exception e);
}
