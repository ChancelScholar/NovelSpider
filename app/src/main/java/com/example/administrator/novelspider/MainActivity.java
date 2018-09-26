package com.example.administrator.novelspider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.util.StringParser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText bookId=null;
    EditText chapterId=null;
    Button confirm=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookId=(EditText) findViewById(R.id.book_id);
        chapterId=(EditText) findViewById(R.id.chapter_id);
        confirm=(Button) findViewById(R.id.confirm);
        confirm.setOnClickListener(this);
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
        bookId.setText(bookIdNum);
        chapterId.setText(chapterIdNum);
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
