package com.example.administrator.novelspider.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.administrator.novelspider.R;
import com.example.administrator.novelspider.po.Chapter;

import java.util.List;

/**
 * Created by Administrator on 2018/11/7 0007.
 */

public class ChapterListAdapter extends ArrayAdapter<Chapter>{
    private int resourceId;
    private int selectedNum = -1;

    public ChapterListAdapter(Context context, int textViewResourceId, List<Chapter> object){
        super(context, textViewResourceId, object);
        resourceId = textViewResourceId;
    }

    public void setSelectedNum(int selectedNum){
        this.selectedNum = selectedNum;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Chapter chapter = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.chapterNameText = (TextView) view.findViewById(R.id.chapter_item_name);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.chapterNameText.setText(chapter.getName());
        //如果有已选中的项则更改背景色
        if(position == selectedNum){
            viewHolder.chapterNameText.setBackgroundColor(Color.parseColor("#d6d6d6"));
        }else{
            //没有选中则默认为白色背景
            viewHolder.chapterNameText.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        return view;
    }

    class ViewHolder{
        TextView chapterNameText;
    }
}
