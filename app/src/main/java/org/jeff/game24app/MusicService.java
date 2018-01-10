package org.jeff.game24app;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class MusicService extends Service {

    private MediaPlayer musicPlayer;
    protected static int musicTime = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        musicPlayer = MediaPlayer.create(this, R.raw.rainbows);
        musicPlayer.setLooping(true);
        if (musicTime != -1) {
            musicPlayer.seekTo(musicTime);
        }
        musicPlayer.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        musicTime = musicPlayer.getCurrentPosition();
        musicPlayer.stop();
        musicPlayer.release();
        musicPlayer = null;
    }
}
