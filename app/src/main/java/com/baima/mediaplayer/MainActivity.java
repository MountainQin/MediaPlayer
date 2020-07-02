package com.baima.mediaplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baima.mediaplayer.service.PlayService;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final int SELECT_MUSIC = 1;

    private PlayService.PlayBinder playBinder;
    private PlayServiceConnection playServiceConnection;
    private Intent playServiceIntent;
    private TextView tv_rew;
    private TextView tvff;
    private TextView tv_play;
    private SharedPreferences defaultSharedPreferences;
    private String currentMusicPath;

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
        }
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
    }

    private void initViews() {
        tv_rew = findViewById(R.id.tv_rew);
        tv_play = findViewById(R.id.tv_play);
        tvff = findViewById(R.id.tvff);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        tv_rew.setOnClickListener(this);
        tv_play.setOnClickListener(this);
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
}
