package com.baima.mediaplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class FileAdapter extends BaseAdapter {

    private Context context;
    private List<String> nameList;

    public FileAdapter(Context context, List<String> nameList) {
        this.context = context;
        this.nameList = nameList;
    }

    @Override
    public int getCount() {
        return nameList.size();
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

        String name = nameList.get(position);
        holder.tv_name.setText(name);
        return convertView;
    }

    class ViewHolder {
        TextView tv_name;
    }
}
