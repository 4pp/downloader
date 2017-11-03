package com.zsp.filedownloader.demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zsp.filedownloader.Const;
import com.zsp.filedownloader.Task;
import com.zsp.filedownloader.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zsp on 2017/10/31.
 */

public class DownLoadAdapter extends BaseAdapter {

    private static final String TAG = "DownLoadAdapter";

    Context ctx;
    Listener listener;
    List<Task> dataSource;
    boolean isTouching;

    public DownLoadAdapter(Context context) {
        dataSource = new LinkedList<>();
        ctx = context;
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public void add(Task task) {
        dataSource.add(task);
        notifyDataSetChanged();

    }

    public void setDataSource(List<Task> list){
        dataSource = list;
        notifyDataSetChanged();
    }

    public void remove(Task task) {
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
        CancelClick cancelClick;

        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(ctx);
            convertView = mInflater.inflate(R.layout.list_item_task,
                    parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.state = (TextView) convertView.findViewById(R.id.state);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress);

            holder.btnCancel = (Button) convertView.findViewById(R.id.btn_cancel);
            cancelClick = new CancelClick();
            holder.btnCancel.setOnClickListener(cancelClick);
            convertView.setTag(holder.btnCancel.getId(),cancelClick);

            convertView.setTag(holder);

            holder.btnCancel.setOnTouchListener(new ItemTouch());

        } else {
            holder = (ViewHolder) convertView.getTag();
            cancelClick = (CancelClick) convertView.getTag(holder.btnCancel.getId());
        }

        cancelClick.setPosition(position);
        cancelClick.setListener(listener);


        Task task = dataSource.get(position);
        holder.name.setText(task.getDownloadUrl());
        if (task.getContentLength() > 0) {
            int progress = (int) (task.getFinishedLength() * 1.0f / task.getContentLength() * 100);
            holder.progressBar.setProgress(progress);
        }else{
            holder.progressBar.setProgress(0);
        }

        if (task.getState() == Const.DOWNLOAD_STATE_WAIT){
            holder.state.setText("等待");
        }else if(task.getState() == Const.DOWNLOAD_STATE_STOP){
            holder.state.setText("已停止");
        }else if(task.getState() == Const.DOWNLOAD_STATE_CONNECT){
            holder.state.setText("连接中");
        }else if(task.getState() == Const.DOWNLOAD_STATE_DOWNLOADING){
            holder.state.setText("已下载"+task.getFinishedLength() + "/" + task.getContentLength());
        }else if(task.getState() == Const.DOWNLOAD_STATE_ERROR){
            holder.state.setText("失败");
        }else if(task.getState() == Const.DOWNLOAD_STATE_FINISH){
            holder.state.setText("完成");
        }

        return convertView;
    }

    private static class ViewHolder {
        ProgressBar progressBar;
        TextView name;
        TextView state;
        Button btnCancel;
    }

    public class ItemTouch implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.e("test","ACTION_DOWN");
                    isTouching = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    Log.e("test","ACTION_MOVE");

                    break;

                case MotionEvent.ACTION_UP:
                    Log.e("test","ACTION_UP");
                    isTouching = false;
                    break;

                case MotionEvent.ACTION_CANCEL:
                    Log.e("test","ACTION_CANCEL");
                    isTouching = false;
                    break;

            }
            return false;
        }
    }

    public static class BaseItemClick implements View.OnClickListener{

        public int position;
        public Listener listener;

        public void setPosition(int position) {
            this.position = position;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {}
    }

    public static class CancelClick extends BaseItemClick{
        @Override
        public void onClick(View v) {
            if (listener!=null){
                listener.onCancel(v,position);
            }
        }
    }

    interface Listener {
        void onCancel(View view, int position);
    }
}
