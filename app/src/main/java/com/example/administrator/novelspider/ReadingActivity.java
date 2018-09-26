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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.util.StringParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReadingActivity extends AppCompatActivity implements View.OnClickListener{
    TextView contentText;    //文章内容
    TextView chapterText;    //文章标题
    Button lastChapter;      //上一章按钮
    Button nextChapter;      //下一章按钮
    String bookLibURL = "http://www.bkxs.net";    //书库主页网址
    String lastChapterURL = null;    //上一章链接
    String nextChapterURL = null;    //下一章链接
    ScrollView contentScrollView = null;    //放置文章的滚动模块

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        contentText = (TextView) findViewById(R.id.content);
        chapterText = (TextView) findViewById(R.id.chapter);
        lastChapter = (Button) findViewById(R.id.lastChapter);
        nextChapter = (Button) findViewById(R.id.nextChapter);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        Intent intent = getIntent();
        String bookId = intent.getStringExtra("book_id");
        String chapterId = intent.getStringExtra("chapter_id");
        sendRequestWithOkHttp(bookLibURL + "/bkxs/"+ bookId + "/" + chapterId + ".html");
        lastChapter.setOnClickListener(this);
        nextChapter.setOnClickListener(this);
        ActionBar actionBar = getSupportActionBar();
        //隐藏标题栏
        if(actionBar!=null){
            actionBar.hide();
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.lastChapter:
                sendRequestWithOkHttp(lastChapterURL);
                break;
            case R.id.nextChapter:
                sendRequestWithOkHttp(nextChapterURL);
                break;
            default:
                Toast.makeText(ReadingActivity.this,"软件出现未知问题，请尽快联系软件制作人员",Toast.LENGTH_SHORT);
                break;
        }
    }

    //跳转后回到顶部
    private void backToTop(){
        contentScrollView.scrollTo(0,0);
    }

    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //如果返回的是空串，则销毁该活动
                if(StringParser.isEmpty(response)){
                    Toast.makeText(ReadingActivity.this,"书号或章节号输入错误，请重新输入",Toast.LENGTH_SHORT);
                    Log.v("Destroy:","111");
                    onDestroy();
                }
                //文本内容生成器
                StringBuilder stringBuilder = new StringBuilder();
                //文本内容匹配
                Pattern patternContent = Pattern.compile("(&nbsp;){4}[\\S&&[^<&]]+");
                //章节名
                Pattern patternChapter = Pattern.compile("<h1>第\\d+章\\s*\\w+");
                //上一章链接
                Pattern patternLastChapter = Pattern.compile("<a href=\"/bkxs/\\d{1,6}/\\d{1,10}.html\" target=\"_top\" class=\"pre\">上一章</a>");
                //下一章链接
                Pattern patternNextChapter = Pattern.compile("<a href=\"/bkxs/\\d{1,6}/\\d{1,10}.html\" target=\"_top\" class=\"next\">下一章</a>");
                Matcher matcherContent = patternContent.matcher(response);
                Matcher matcherChapter = patternChapter.matcher(response);
                Matcher matcherLastChapter = patternLastChapter.matcher(response);
                Matcher matcherNextChapter = patternNextChapter.matcher(response);
                String chapter = null;
                // 查找章节名
                if(matcherChapter.find()){
                    chapter = response.substring("<h1>".length()+matcherChapter.start(),matcherChapter.end());
                }
                //查找上一章链接
                if(matcherLastChapter.find()){
                    lastChapterURL =bookLibURL + response.substring("<a href=\"".length()+matcherLastChapter.start(),matcherLastChapter.end()-"\" target=\"_top\" class=\"pre\">上一章</a>".length());
                }
                //查找下一章链接
                if(matcherNextChapter.find()){
                    nextChapterURL = bookLibURL + response.substring("<a href=\"".length()+matcherNextChapter.start(),matcherNextChapter.end()-"\" target=\"_top\" class=\"next\">下一章</a>".length());
                }
                Log.d("上一章链接：",lastChapterURL);
                Log.d("下一章链接：",nextChapterURL);
                //查找文章内容并按一定格式生成文本
                while(matcherContent.find()){
                    String content = response.substring(matcherContent.start()+"&nbsp;".length()*4, matcherContent.end());
                    stringBuilder.append("        "+content+"\n");
                }
                chapterText.setText(chapter);
                //responseText.setBackgroundColor(Color.rgb(199, 237, 204));
                contentText.setText(stringBuilder.toString());
                //处理完信息后回滚到文本头
                backToTop();
            }
        });
    }

    private void sendRequestWithOkHttp(final String address){
        //每请求一次数据就保存一次书号以及章节号
        String[] bookNum = address.substring(bookLibURL.length()+1).split("/");
        if(bookNum[2].endsWith(".html")){
            bookNum[2] = bookNum[2].substring(0, bookNum[2].length()-".html".length());
        }
        SharedPreferences.Editor editor = getSharedPreferences("novel_data",MODE_PRIVATE).edit();
        editor.putString("book_id",bookNum[1]);
        editor.putString("chapter_id",bookNum[2]);
        editor.apply();
        //异步请求网页内容
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client=new OkHttpClient();
                    Request request=new Request.Builder()
                            .url(address)
                            .build();
                    Response response=client.newCall(request).execute();
                    byte[] responseDataBytes=response.body().bytes();
                    //parseJSONWithJSONObject(responseData);
                    String responseData = new String(responseDataBytes,"UTF-8");
                    Log.d("test","end");
                    showResponse(responseData);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
