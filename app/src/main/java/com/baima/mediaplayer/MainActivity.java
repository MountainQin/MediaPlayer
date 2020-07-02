package com.baima.mediaplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.baima.mediaplayer.service.PlayService;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_MUSIC = 1;

    private PlayService.PlayBinder playBinder;
    private PlayServiceConnection playServiceConnection;
    private Intent playServiceIntent;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_MUSIC:
                    file = (File) data.getSerializableExtra(SelectFileActivity.EXTRA_SELECTED_FILE);
                    playServiceIntent = new Intent(this, PlayService.class);
                    startService(playServiceIntent);
                    playServiceConnection = new PlayServiceConnection();
                    bindService(playServiceIntent, playServiceConnection, BIND_AUTO_CREATE);
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

    private class PlayServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playBinder = (PlayService.PlayBinder) service;
            playBinder.play(file.getAbsolutePath());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
