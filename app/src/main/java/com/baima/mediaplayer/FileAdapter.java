package com.baima.mediaplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FileAdapter extends BaseAdapter {

    private Context context;
    private List<String> pathList;

    public FileAdapter(Context context, List<String> pathList) {
        this.context = context;
        this.pathList = pathList;
    }

    @Override
    public int getCount() {
        return pathList.size();
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
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = new TextView(context);
            convertView.setMinimumHeight(80);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String path = pathList.get(position);
        File file = new File(path);
        holder.tv_name.setText(file.getName());
        return convertView;
    }

    class ViewHolder {
        TextView tv_name;
    }
}
