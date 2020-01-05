package com.example.administrator.novelspider.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.novelspider.MainActivity;
import com.example.administrator.novelspider.R;
import com.example.administrator.novelspider.dao.DatabaseHandler;
import com.example.administrator.novelspider.dbhelper.BookDatabaseHelper;
import com.example.administrator.novelspider.listener.AddBookListener;
import com.example.administrator.novelspider.listener.ProcessListener;
import com.example.administrator.novelspider.po.Book;
import com.example.administrator.novelspider.po.Content;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/6/23 0023.
 */

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.ViewHolder>{
    private List<Book> books;
    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView bookCover;   //书籍封面
        TextView bookNameText;     //书名
        TextView authorText;       //作者
        TextView introductionText;//简介
        Button addNovelBtn;        //添加书籍按钮

        public ViewHolder(View view){
            super(view);
            bookNameText = (TextView) view.findViewById(R.id.novel_name);
            bookCover = (ImageView) view.findViewById(R.id.novel_image_cover);
            authorText = (TextView) view.findViewById(R.id.novel_author);
            introductionText = (TextView) view.findViewById(R.id.novel_introduction);
            addNovelBtn = (Button) view.findViewById(R.id.add_novel);
        }
    }

    //获取家居信息列表
    public NovelAdapter(Context context, List<Book> list){
        this.context = context;
        books = list;
    }

    //更新列表
    public void update(List<Book> bookList){
        books = new ArrayList<>(bookList);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType){
        if(context != null){
            context = parent.getContext();
        }
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position){
        final Book book = books.get(position);
        //为了图片加载快一些，使用Glide加载图片
        if(book.getImageUrl() != null){
            Glide.with(context).load(book.getImageUrl()).error(R.drawable.no_cover).into(holder.bookCover);
        }

        //加载文字
        holder.bookNameText.setText(book.getName());
        holder.authorText.setText("作者：" + book.getAuthor());
        //如果简介大于50个字则显示省略号
        String introduction = book.getIntroduction();
        if(introduction.length() > 50){
            introduction = introduction.substring(0, 50) + "...";
        }
        holder.introductionText.setText("简介：" + introduction);

        //获取书籍信息，查看是否已添加
        final BookDatabaseHelper helper = new BookDatabaseHelper(context, "BookStore.db", null, 2);
        DatabaseHandler databaseHandler = new DatabaseHandler(helper);
        Book existBook = databaseHandler.getBookById(book.getBookNum());
        if(existBook != null){
            holder.addNovelBtn.setBackgroundColor(Color.parseColor("#595959"));
        }else {
            //添加点击事件
            holder.addNovelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //把第一章作为开始阅读的章节
                    try{
                        MainActivity.getBookToSave(book.getBookNum(), book.getChapters().get(0).getChapterId());
                    }catch (Exception e){
                        Toast.makeText(context, "添加失败，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                    //设置按钮颜色
                    addNovelListener.success(holder.addNovelBtn);
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount(){
        return books.size();
    }

    private AddBookListener addNovelListener = new AddBookListener() {
        @Override
        public void success(View view) {
            //设置按钮颜色
            view.setBackgroundColor(Color.parseColor("#595959"));
        }

        @Override
        public void fail(Exception e) {

        }
    };
}
