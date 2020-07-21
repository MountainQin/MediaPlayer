package com.baima.mediaplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baima.mediaplayer.entities.Music;
import com.baima.mediaplayer.service.PlayService;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "baima";

    private static final int SELECT_MUSIC = 1;

    private PlayService.PlayBinder playBinder;
    private PlayServiceConnection playServiceConnection;
    private Intent playServiceIntent;
    private TextView tv_rew;
    private TextView tvff;
    private TextView tv_play;
    private SharedPreferences defaultSharedPreferences;
    private String currentMusicPath;
    private ListView lv_music;
    private MusicAdapter adapter;
    private List<Music> musicList;
    private TextView tv_playing_name;
    private TextView tv_previous;
    private TextView tv_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String readExternalStorage = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, readExternalStorage)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{readExternalStorage}, 1);
        } else {
            initViews();
            startPlay();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Music music = musicList.get(position);
        currentMusicPath=music.getPath();
        //如果 没链接服务就链接播放，如果 已经连接就直接播放
        if (playBinder==null) {
            startBindPlayService();
        }else {
            playBinder.play(music.getPath());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Music music = musicList.get(position);
        showDeleteMusicDialog(music);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (playBinder == null) {
            //没有连接服务
            switch (v.getId()) {
                case R.id.tv_play:
                    //开启并连接服务，播放最后播放的音乐
                    currentMusicPath = defaultSharedPreferences.getString("last_music_path", "");
                    if (currentMusicPath.equals("")) {
                        Toast.makeText(this, "请先选择音乐！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startBindPlayService();
                    break;
            }

        } else {
            //已经连接服务
            switch (v.getId()) {
                case R.id.tv_rew:
                    playBinder.rew();
                    break;
                case R.id.tv_previous:
playPrevious();
                    break;
                                    case R.id.tv_play:
                    String s = tv_play.getText().toString();
                    if ("播放".equals(s)) {
                        playBinder.start();
                        tv_play.setText("暂停");
                    } else {
                        playBinder.pause();
                        tv_play.setText("播放");
                    }
                    break;
                case R.id.tv_next:
                    playNext();
                    break;
                case R.id.tvff:
                    playBinder.ff();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_MUSIC:
                    File file = (File) data.getSerializableExtra(SelectFileActivity.EXTRA_SELECTED_FILE);
                    currentMusicPath = file.getAbsolutePath();
                    addMusic(file.getAbsolutePath());
                    refreshListData();
                    startBindPlayService();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        //按返回键移至后台
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        String defaultPath = defaultSharedPreferences.getString("default_path", SelectFileActivity.ROOT);
        menu.findItem(R.id.default_path)
                .setTitle("选择音乐默认路径:" + defaultPath);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_music:
                Intent intent = new Intent(this, SelectFileActivity.class);
                startActivityForResult(intent, SELECT_MUSIC);
                break;
            case R.id.exit:
                if (playServiceConnection != null && playServiceIntent != null) {
                    unbindService(playServiceConnection);
                    stopService(playServiceIntent);
                    playServiceConnection = null;
                    playServiceIntent = null;
                }
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        tv_play.setText("播放");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        tv_play.setText("暂停");
        //保存为最后播放的音乐
        SharedPreferences.Editor edit = defaultSharedPreferences.edit();
        edit.putString("last_music_path", currentMusicPath);
        edit.apply();

        //设置正在播放标签
        String name = new File(currentMusicPath).getName();
        if (name.contains(".")){
            name=name.substring(0, name.lastIndexOf("."));
        }
        tv_playing_name.setText("正在播放："+name);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initViews();
                    startPlay();
                }else{
                    Toast.makeText(this, "拒绝权限将无法播放音乐！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initViews() {
        tv_playing_name = findViewById(R.id.tv_playing_name);
        lv_music = findViewById(R.id.lv_music);
        tv_rew = findViewById(R.id.tv_rew);
        tv_previous = findViewById(R.id.tv_previous);

        tv_play = findViewById(R.id.tv_play);

        tv_next = findViewById(R.id.tv_next);
        tvff = findViewById(R.id.tvff);
        defaultSharedPreferences =getSharedPreferences("config", MODE_PRIVATE);

        musicList = new ArrayList<>();
        adapter = new MusicAdapter(this, musicList);
        lv_music.setAdapter(adapter);
        refreshListData();

        lv_music.setOnItemClickListener(this);
        lv_music.setOnItemLongClickListener(this);
        tv_rew.setOnClickListener(this);
        tv_previous.setOnClickListener(this);
        tv_play.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        tvff.setOnClickListener(this);

    }

    //开启并连接服务
    private void startBindPlayService() {
        playServiceIntent = new Intent(this, PlayService.class);
        startService(playServiceIntent);
        playServiceConnection = new PlayServiceConnection();
        bindService(playServiceIntent, playServiceConnection, BIND_AUTO_CREATE);
    }

    private class PlayServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playBinder = (PlayService.PlayBinder) service;
            playBinder.setOnPreparedListener(MainActivity.this);
            playBinder.setOnCompletionListener(MainActivity.this);
            playBinder.play(currentMusicPath);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


    //刷新列表数据
    private void refreshListData() {
        musicList.clear();
        List<Music> list = LitePal.order("addDate desc").find(Music.class);
        musicList.addAll(list);
        adapter.notifyDataSetChanged();
        lv_music.smoothScrollToPosition(0);
    }

    //添加音乐
    private void addMusic(String path) {
        List<Music> musicList = LitePal.where("path=?", path).find(Music.class);
        if (musicList.size() > 0) {
            //如果 存在就修改添加时间
            for (Music music : musicList) {
                music.setAddDate(System.currentTimeMillis());
                music.update(music.getId());
            }
        } else {
            //如果在数据 库中不存在就添加
            Music music = new Music();
            music.setPath(path);
            music.setAddDate(System.currentTimeMillis());
            music.save();
        }
    }

    //显示删除音乐的对话框
    private void showDeleteMusicDialog(final Music music){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage("你确定要删除吗？")
                .setNegativeButton("取消",null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (currentMusicPath.equals(music.getPath())){
                            playBinder.stop();
                        }
                        music.delete();
                        musicList.remove(music);
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    //上一首
    private void playPrevious(){
        if (musicList.size()<1) {
            //如果列表没有项目返回；
return;
        }
            for (int i = 0; i < musicList.size(); i++) {
                if (musicList.get(i).getPath().equals(currentMusicPath)){
    i--;
    if (i<0){
        i=musicList.size()-1;
    }
    currentMusicPath=musicList.get(i).getPath();
    playBinder.play(currentMusicPath);
    return;
                }
            }
    }
    //下一首
    private void playNext(){
        if (musicList.size()<1) {
            //如果列表没有项目返回；
            return;
        }
        for (int i = 0; i < musicList.size(); i++) {
            if (musicList.get(i).getPath().equals(currentMusicPath)){
                i++;
                if (i>=musicList.size()){
                    i=0;
                }
                currentMusicPath=musicList.get(i).getPath();
                playBinder.play(currentMusicPath);
                return;
            }
        }
    }

    //如果从文件管理等过来就播放
    private void startPlay(){
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data!=null){
            currentMusicPath=data.getPath();
            addMusic(currentMusicPath);
            refreshListData();
            startBindPlayService();
                    }
    }
}
