package com.baima.mediaplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.baima.mediaplayer.util.StorageUtil;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectFileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "baima";

    public static final String  EXTRA_SELECTED_FILE = "selected_file";
    //选择文件时的根目录，存储卡和可移动存储卡
    public static final String ROOT = "/root";


    private ListView lv_file;
    private String currentPath;
    private List<String> pathList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private FileAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);

        lv_file = findViewById(R.id.lv_file);
        adapter = new FileAdapter(this, nameList);
        lv_file.setAdapter(adapter);
        lv_file.setOnItemClickListener(this);

        //运行时权限 处理
        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, readExternalStorage)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{readExternalStorage}, 1);
        } else {
            goToRoot();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String path = pathList.get(position);
        File file = new File(path);
        if (file.isFile()) {
            //如果 是文件，返回结果
            Intent intent = new Intent();
            intent.putExtra(EXTRA_SELECTED_FILE,file);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            //文件夹，如果是根目录返回根目录，如果是普通 目录跳转
            if (path.equals(ROOT)) {
                goToRoot();
            } else {
                goToPath(path);
            }
        }
    }

    @Override
    public void onBackPressed() {
        //返回键

        if (currentPath.equals(ROOT)) {
            super.onBackPressed();
        } else if (pathList.size() > 1 && pathList.get(1).equals(ROOT)) {
            //如果 上一级是根目录返回根目录
            goToRoot();
        } else {
            goToPath(new File(currentPath).getParent());
        }
    }

    //跳转根目录
    private void goToRoot() {
        pathList.clear();
        nameList.clear();
        currentPath = ROOT;
        pathList.addAll(StorageUtil.getMountedPaths(this));
        for (String path : pathList) {
            nameList.add(new File(path).getName());
        }
        adapter.notifyDataSetChanged();
    }

    //跳转文件夹
    private void goToPath(String path) {
        currentPath = path;
        pathList.clear();
        nameList.clear();
        //根目录
        pathList.add(ROOT);
        nameList.add("根目录");
        //上一级
        //如果 跳转的目录路径是存储卡路径，上一级是根目录
        if (StorageUtil.getMountedPaths(this).contains(path)) {
            pathList.add(ROOT);
        } else {
            pathList.add(new File(path).getParent());
        }
        nameList.add("上一级");


        File file = new File(path);
        File[] files = file.listFiles();
        //获取出目录
        List<File> dirList = new ArrayList<>();
        for (File file1 : files) {
            if (file1.isDirectory() && !file1.getName().startsWith(".")) {
                dirList.add(file1);
            }
        }

        //获取出文件
        List<File> fileList = new ArrayList<>();
        for (File file1 : files) {
            if (file1.isFile() && !file1.getName().startsWith(".")) {
                fileList.add(file1);
            }
        }

        //分别 添加目录和文件
        List<File> dirFileList = new ArrayList<>();
        sort(dirList);
        sort(fileList);
        dirFileList.addAll(dirList);
        dirFileList.addAll(fileList);

        for (File file1 : dirFileList) {
            pathList.add(file1.getAbsolutePath());
            nameList.add(file1.getName());
        }
        adapter.notifyDataSetChanged();
    }


    private void sort(List<File> fileList) {
        final Collator collator = Collator.getInstance();
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });
    }
}
