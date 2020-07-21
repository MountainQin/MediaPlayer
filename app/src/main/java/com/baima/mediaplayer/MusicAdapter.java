package com.baima.mediaplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baima.mediaplayer.entities.Music;

import java.io.File;
import java.util.List;

public class MusicAdapter extends BaseAdapter {

    private Context context;
    private List<Music> musicList;

    public MusicAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if (convertView==null){
            convertView =LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
            holder=new ViewHolder();
            holder.tv_name=convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }

        Music music = musicList.get(position);
        String name = new File(music.getPath()).getName();
        if (name.contains(".")){
            name=name.substring(0, name.lastIndexOf("."));
        }
        holder.tv_name.setText(name);
        return  convertView;
    }

class ViewHolder{
        TextView tv_name;
}
}
