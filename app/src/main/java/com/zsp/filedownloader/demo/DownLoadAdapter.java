package com.zsp.filedownloader.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zsp.filedownloader.DownLoadTask;
import com.zsp.filedownloader.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsp on 2017/10/31.
 */

public class DownLoadAdapter extends BaseAdapter {

    List<DownLoadTask> dataSource;
    Context ctx;

    public DownLoadAdapter(Context context){
        dataSource = new ArrayList();
        ctx = context;

    }

    public void add(DownLoadTask task){
        dataSource.add(task);
        notifyDataSetChanged();
    }

    public void remove(DownLoadTask task){
        dataSource.remove(task);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return dataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(ctx);
            convertView = mInflater.inflate(R.layout.list_item_task,
                    parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.state = (TextView) convertView.findViewById(R.id.state);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        DownLoadTask task = dataSource.get(position);
        holder.name.setText(task.downloadUrl);
        if (task.contentLength > 0){
            int progress = (int)(task.finishedLength * 1.0f / task.contentLength * 100);
            holder.progressBar.setProgress(progress);
        }
        holder.state.setText(task.finishedLength+"/"+task.contentLength);
        return convertView;
    }

    private static class ViewHolder
    {
        ProgressBar progressBar;
        TextView name;
        TextView state;
    }

}
