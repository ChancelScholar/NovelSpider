package com.example.administrator.novelspider.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.administrator.novelspider.R;
import com.example.administrator.novelspider.ReadingActivity;
import com.example.administrator.novelspider.po.BackgroundColor;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2018/9/26 0026.
 * 背景色选择ListView适配器
 */

public class BackgroundColorAdapter extends RecyclerView.Adapter<BackgroundColorAdapter.ViewHolder>{
    private List<BackgroundColor> backgroundColorList;
    private Context mContext;    //上下文

    static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageView;

        public ViewHolder(View view){
            super(view);
            circleImageView = (CircleImageView) view.findViewById(R.id.color_item);
        }
    }

    public BackgroundColorAdapter(Context context, List<BackgroundColor> backgroundColorList){
        this.backgroundColorList = backgroundColorList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        //设置背景色组件的点击事件
        viewHolder.circleImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int position = viewHolder.getAdapterPosition();
                BackgroundColor color = backgroundColorList.get(position);
                ReadingActivity readingActivity = (ReadingActivity) mContext;
                readingActivity.setBackgroundColor(color);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        BackgroundColor backgroundColor = backgroundColorList.get(position);
        holder.circleImageView.setImageResource(backgroundColor.getImageId());
    }

    @Override
    public int getItemCount(){
        return backgroundColorList.size();
    }
}
