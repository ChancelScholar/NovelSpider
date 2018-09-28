package com.example.administrator.novelspider;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.util.StatusBarCompat;
import com.example.administrator.novelspider.util.StringParser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText bookId=null;         //书号
    EditText chapterId=null;     //章节号
    Button confirm=null;    //确认按钮
    String code;     //颜色十六进制代码
    LinearLayout mainFrame;     //主布局管理器
    TextView webLink;       //网页链接文本

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookId = (EditText) findViewById(R.id.book_id);
        chapterId = (EditText) findViewById(R.id.chapter_id);
        confirm = (Button) findViewById(R.id.confirm);
        webLink = (TextView) findViewById(R.id.web_link);
        mainFrame = (LinearLayout) findViewById(R.id.main_frame);
        confirm.setOnClickListener(this);
        webLink.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(webLink.getText());
                return false;
            }
        });
        ActionBar actionBar = getSupportActionBar();
        //隐藏标题栏
        if(actionBar!=null){
            actionBar.hide();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //为了在活动置顶时重新获得书号和章节号，在onResume方法中获取存储的书号以及章节号
        SharedPreferences preferences = getSharedPreferences("novel_data",MODE_PRIVATE);
        String bookIdNum = preferences.getString("book_id","");
        String chapterIdNum = preferences.getString("chapter_id", "");
        float textSize = preferences.getFloat("textSize",25);
        String code = preferences.getString("backgroundColor","#C7EDCC");
        bookId.setText(bookIdNum);
        chapterId.setText(chapterIdNum);
        //根据用户设置来初始化字体大小
        ReadingActivity.setTextSize(textSize);
        //根据用户设置来初始化背景色
        ReadingActivity.setBackgroundColor(code);
        //更改状态栏背景色
        StatusBarCompat.compat(MainActivity.this, Color.parseColor("#C7EDCC"));
    }

    @Override
    public void onClick(View v){
        if(v.getId()==R.id.confirm){
            //获取书号以及章节号
            String bookIdStr = bookId.getText().toString();
            String chapterIdStr = chapterId.getText().toString();
            //判空操作
            if(StringParser.isEmpty(bookIdStr) || StringParser.isEmpty(chapterIdStr)){
                Toast.makeText(MainActivity.this,"书号或章节号未输入",Toast.LENGTH_SHORT);
            }else{
                Intent intent = new Intent(MainActivity.this,ReadingActivity.class);
                intent.putExtra("book_id", bookIdStr);
                intent.putExtra("chapter_id",chapterIdStr);
                startActivity(intent);
            }
        }
    }
}
