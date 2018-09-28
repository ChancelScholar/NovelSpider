package com.example.administrator.novelspider.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.administrator.novelspider.R;
import com.example.administrator.novelspider.po.BackgroundColor;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2018/9/26 0026.
 * 背景色选择ListView适配器
 */

public class BackgroundColorAdapter extends ArrayAdapter<BackgroundColor>{
    private int resourceId;

    public BackgroundColorAdapter(Context context,int textViewResourceId,List<BackgroundColor> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        BackgroundColor color = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.colorImage = (CircleImageView) view.findViewById(R.id.color_item);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();    //重新获取ViewHolder
        }
        viewHolder.colorImage.setImageResource(color.getImageId());
        return view;
    }

    class ViewHolder{
        CircleImageView colorImage;
    }
}
