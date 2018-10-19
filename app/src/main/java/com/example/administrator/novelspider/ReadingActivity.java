package com.example.administrator.novelspider;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.Adapter.BackgroundColorAdapter;
import com.example.administrator.novelspider.listener.ProcessListener;
import com.example.administrator.novelspider.po.BackgroundColor;
import com.example.administrator.novelspider.po.Content;
import com.example.administrator.novelspider.po.ReadingRecord;
import com.example.administrator.novelspider.service.SpiderService;
import com.example.administrator.novelspider.util.StatusBarCompat;
import com.example.administrator.novelspider.util.StringParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadingActivity extends AppCompatActivity implements View.OnClickListener{
    private String bookId;            //书号
    private String chapterId;        //章节号+
    private String bookName;  //书名
    private TextView contentText;    //文章内容
    private TextView chapterText;    //文章标题
    private Button lastChapter;      //上一章按钮
    private Button nextChapter;      //下一章按钮
    private String bookLibURL = "http://www.bkxs.net";    //书库主页网址
    private String lastChapterURL = null;    //上一章链接
    private String nextChapterURL = null;    //下一章链接
    private ScrollView contentScrollView = null;    //放置文章的滚动模块
    private List<BackgroundColor> colorList = new ArrayList<>();    //背景色列表
    private Button increaseTextSizeBtn;     //增加文本字体大小按钮
    private Button decreaseTextSizeBtn;     //减小文本字体大小按钮
    private static float textSize = 25;    //默认大小为30px
    private LinearLayout masterLayout;         //主界面的布局管理器
    private static String colorCode = "#C7EDCC";    //默认背景色
    //页面获取完成回调接口
    private ProcessListener processListener = new ProcessListener() {
        @Override
        public void success(Content content) {
            //释放AsyncTask
            spiderBinder.releaseTask();
            bookName = content.getBookName();
            lastChapterURL = content.getLastChapterLink();
            nextChapterURL = content.getNextChapterLink();
            showContent(content);
        }

        @Override
        public void fail(){
            Toast.makeText(ReadingActivity.this, "页面获取失败，请检查网络设置或书号、章节号是否有误", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    //创建服务以及服务连接
    private SpiderService.SpiderBinder spiderBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            spiderBinder = (SpiderService.SpiderBinder) service;
            Log.d("ReadingActivity", "spiderBinder is instantiated");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

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
        bookId = intent.getStringExtra("book_id");
        chapterId = intent.getStringExtra("chapter_id");
        String bookUrl = bookLibURL + "/bkxs/"+ bookId + "/" + chapterId + ".html";
        //创建爬虫,添加页面处理器以及url
        //createSpider(new NovelProcessor(processListener), url);
        //sendRequestWithOkHttp(bookLibURL + "/bkxs/"+ bookId + "/" + chapterId + ".html");
        //设置字体大小
        //创建后台爬虫服务
        Intent intentService = new Intent(this, SpiderService.class);
        startService(intentService);   //启动服务
        bindService(intentService, connection, BIND_AUTO_CREATE);    //绑定服务
        //为了防止服务绑定不成功，首次创建活动先使用子线程获取内容，待点击下一章或上一章再使用服务获取内容
        sendRequestWithJsoup(bookUrl);
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
                SharedPreferences.Editor editor = getSharedPreferences("readingSet",MODE_PRIVATE).edit();
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
                setBookAndChapterIdByURL(lastChapterURL);
                spiderBinder.startSpider(lastChapterURL, processListener);
                break;
            case R.id.nextChapter:
                setBookAndChapterIdByURL(nextChapterURL);
                spiderBinder.startSpider(nextChapterURL, processListener);
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

    @Override
    public void onPause(){
        //重写onPause()方法，在活动不可见时保存当前书目的阅读章节
        super.onPause();
        ReadingRecord record = new ReadingRecord();
        record.setBookNum(bookId);
        record.setChapterNum(chapterId);
        record.setName(bookName);
        record.save();
    }

    @Override
    public void onDestroy(){
        //解绑服务
        super.onDestroy();
        unbindService(connection);
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
        SharedPreferences.Editor editor = getSharedPreferences("readingSet",MODE_PRIVATE).edit();
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

    //设置上一章链接
    public void setLastChapterURL(String lastChapterURL){
        this.lastChapterURL = lastChapterURL;
    }

    //设置下一章链接
    public void setNextChapterURL(String nextChapterURL){
        this.nextChapterURL = nextChapterURL;
    }

    //设置书名
    public void setBookName(String bookName){
        this.bookName = bookName;
    }

    //通过url设置书号以及章节号
    public void setBookAndChapterIdByURL(String url){
        //进行分组.第5个为书号，第6个为章节号
        String[] s = url.split("/");
        bookId = s[4];
        chapterId = s[5].substring(0, s[5].length() - ".html".length());
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

    //显示小说文本内容
    public void showContent(Content content){
        //如果返回的是空串，则销毁该活动
        if(StringParser.isEmpty(content.getContent())||StringParser.isEmpty(content.getChapterName())){
            Toast.makeText(ReadingActivity.this,"书号或章节号输入错误，请重新输入",Toast.LENGTH_SHORT);
            Log.v("Destroy:","111");
            onDestroy();
        }else{
            chapterText.setText(content.getChapterName());
            contentText.setText(content.getContent());
        }
    }

    //为了避免后台服务绑定失败，首次创建活动使用子线程获取内容
    private void sendRequestWithJsoup(final String address){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Document page = Jsoup.connect(address).timeout(10000).get();
                    showContentWithJsoup(page.body());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showContentWithJsoup(final Element body){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //抽取小说名，小说章节标题、内容、上一章链接，下一章链接
                String novelName = body.getElementsByClass("con_top").get(0).child(2).text();
                String chapterTitle = body.getElementsByTag("h1").text();
                //使用first和last截取小说内容
                int first = "    天才一秒记住本站地址：[博看小说网]\nhttp://www.bkxs.net/最快更新！无广告！".length();
                int last = "章节错误,点此报送(免注册),\n报送后维护人员会在两分钟内校正章节内容,请耐心等待。".length();
                String novelContent = body.getElementById("content").text();
                novelContent = novelContent.replace(" ","\n");
                novelContent = novelContent.substring(first, novelContent.length() - last);
                lastChapterURL = "http://www.bkxs.net"+body.getElementsByClass("pre").get(0).attr("href");
                nextChapterURL = "http://www.bkxs.net"+body.getElementsByClass("next").get(0).attr("href");
                //保存当前阅读进度
                ReadingRecord book = new ReadingRecord();
                book.setName(novelName);
                book.setBookNum(bookId);
                book.setChapterNum(chapterId);
                book.save();
                //设置内容
                chapterText.setText(chapterTitle);
                contentText.setText(novelContent);
                //回滚到顶部
                backToTop();
            }
        });
    }
}
