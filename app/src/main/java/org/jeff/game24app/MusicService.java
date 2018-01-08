package org.jeff.game24app;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MusicService extends Service {

    private MediaPlayer musicPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*Log.d("MusicService", "music start");
        musicPlayer = MediaPlayer.create(this, R.raw.bensound_littleidea.mp3);
        musicPlayer.setLooping(true);
        musicPlayer.start();*/
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        /*Log.d("MusicService", "music stop");
        musicPlayer.stop();
        musicPlayer.release();
        musicPlayer = null;*/
    }
}
