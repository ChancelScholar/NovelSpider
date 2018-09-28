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
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.Adapter.BackgroundColorAdapter;
import com.example.administrator.novelspider.po.BackgroundColor;
import com.example.administrator.novelspider.util.StatusBarCompat;
import com.example.administrator.novelspider.util.StringParser;

import java.util.ArrayList;
import java.util.List;
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
    private List<BackgroundColor> colorList = new ArrayList<>();    //背景色列表
    Button increaseTextSizeBtn;     //增加文本字体大小按钮
    Button decreaseTextSizeBtn;     //减小文本字体大小按钮
    private static float textSize = 25;    //默认大小为30px
    LinearLayout masterLayout;         //主界面的布局管理器
    private static String colorCode = "#C7EDCC";    //默认背景色

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        contentText = (TextView) findViewById(R.id.content);
        chapterText = (TextView) findViewById(R.id.chapter);
        lastChapter = (Button) findViewById(R.id.lastChapter);
        nextChapter = (Button) findViewById(R.id.nextChapter);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        increaseTextSizeBtn = (Button) findViewById(R.id.increase_text_size);
        decreaseTextSizeBtn = (Button) findViewById(R.id.decrease_text_size);
        masterLayout = (LinearLayout) findViewById(R.id.master_layout);
        Intent intent = getIntent();
        String bookId = intent.getStringExtra("book_id");
        String chapterId = intent.getStringExtra("chapter_id");
        sendRequestWithOkHttp(bookLibURL + "/bkxs/"+ bookId + "/" + chapterId + ".html");
        //设置字体大小
        contentText.setTextSize(textSize);
        //设置背景色
        masterLayout.setBackgroundColor(Color.parseColor(colorCode));
        lastChapter.setBackgroundColor(Color.parseColor(colorCode));
        nextChapter.setBackgroundColor(Color.parseColor(colorCode));
        //若背景色为黑色，则设字体颜色设置为深灰色
        if(colorCode.equalsIgnoreCase("0D0D0D")){
            contentText.setTextColor(Color.parseColor("#3a3a3a"));
        }
        //设置状态栏颜色
        StatusBarCompat.compat(ReadingActivity.this,Color.parseColor(colorCode));
        lastChapter.setOnClickListener(this);
        nextChapter.setOnClickListener(this);
        increaseTextSizeBtn.setOnClickListener(this);
        decreaseTextSizeBtn.setOnClickListener(this);
        ActionBar actionBar = getSupportActionBar();
        //隐藏标题栏
        if(actionBar!=null){
            actionBar.hide();
        }
        //初始化颜色列表
        initColors();
        //获取背景色列表适配器
        BackgroundColorAdapter adapter = new BackgroundColorAdapter(ReadingActivity.this,R.layout.color_item,colorList);
        ListView listView = (ListView) findViewById(R.id.background_color_list);
        listView.setAdapter(adapter);
        //设置背景色列表点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BackgroundColor color = colorList.get(position);
                //获取背景色的十六进制代码
                colorCode = color.getCode();
                //若背景色为黑色，则设字体颜色设置为深灰色
                if(colorCode.equalsIgnoreCase("#0D0D0D")){
                    chapterText.setTextColor(Color.parseColor("#3a3a3a"));
                    contentText.setTextColor(Color.parseColor("#3a3a3a"));
                    lastChapter.setTextColor(Color.parseColor("#3a3a3a"));
                    nextChapter.setTextColor(Color.parseColor("#3a3a3a"));
                }
                masterLayout.setBackgroundColor(Color.parseColor(colorCode));
                lastChapter.setBackgroundColor(Color.parseColor(colorCode));
                nextChapter.setBackgroundColor(Color.parseColor(colorCode));
                //设置状态栏颜色
                StatusBarCompat.compat(ReadingActivity.this,Color.parseColor(colorCode));
                //存储阅读背景色设置
                SharedPreferences.Editor editor = getSharedPreferences("novel_data",MODE_PRIVATE).edit();
                editor.putString("backgroundColor",colorCode);
                editor.apply();
            }
        });
        //设置自由复制监听器
        contentText.setTextIsSelectable(true);
        contentText.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setText(contentText.getText());
                return false;
            }
        });
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
            case R.id.increase_text_size:
                changeTextSize(R.id.increase_text_size);
                break;
            case R.id.decrease_text_size:
                changeTextSize(R.id.decrease_text_size);
                break;
            default:
                Toast.makeText(ReadingActivity.this,"软件出现未知问题，请尽快联系软件制作人员",Toast.LENGTH_SHORT);
                break;
        }
    }

    //修改字体大小,每次增大或减少2px,type:修改类型，增大或减小
    private void changeTextSize(int type){
        if(type == R.id.increase_text_size){
            textSize += 2;

        }else if(type == R.id.decrease_text_size){
            textSize -= 2;
        }
        contentText.setTextSize(textSize);
        //存储字体大小设置
        SharedPreferences.Editor editor = getSharedPreferences("novel_data",MODE_PRIVATE).edit();
        editor.putFloat("textSize",textSize);
        editor.apply();
    }

    //设置字体大小
    public static void setTextSize(float size){
        textSize = size;
    }

    //设置背景色
    public static void setBackgroundColor(String code){
        colorCode = code;
    }

    //获取字体大小
    public static float getTextSize(){
        return textSize;
    }

    //获取背景色十六进制代码
    public static String getBackgroundColorCode(){
        return colorCode;
    }

    //初始化背景色选择列表
    private void initColors(){
        BackgroundColor color0D0D0D = new BackgroundColor("#0D0D0D", R.drawable.black);
        BackgroundColor colorC6D6E3 = new BackgroundColor("#C6D6E3", R.drawable.c6d6e3);
        BackgroundColor colorD2CBC3 = new BackgroundColor("#D2CBC3", R.drawable.d2cbc3);
        BackgroundColor colorD2DBC6 = new BackgroundColor("#D2DBC6", R.drawable.d2dbc6);
        BackgroundColor colorDEDEDE = new BackgroundColor("#DEDEDE", R.drawable.dedede);
        BackgroundColor colorE2C8A7 = new BackgroundColor("#E2C8A7", R.drawable.e2c8a7);
        BackgroundColor colorEDE4DD = new BackgroundColor("#EDE4DD", R.drawable.ede4dd);
        BackgroundColor colorEFDEC4 = new BackgroundColor("#EFDEC4", R.drawable.efdec4);
        BackgroundColor colorF0DEDA = new BackgroundColor("#F0DEDA", R.drawable.f0deda);
        colorList.add(colorC6D6E3);
        colorList.add(colorE2C8A7);
        colorList.add(colorD2CBC3);
        colorList.add(colorDEDEDE);
        colorList.add(colorEDE4DD);
        colorList.add(color0D0D0D);
        colorList.add(colorD2DBC6);
        colorList.add(colorEFDEC4);
        colorList.add(colorF0DEDA);
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
