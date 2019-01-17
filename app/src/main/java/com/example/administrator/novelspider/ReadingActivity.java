package com.example.administrator.novelspider;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.Adapter.BackgroundColorAdapter;
import com.example.administrator.novelspider.Adapter.ChapterListAdapter;
import com.example.administrator.novelspider.dao.DatabaseHandler;
import com.example.administrator.novelspider.dao.JsonHandler;
import com.example.administrator.novelspider.dbhelper.BookDatabaseHelper;
import com.example.administrator.novelspider.listener.ProcessListener;
import com.example.administrator.novelspider.po.BackgroundColor;
import com.example.administrator.novelspider.po.Chapter;
import com.example.administrator.novelspider.po.Content;
import com.example.administrator.novelspider.po.Book;
import com.example.administrator.novelspider.service.SpiderService;
import com.example.administrator.novelspider.util.StatusBarCompat;
import com.example.administrator.novelspider.util.StringParser;
import com.example.administrator.novelspider.util.URLParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReadingActivity extends AppCompatActivity implements View.OnClickListener{
    private String bookId;            //书号
    private String chapterId;        //章节号
    private String bookLibURL = "http://www.bkxs.net";    //书库主页网址
    private String lastChapterURL = null;    //上一章链接
    private String nextChapterURL = null;    //下一章链接

    private TextView bookNameText;   //书名组件
    private TextView contentText;    //文章内容
    private Button lastChapter;      //上一章按钮
    private Button nextChapter;      //下一章按钮
    private ScrollView contentScrollView = null;    //放置文章的滚动模块
    private List<BackgroundColor> colorList = new ArrayList<>();    //背景色列表

    private Button increaseTextSizeBtn;     //增加文本字体大小按钮
    private Button decreaseTextSizeBtn;     //减小文本字体大小按钮
    private Button defaultTextSizeBtn;      //设置默认字体大小按钮
    private float textSize = 20;    //默认大小为20px

    private LinearLayout masterLayout;         //主界面的布局管理器
    private String colorCode = "#C7EDCC";    //默认背景色

    private BookDatabaseHelper dbHelper;       //用于数据库操作
    private RecyclerView backgroundColorListView;         //背景色设置列表
    private BackgroundColorAdapter colorAdapter;       //背景色设置适配器
    private Map<String, Integer> chapterSeriesNum = new HashMap<>();     //章节号对应的排序序号，用于选中某一项
    private List<Chapter> chapterList = new ArrayList<>();                //章节列表
    private ListView chapterListView;                 //章节列表ListView
    private ChapterListAdapter chapterListAdapter;   //章节列表适配器
    private DrawerLayout chapterDrawerLayout;        //章节列表的DrawerLayout
    private RelativeLayout settingPanel;              //设置面板
    private boolean isFirstClick = true;            //监听是否是第一次点击,显示设置面板
    private Button catalogueButton;                  //目录按钮
    private Button cacheButton;                       //缓存开关
    private boolean cacheStatus = false;            //缓存状态

    private DatabaseHandler dbHandler;                //数据库处理类
    private JsonHandler jsonHandler;                  //Json数据处理类
    private static Map<String, Content> contents;      //章节存储器

    private boolean isFirstCreate = true;           //首次启动，判断是否需要加载章节列表
    private boolean hasChapterList = false;        //是否已有章节列表

    //页面获取完成回调接口
    private ProcessListener processListener = new ProcessListener() {
        @Override
        public void success(Content content) {
            chapterId = content.getChapterId();
            //章节列表定位到当前阅读的章节
            int selectedNum = chapterSeriesNum.get(chapterId);
            chapterListView.setSelection(selectedNum);
            chapterListAdapter.setSelectedNum(selectedNum);
            //释放AsyncTask
            spiderBinder.releaseTask();
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

    private static final int IOEXCEPTION = 1;     //异步处理网络异常
    private static final int GET_CHAPTER_LIST_SUCCESS = 0;            //获取章节列表成功
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case IOEXCEPTION:
                    Toast.makeText(ReadingActivity.this,"请检查网络设置或章节号有误，尝试删除书籍重新添加", Toast.LENGTH_SHORT).show();
                    //网络有误则显示本地列表
                    sendRequestToGetChapterList(bookLibURL + "/bkxs/" + bookId + "/");
                    break;
                case GET_CHAPTER_LIST_SUCCESS:
                    System.out.println("展示章节列表");
                    Chapter chapter = (Chapter) msg.obj;
                    chapterListAdapter.setSelectedNum(chapterSeriesNum.get(chapter.getId()));
                    chapterListView.setSelection(chapterSeriesNum.get(chapter.getId()));
                    chapterListAdapter.notifyDataSetChanged();
                    break;
                default:
                    Toast.makeText(ReadingActivity.this,"软件出现未知问题，请尽快联系软件制作人员",Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

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

    //添加章节
    public static void addContent(Content content){
        contents.put(content.getChapterId(), content);
    }

    //移除章节
    public static void removeContent(Content content){
        contents.remove(content.getChapterId());
    }

    //判断章节是否已保存过
    public static boolean contentIsExist(String chapterId){
        boolean result = false;
        result = contents.containsKey(chapterId);
        return result;
    }

    //根据章节号获取content对象
    public static Content getContent(String chapterId){
        return contents.get(chapterId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
        init();
    }

    //初始化组件
    private void init(){
        //初始化文件操作类
        dbHandler = new DatabaseHandler();
        jsonHandler = new JsonHandler();
        //初始化章节存储器
        contents = new HashMap<>();
        //初始化各组件
        bookNameText = (TextView) findViewById(R.id.book_name);
        contentText = (TextView) findViewById(R.id.content);
        lastChapter = (Button) findViewById(R.id.lastChapter);
        nextChapter = (Button) findViewById(R.id.nextChapter);
        contentScrollView = (ScrollView) findViewById(R.id.contentScrollView);
        increaseTextSizeBtn = (Button) findViewById(R.id.increase_text_size);
        decreaseTextSizeBtn = (Button) findViewById(R.id.decrease_text_size);
        defaultTextSizeBtn = (Button) findViewById(R.id.default_size);
        masterLayout = (LinearLayout) findViewById(R.id.master_layout);
        dbHelper = new BookDatabaseHelper(this, "BookStore.db", null, 2);
        Intent intent = getIntent();
        bookId = intent.getStringExtra("book_id");
        //避免软件被系统的伪后台杀死，重新获取intent中的章节号，导致章节号没有及时更新，故直接从数据库中获取章节号
        Book book = dbHandler.getBookById(dbHelper, bookId);
        //由于是使用chapterId来记录当前阅读章节，为了避免未点击下一章而退出活动导致chapterId为null，并更新了数据库内容，故需获取chapterId
        chapterId = book.getChapterNum();
        String bookUrl = bookLibURL + "/bkxs/"+ bookId + "/" + chapterId + ".html";
        //为了防止服务绑定不成功，首次创建活动先获取本地章节列表，若本地章节列表没有这一章就开启子线程获取
        getContents();
        sendRequestWithJsoup(bookUrl);
        //创建后台爬虫服务
        Intent intentService = new Intent(this, SpiderService.class);
        startService(intentService);   //启动服务
        bindService(intentService, connection, BIND_AUTO_CREATE);    //绑定服务
        // 获取用户设置，默认字体大小为20px
        SharedPreferences preferences = getSharedPreferences("readingSet", MODE_PRIVATE);
        textSize = preferences.getFloat("textSize", 20);
        colorCode = preferences.getString("backgroundColor","#C7EDCC");
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
        //设置字体大小设置监听器
        increaseTextSizeBtn.setOnClickListener(this);
        decreaseTextSizeBtn.setOnClickListener(this);
        defaultTextSizeBtn.setOnClickListener(this);
        ActionBar actionBar = getSupportActionBar();
        //隐藏标题栏
        if(actionBar!=null){
            actionBar.hide();
        }
        //初始化颜色列表
        initColors();
        //获取背景色列表适配器，设置横向排布
        colorAdapter = new BackgroundColorAdapter(this, colorList);
        backgroundColorListView = (RecyclerView) findViewById(R.id.background_color_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        backgroundColorListView.setLayoutManager(layoutManager);
        backgroundColorListView.setAdapter(colorAdapter);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
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
        //点击内容组件弹出设置页面
        contentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFirstClick){     //第一次点击显示出设置面板
                    settingPanel.setVisibility(View.VISIBLE);
                    //隐藏上一章和下一章按钮
                    lastChapter.setVisibility(View.INVISIBLE);
                    nextChapter.setVisibility(View.INVISIBLE);
                    isFirstClick = false;
                }else{
                    settingPanel.setVisibility(View.INVISIBLE);
                    //重新显示上一章下一章按钮
                    lastChapter.setVisibility(View.VISIBLE);
                    nextChapter.setVisibility(View.VISIBLE);
                    isFirstClick = true;
                }
            }
        });
        //初始化章节列表的DrawerLayout
        chapterDrawerLayout = (DrawerLayout) findViewById(R.id.chapter_draw_layout);
        //初始化章节列表组件
        chapterListView = (ListView) findViewById(R.id.chapter_list);
        chapterListAdapter = new ChapterListAdapter(this, R.layout.chapter_list_item, chapterList);
        chapterListView.setAdapter(chapterListAdapter);
        //设置章节列表的点击监听器
        chapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Chapter chapter = chapterList.get(position);
                chapterListAdapter.setSelectedNum(chapterSeriesNum.get(chapter.getId()));
                chapterListAdapter.notifyDataSetChanged();
                //关闭列表
                chapterDrawerLayout.closeDrawer(GravityCompat.START);
                //更新章节号
                chapterId = chapter.getId();
                //暂停爬虫，避免访问冲突
                spiderBinder.pause();
                //发送章节请求
                sendRequestWithJsoup(chapter.getUrl());
            }
        });
        //初始化设置面板
        settingPanel = (RelativeLayout) findViewById(R.id.setting);
        settingPanel.setVisibility(View.INVISIBLE);
        //初始化章节目录按钮
        catalogueButton = (Button) findViewById(R.id.catalogue_button);
        catalogueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭设置面板，打开章节列表
                settingPanel.setVisibility(View.INVISIBLE);
                lastChapter.setVisibility(View.VISIBLE);
                nextChapter.setVisibility(View.VISIBLE);
                isFirstClick = true;
                chapterDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //初始化缓存开关按钮
        cacheButton = (Button) findViewById(R.id.cache_button);
        cacheStatus = preferences.getBoolean("cacheStatus", false);    //获取用户的缓存设置
        if(cacheStatus){
            cacheButton.setText("自动缓存：关");
        }
        cacheButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("readingSet",MODE_PRIVATE).edit();
                if(SpiderService.START == spiderBinder.getCacheStatus()){
                    spiderBinder.setIsStart(false);
                    cacheStatus = false;
                    cacheButton.setText("自动缓存：开");
                    editor.putBoolean("cacheStatus", false);
                }else if (SpiderService.STOP == spiderBinder.getCacheStatus()){
                    spiderBinder.setIsStart(true);
                    cacheStatus = true;
                    spiderBinder.startSpider(bookLibURL + "/bkxs/" + bookId + "/" + chapterId + ".html");
                    cacheButton.setText("自动缓存：关");
                    editor.putBoolean("cacheStatus", true);
                }
                editor.apply();
            }
        });
    }

    public void setBackgroundColor(BackgroundColor color){
        colorCode = color.getCode();
        //若背景色为黑色，则设字体颜色设置为深灰色
        if(colorCode.equalsIgnoreCase("#0D0D0D")){
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

    //获取章节数据列表
    private void getContents(){
        try {
            if((contents = jsonHandler.getChapters(bookId))==null){
                contents = new HashMap<>();
            }
        }catch(IOException e){
            Log.d("GetChapters:", "获取章节数据失败");
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.lastChapter:
                if(StringParser.isEmpty(lastChapterURL)){
                    Toast.makeText(ReadingActivity.this, "请检查您的网络连接", Toast.LENGTH_SHORT).show();
                }else if(lastChapterURL.split("/").length == 6){     //是否有上一章
                    String url = lastChapterURL;   //保存上一章的链接，避免获取数据后改变了上一章的链接
                    sendRequestWithJsoup(lastChapterURL);
                }else{
                    Toast.makeText(ReadingActivity.this, "这是第一章", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.nextChapter:
                if(StringParser.isEmpty(nextChapterURL)){
                    Toast.makeText(ReadingActivity.this, "请检查您的网络连接", Toast.LENGTH_SHORT).show();
                }else if(nextChapterURL.split("/").length == 6){    //是否还有下一章
                    sendRequestWithJsoup(nextChapterURL);
                }else{
                    Toast.makeText(ReadingActivity.this, "已是最后一章", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.increase_text_size:
                changeTextSize(R.id.increase_text_size);
                break;
            case R.id.decrease_text_size:
                changeTextSize(R.id.decrease_text_size);
                break;
            case R.id.default_size:
                //设置默认字体大小
                contentText.setTextSize(20);
            default:
                Toast.makeText(ReadingActivity.this,"软件出现未知问题，请尽快联系软件制作人员",Toast.LENGTH_SHORT);
                break;
        }
    }

    @Override
    public void onPause(){
        //重写onPause()方法，在活动不可见时保存当前书目的阅读章节
        super.onPause();
        Book book = new Book();
        book.setBookNum(bookId);
        book.setChapterNum(chapterId);
        dbHandler.updateBook(dbHelper, book);
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
        //保存书号章节号
        if(content.getBookId()!=null && content.getChapterId()!=null){
            bookId = content.getBookId();
            chapterId = content.getChapterId();
        }

        //已获得章节列表后由显示内容时定位到对应的位置，第一次定位由获取章节列表时完成
        if(chapterSeriesNum.size() > 0){
            //章节列表定位到当前阅读的章节
            int selectedNum = chapterSeriesNum.get(chapterId);
            chapterListView.setSelection(selectedNum);
            chapterListAdapter.setSelectedNum(selectedNum);
        }
        //如果返回的是空串，则提示
        if(StringParser.isEmpty(content.getContent())||StringParser.isEmpty(content.getChapterName())){
            Toast.makeText(ReadingActivity.this,"书号或章节号输入错误，请重新输入",Toast.LENGTH_SHORT);
            Log.v("Destroy:","111");
        }else{
            bookNameText.setText(content.getBookName());
            contentText.setText("\n" + content.getChapterName() +"\n"+ content.getContent());
        }
    }

    //为了避免后台服务绑定失败，使用子线程获取内容
    private void sendRequestWithJsoup(final String address){
        //本地列表不存在时才发起请求
        if(!contentIsExist(URLParser.getChapterId(address))){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Document page = Jsoup.connect(address).timeout(10000).get();
                        showContentWithJsoup(page.body());
                    }catch (IOException e){
                        Message message = new Message();
                        message.what = IOEXCEPTION;
                        handler.sendMessage(message);
                    }
                    if(cacheStatus){
                        spiderBinder.setIsStart(true);
                        spiderBinder.startSpider(address);
                    }
                }
            }).start();
        }else{   //存在则直接显示
            Content content = contents.get(URLParser.getChapterId(address));
            showContent(content);
            lastChapterURL = content.getLastChapterLink();
            nextChapterURL = content.getNextChapterLink();
            if(isFirstCreate){
                sendRequestToGetChapterList(bookLibURL + "/bkxs/" + bookId + "/");
                if(cacheStatus){
                    spiderBinder.setIsStart(true);
                    spiderBinder.startSpider(address);
                }
                isFirstCreate = false;
            }
        }
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
                Book book = new Book();
                book.setName(novelName);
                book.setBookNum(bookId);
                book.setChapterNum(chapterId);
                dbHandler.updateBook(dbHelper, book);
                //获取书名并展示
                bookNameText.setText(novelName);
                //设置内容
                contentText.setText("\n" + chapterTitle + "\n" + novelContent);
                //回滚到顶部
                backToTop();
                //获取完章节后，若没有章节列表则进行获取章节列表
                if(!hasChapterList) {
                    sendRequestToGetChapterList(bookLibURL + "/bkxs/" + bookId + "/");
                    hasChapterList = true;
                }
            }
        });
    }

    private void sendRequestToGetChapterList(final String address){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Document page = Jsoup.connect(address).timeout(10000).get();
                    showChapterList(page.body());
                }catch (IOException e){
                    //网络异常时显示本地列表
                    try {
                        chapterList = jsonHandler.getChapterList(bookId + "/" + bookId + ".json");
                        chapterSeriesNum = jsonHandler.getChapterSeriesNum(bookId + "/" + bookId + "Series.json");
                        //发送消息异步显示章节列表
                        Chapter chapter = new Chapter();
                        chapter.setId(chapterId);
                        chapter.setUrl(bookLibURL + "/bkxs/" + bookId + "/" + chapterId + ".html");
                        Message message = new Message();
                        message.what = GET_CHAPTER_LIST_SUCCESS;
                        message.obj = chapter;
                        Looper.prepare();
                        handler.sendMessage(message);
                        Looper.loop();
                    }catch (IOException ei){
                        //未存储有列表文件，报错
                        Toast.makeText(ReadingActivity.this, "网络异常，请检查网络配置", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).start();
    }

    private void showChapterList(final Element body){
        Elements chapterListDoc = body.getElementById("list").getAllElements().get(1).getAllElements().get(0).children();
        int dtCount = 0;
        List<Element> redundantChapters = new ArrayList<Element>();
        for(Element chapter:chapterListDoc){
            if(chapter.tagName().equalsIgnoreCase("dt")){
                dtCount++;
                redundantChapters.add(chapter);
                if(dtCount == 2){
                    break;
                }
            }else{
                redundantChapters.add(chapter);
            }
        }
        Iterator<Element> it = redundantChapters.iterator();
        while(it.hasNext()){
            Element redundantChapter = (Element)it.next();
            chapterListDoc.remove(redundantChapter);
        }
        int num = 0;
        for(Element chapterDoc:chapterListDoc){
            System.out.println("链接："+chapterDoc.child(0).attr("href")+" 章节名："+chapterDoc.text());
            Chapter chapter = new Chapter();
            chapter.setName(chapterDoc.text());
            chapter.setUrl(bookLibURL + chapterDoc.child(0).attr("href"));
            String[] s = chapter.getUrl().split("/");
            String chapterId = s[5].substring(0, s[5].length() - ".html".length());
            chapter.setId(chapterId);
            chapterSeriesNum.put(chapterId, num);
            chapterList.add(chapter);
            num++;
        }
        //处理完成后存储文档
        try {
            jsonHandler.saveChapterList(chapterList, bookId + "/" + bookId + ".json");
            jsonHandler.saveChapterSeriesNum(chapterSeriesNum, bookId + "/" + bookId + "Series.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //发送消息异步显示章节列表
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setUrl(bookLibURL + "/bkxs/" + bookId + "/" + chapterId + ".html");
        Message message = new Message();
        message.what = GET_CHAPTER_LIST_SUCCESS;
        message.obj = chapter;
        handler.sendMessage(message);
        hasChapterList = true;
    }
}
