package com.zsp.filedownloader.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zsp.filedownloader.Config;
import com.zsp.filedownloader.DownLoader;
import com.zsp.filedownloader.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = DownLoader.getInstance().getConfig().getSaveDir();
        File file = new File(path);
        if (!file.exists()){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android
                    .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DemoActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String path = DownLoader.getInstance().getConfig().getSaveDir();
                        File file = new File(path);
                        if (!file.exists()) {
                            boolean b = file.mkdirs();
                            Log.d(TAG, "onCreate: 创建目录" + path + "===" + b);
                        }
                    }
                    break;
                }
        }
    }
}
