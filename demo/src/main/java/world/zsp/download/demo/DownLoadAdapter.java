package world.zsp.download.demo;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import world.zsp.download.library.DownLoadState;
import world.zsp.download.library.Task;

/**
 * Created by zsp on 2017/10/31.
 */

public class DownLoadAdapter extends BaseAdapter{

    private static final String TAG = "DownLoadAdapter";

    SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss MM/dd", Locale.CHINESE);
    Context ctx;
    Listener listener;
    List<Task> dataSource;
    public boolean isTouching;

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

    public void insert(Task task) {
        dataSource.add(0,task);
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
        StopClick stopClick;
        ReStartClick restartClick;
        OpenClick openClick;

        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(ctx);
            convertView = mInflater.inflate(R.layout.list_item_task,
                    parent, false);
            holder = new ViewHolder();
            holder.nameView = (TextView) convertView.findViewById(R.id.name);
            holder.stateView = (TextView) convertView.findViewById(R.id.state);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress);
            holder.createAtView = (TextView) convertView.findViewById(R.id.createat);

            holder.btnCancel = (Button) convertView.findViewById(R.id.btn_cancel);
            cancelClick = new CancelClick();
            holder.btnCancel.setOnClickListener(cancelClick);
            holder.btnCancel.setOnTouchListener(new ItemTouch());
            convertView.setTag(holder.btnCancel.getId(),cancelClick);


            holder.btnStop = (Button)convertView.findViewById(R.id.btn_stop);
            stopClick = new StopClick();
            holder.btnStop.setOnClickListener(stopClick);
            holder.btnStop.setOnClickListener(stopClick);
            holder.btnStop.setOnTouchListener(new ItemTouch());
            convertView.setTag(holder.btnStop.getId(),stopClick);

            holder.btnReStart = (Button)convertView.findViewById(R.id.btn_restart);
            restartClick = new ReStartClick();
            holder.btnReStart.setOnClickListener(restartClick);
            holder.btnReStart.setOnClickListener(restartClick);
            holder.btnReStart.setOnTouchListener(new ItemTouch());
            convertView.setTag(holder.btnReStart.getId(),restartClick);

            holder.btnOpen = (Button)convertView.findViewById(R.id.btn_open);
            openClick = new OpenClick();
            holder.btnOpen.setOnClickListener(openClick);
            holder.btnOpen.setOnClickListener(openClick);
            holder.btnOpen.setOnTouchListener(new ItemTouch());
            convertView.setTag(holder.btnOpen.getId(),openClick);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            cancelClick = (CancelClick) convertView.getTag(holder.btnCancel.getId());
            stopClick = (StopClick) convertView.getTag(holder.btnStop.getId());
            restartClick = (ReStartClick)convertView.getTag(holder.btnReStart.getId());
            openClick = (OpenClick) convertView.getTag(holder.btnOpen.getId());
        }

        cancelClick.setPosition(position);
        cancelClick.setListener(listener);

        stopClick.setPosition(position);
        stopClick.setListener(listener);

        restartClick.setPosition(position);
        restartClick.setListener(listener);

        openClick.setPosition(position);
        openClick.setListener(listener);


        Task task = dataSource.get(position);
        holder.nameView.setText(task.getFileName());
        String time = sf.format(new Date(task.getCreateAt()));
        holder.createAtView.setText(time);
        if (task.getContentLength() > 0) {
            int progress = (int) (task.getFinishedLength() * 1.0f / task.getContentLength() * 100);
            holder.progressBar.setProgress(progress);
        }else{
            holder.progressBar.setProgress(0);
        }

        holder.progressBar.setVisibility(View.VISIBLE);
        holder.btnCancel.setVisibility(View.VISIBLE);
        holder.btnStop.setVisibility(View.GONE);
        holder.btnReStart.setVisibility(View.GONE);
        holder.btnOpen.setVisibility(View.GONE);

        if (task.getState() == DownLoadState.DOWNLOAD_STATE_FINISH){
            holder.btnOpen.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_DOWNLOADING){
            holder.btnStop.setVisibility(View.VISIBLE);
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_STOP){
            holder.btnReStart.setVisibility(View.VISIBLE);
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_ERROR){
            holder.btnReStart.setVisibility(View.VISIBLE);
        }

        if (task.getState() == DownLoadState.DOWNLOAD_STATE_WAIT){
            holder.stateView.setText("等待");
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_STOP){
            holder.stateView.setText("已停止");
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_CONNECT){
            holder.stateView.setText("连接中");
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_DOWNLOADING){
            holder.stateView.setText("已下载"+task.getFinishedLength() + "/" + task.getContentLength());
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_ERROR){
            holder.stateView.setText("失败");
        }else if(task.getState() == DownLoadState.DOWNLOAD_STATE_FINISH){
            holder.stateView.setText("完成");
        }

        return convertView;
    }

    private static class ViewHolder {
        ProgressBar progressBar;
        TextView nameView;
        TextView stateView;
        TextView createAtView;
        Button btnCancel;
        Button btnStop;
        Button btnReStart;
        Button btnOpen;
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

    public static class StopClick extends BaseItemClick{
        @Override
        public void onClick(View v) {
            if (listener!=null){
                listener.onStop(v,position);
            }
        }
    }

    public static class ReStartClick extends BaseItemClick{
        @Override
        public void onClick(View v) {
            if (listener!=null){
                listener.onRestart(v,position);
            }
        }
    }

    public static class OpenClick extends BaseItemClick{
        @Override
        public void onClick(View v) {
            if (listener!=null){
                listener.onOpen(v,position);
            }
        }
    }

    public interface Listener {
        void onCancel(View view, int position);
        void onStop(View view, int position);
        void onRestart(View view, int position);
        void onOpen(View view, int position);
    }
}
