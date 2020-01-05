package com.example.administrator.novelspider.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.administrator.novelspider.MainActivity;
import com.example.administrator.novelspider.R;
import com.example.administrator.novelspider.dao.DatabaseHandler;
import com.example.administrator.novelspider.dbhelper.BookDatabaseHelper;
import com.example.administrator.novelspider.po.Book;

import java.util.List;


/**
 * Created by Administrator on 2018/10/20 0020.
 */

public class BookAdapter extends BaseAdapter{
    private LayoutInflater inflater;
    private List<Book> books;
    private boolean isShowDelete = false;
    private Context mContext;
    private DatabaseHandler dbHandler;

    public BookAdapter(List<Book> books, Context context){
        super();
        this.books = books;
        inflater = LayoutInflater.from(context);
        mContext = context;
        BookDatabaseHelper helper = new BookDatabaseHelper(mContext, "BookStore.db", null, 2);
        dbHandler = new DatabaseHandler(helper);
    }

    public void setIsShowDelete(boolean isShowDelete){
        this.isShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        if(books == null){
            return 0;
        }else{
            return books.size();
        }
    }

    @Override
    public Object getItem(int position){
        return books.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){
        Book book = books.get(position);
        ViewHolder viewHolder;
        convertView = inflater.inflate(R.layout.book_item, parent,  false);
        viewHolder = new ViewHolder();
        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.book_image);
        viewHolder.textView = (TextView) convertView.findViewById(R.id.book_name);
        viewHolder.deleteImage = (ImageView) convertView.findViewById(R.id.delete_img);
        if(book.getBitmap() != null){
            viewHolder.imageView.setImageBitmap(book.getBitmap());
        }else{
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.no_cover);
            viewHolder.imageView.setImageBitmap(bitmap);
        }
        viewHolder.textView.setText(book.getName());
        viewHolder.deleteImage.setVisibility(isShowDelete?View.VISIBLE:View.GONE);
        viewHolder.deleteImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                AlertDialog deleteDialog = new AlertDialog.Builder(mContext)
                        .setTitle("删除书籍")
                        .setMessage("你确定要删除《"+books.get(position).getName()+"》吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHandler.deleteBook(books.get(position));
                                books.remove(position);
                                notifyDataSetChanged();
                                if(books.size() == 0){    //如果所有书籍都删除完了就自动取消删除状态
                                    MainActivity.setIsLongClick(false);
                                    MainActivity.setIsShowDelete(false);
                                    setIsShowDelete(false);
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                deleteDialog.show();
            }
        });
        return convertView;
    }

    class ViewHolder{
        ImageView imageView;
        TextView textView;
        ImageView deleteImage;
    }
}
