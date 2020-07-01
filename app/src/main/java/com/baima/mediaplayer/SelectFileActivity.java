package com.baima.mediaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.baima.mediaplayer.util.StorageUtil;

import java.util.ArrayList;
import java.util.List;

public class SelectFileActivity extends AppCompatActivity {

    private static final String TAG = "baima";

    //选择文件时的根目录，存储卡和可移动存储卡
    private static final String ROOT = "/root";
    private ListView lv_file;
    private String currentPath;
    private List<String> pathList = new ArrayList<>();
    private FileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);

        lv_file = findViewById(R.id.lv_file);
        adapter = new FileAdapter(this, pathList);
        lv_file.setAdapter(adapter);

        //运行时权限 处理
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, readExternalStorage)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{readExternalStorage}, 1);
        } else {
            goToRoot();
        }
    }

    //跳转根目录
    private void goToRoot() {
        pathList.clear();
        currentPath = ROOT;
        pathList.addAll(StorageUtil.getMountedPaths(this));
        adapter.notifyDataSetChanged();
    }
}
