package com.baima.mediaplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;

public class PlayService extends Service {

    private static final int PLAY_FILE_EXCEPTION = 1;

    private PlayBinder playBinder;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAY_FILE_EXCEPTION:
                    Toast.makeText(PlayService.this, "文件错误，请重试！", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        playBinder = new PlayBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        playBinder.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playBinder;
    }

    public class PlayBinder extends Binder {
        private MediaPlayer mediaPlayer;

        public PlayBinder() {
            mediaPlayer = new MediaPlayer();
        }

        public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
            mediaPlayer.setOnCompletionListener(listener);
        }

        public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
            mediaPlayer.setOnPreparedListener(listener);
        }


        public void play(final String path) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(PLAY_FILE_EXCEPTION);
            }
        }

        //快退
        public void rew() {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                currentPosition -= 5000;
                if (currentPosition < 0) {
                    currentPosition = 0;
                }
                mediaPlayer.seekTo(currentPosition);
            }
        }

        //快进
        public void ff() {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                currentPosition += 5000;
                if (currentPosition > mediaPlayer.getDuration()) {
                    currentPosition = mediaPlayer.getDuration();
                }
                mediaPlayer.seekTo(currentPosition);
            }
        }

        public void start() {
            mediaPlayer.start();
            ;
        }

        public void pause() {
            mediaPlayer.pause();
        }

        public void release() {
            mediaPlayer.release();
        }
    }

}
