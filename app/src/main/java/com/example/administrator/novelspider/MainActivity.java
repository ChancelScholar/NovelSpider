package com.example.administrator.novelspider;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.Adapter.BookAdapter;
import com.example.administrator.novelspider.dao.DatabaseHandler;
import com.example.administrator.novelspider.dao.ImageHandle;
import com.example.administrator.novelspider.dbhelper.BookDatabaseHelper;
import com.example.administrator.novelspider.listener.SearchListener;
import com.example.administrator.novelspider.po.Book;
import com.example.administrator.novelspider.util.StatusBarCompat;
import com.example.administrator.novelspider.util.StringParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity{

    private EditText bookIdText;    //书号
    private EditText chapterIdText; //章节号
    private TextView webLink;       //网页链接文本
    boolean isCreateDB = false;    //是否创建数据库标志
    private static List<Book> books;      //书目列表
    private GridView bookLib;       //书架
    private static Book book = new Book();     //用于存储添加的书籍信息
    private static BookAdapter bookAdapter;   //书架适配器
    private FloatingActionButton floatingButton;     //添加书籍按钮
    private static boolean isShowDelete = false;
    private BookDatabaseHelper dbHelper;  //数据库操作帮手
    private static boolean isLongClick = false;     //区分长按和点击
    private static ImageHandle imageHandle;     //图像文件处理类
    private static DatabaseHandler dbHandler;   //数据库处理类

    public static final int UPDATE_BOOKS = 1;
    //使用接口返回处理，避免编写不规范造成强连接，导致内存泄露，所以应该使用弱连接的写法
    private static Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_BOOKS:
                    bookAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageHandle = new ImageHandle();
        bookLib = (GridView) findViewById(R.id.gridview);
        dbHelper = new BookDatabaseHelper(this,"BookStore.db", null, 2);
        dbHandler = new DatabaseHandler(dbHelper);
        floatingButton = (FloatingActionButton) findViewById(R.id.add_book);
        ActionBar actionBar = getSupportActionBar();
        //隐藏标题栏
        if(actionBar!=null){
            actionBar.hide();
        }
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //若当前页面在删除状态，则退出删除状态
                if(isShowDelete){
                    bookAdapter.setIsShowDelete(false);
                    isShowDelete = false;
                }
                //跳转到搜索页
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        getGrants();
    }

    private void getGrants(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initBooks(){
        //获取所有书目
        books = dbHandler.getAllBooks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                }else{
                    Toast.makeText(MainActivity.this, "拒绝权限将无法使用该软件", Toast.LENGTH_SHORT).show();
                    getGrants();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // 获取用户自定义的阅读设置
        SharedPreferences preferences = getSharedPreferences("readingSet",MODE_PRIVATE);
        isCreateDB = preferences.getBoolean("isCreateDB",false);
        //创建数据库
        if(!isCreateDB){
            dbHelper.getWritableDatabase();
            //使用SharePreference存储一些设置信息
            SharedPreferences.Editor editor = getSharedPreferences("readingSet",MODE_PRIVATE).edit();
            editor.putBoolean("isCreateDB",true);
            editor.apply();
        }
        //为了及时更新书目，把初始化操作在onResume()执行
        initBooks();
        bookAdapter = new BookAdapter(books, this);
        bookLib.setAdapter(bookAdapter);
        //设置长按删除监听
        bookLib.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("长按。。。");
                isLongClick = true;
                bookAdapter.setIsShowDelete(true);
                isShowDelete = true;
                return false;
            }
        });
        //设置书籍点击事件
        bookLib.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isLongClick){
                    System.out.println("点击。。。");
                    Book book = books.get(position);
                    Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
                    intent.putExtra("book_id", book.getBookNum());
                    startActivity(intent);
                }
            }
        });
        //更改状态栏背景色
        StatusBarCompat.compat(MainActivity.this, Color.parseColor("#E2C8A7"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode ==  KeyEvent.KEYCODE_BACK){
            //判断是否正在进行删除书籍
            if(isShowDelete){
                bookAdapter.setIsShowDelete(false);
                isShowDelete = false;
                isLongClick = false;
            }else{
                //若不是则提示再次点击退出应用
                exitByDoubleClick();
            }
        }
        return  true;
    }

    public static void setIsLongClick(boolean isLongClick){
        MainActivity.isLongClick = isLongClick;
    }

    public static void setIsShowDelete(boolean isShowDelete){
        MainActivity.isShowDelete = isShowDelete;
    }

    private static boolean isPrepared = false;   //辅助判断是否准备退出

    //双击退出应用方法
    private void exitByDoubleClick(){
        Timer timer = null;
        if(!isPrepared){
            isPrepared = true;    //准备退出
            Toast.makeText(MainActivity.this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isPrepared = false;    //超过2秒需重新按下
                }
            }, 2000);
        }else{
            finish();
            System.exit(0);   //2秒内再次点击退出应用
        }
    }

    public static void getBookToSave(String bookId, String chapterId){
        book = new Book();
        book.setBookNum(bookId);
        book.setChapterNum(chapterId);
        book.setBookImage(bookId + "s.jpg");
        final String url = "http://www.bkxs.net/bkxs/" + bookId + "/";
        //开启子线程获取书名以及封面url
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Document doc = Jsoup.connect(url).timeout(100000).get();
                    String bookName = doc.getElementById("info").getAllElements().get(1).text();
                    String imageUrl = "http://www.bkxs.net" + doc.getElementById("fmimg").getAllElements().get(1).attr("src");
                    book.setName(bookName);
                    book.setImageUrl(imageUrl);
                    //保存图片
                    if(ImageHandle.SUCCESS == imageHandle.saveImageFile(book.getImageUrl())){
                        book.setBitmap(imageHandle.getImageBitmap(book.getBookImage()));
                        dbHandler.insertBook(book);
                        books.add(book);
                        //异步更新书架
                        Message message = new Message();
                        message.what = UPDATE_BOOKS;
                        handler.sendMessage(message);
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}