package org.jeff.game24app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private static int sessionDepth = 0;
    protected Intent musicIntent;
    private SoundManager soundManager;
    protected boolean playMusic;
    protected boolean playSound;
    protected int hiScore;
    protected static final String PREFS = "prefs";
    protected static final String MUSIC_PREF = "music_pref";
    protected static final String SOUND_PREF = "sound_pref";
    protected static final String SCORE_PREF = "score_pref";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicIntent = new Intent(this, MusicService.class);
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        soundManager = new SoundManager(this);
        playMusic = preferences.getBoolean(MUSIC_PREF, true);
        playSound = preferences.getBoolean(SOUND_PREF, true);
        hiScore = preferences.getInt(SCORE_PREF, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sessionDepth++;
        if (sessionDepth == 1) {
            onEnterForeground();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sessionDepth > 0) {
            sessionDepth--;
        }
        if (sessionDepth == 0) {
            onSentToBackground();
        }
    }

    protected void onEnterForeground() {
        if (playMusic) {
            startService(musicIntent);
        }
    }

    protected void onSentToBackground() {
        stopService(musicIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundManager.destroy();
        soundManager = null;
    }

    protected void updateMusicPrefs(boolean musicSetting) {
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MUSIC_PREF, musicSetting);
        editor.apply();
    }

    protected void updateSoundPrefs(boolean soundSetting) {
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SOUND_PREF, soundSetting);
        editor.apply();
    }

    public void playTapSound() {
        if (playSound) {
            soundManager.playSound(SoundManager.TAP);
        }
    }

    public void playSuccessSound() {
        if (playSound) {
            soundManager.playSound(SoundManager.SUCCESS);
        }
    }
}
