package com.example.administrator.novelspider;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.novelspider.Adapter.NovelAdapter;
import com.example.administrator.novelspider.listener.SearchListener;
import com.example.administrator.novelspider.po.Book;
import com.example.administrator.novelspider.service.SpiderNovelService;
import com.example.administrator.novelspider.util.StatusBarCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText searchText;      //搜索内容文本框
    private Button searchBtn;         //搜索按钮
    private RecyclerView searchContentView;    //搜索内容显示滚动区域
    private NovelAdapter adapter;      //搜索内容显示适配器
    private List<Book> books;          //显示的内容
    private ProgressDialog progressDialog;    //显示等待对话框

    private SpiderNovelService novelService;   //搜索小说服务

    private SearchListener searchListener = new SearchListener() {
        @Override
        public void success(final List<Book> bookList) {
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            //返回主线程
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(bookList != null && bookList.size() > 0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showBooks(bookList);
                            }
                        });
                    }else{
                        Toast.makeText(SearchActivity.this, "暂无匹配内容", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void fail(Exception e) {
            Toast.makeText(SearchActivity.this, "网络连接失败，请检查网络设置，稍后重试", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //隐藏标题栏
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }

        StatusBarCompat.compat(this, Color.parseColor("#E2C8A7"));

        //初始化组件
        searchText = (EditText) findViewById(R.id.search_text);
        searchBtn = (Button) findViewById(R.id.search_btn);
        searchContentView = (RecyclerView) findViewById(R.id.search_result);
        books = new ArrayList<>();
        adapter = new NovelAdapter(this, books);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        //配置RecyclerView
        searchContentView.setLayoutManager(manager);
        searchContentView.setAdapter(adapter);

        //初始化服务
        novelService = new SpiderNovelService(searchListener);

        //初始化对话框
        initProgressDialog();

        //设置监听事件
        addListener();
    }

    private void addListener(){
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取搜索框内容
                final String search = searchText.getText().toString();
                if(search == null || search.length() <= 0){
                    Toast.makeText(SearchActivity.this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                //开启子线程进行搜索
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        novelService.searchNovel(search);
                    }
                }).start();
            }
        });
    }

    public void showBooks(List<Book> bookList){
        books.clear();
        //添加数据
        books.addAll(bookList);
        //通知数据刷新
        adapter.notifyDataSetChanged();
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(SearchActivity.this);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("请稍等...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }

    @Override
    public void onBackPressed(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
            //停止爬取
            novelService.setCancel(true);
        }
        super.onBackPressed();
    }
}
