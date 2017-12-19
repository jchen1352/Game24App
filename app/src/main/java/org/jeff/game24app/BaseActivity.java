package org.jeff.game24app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private static int sessionDepth = 0;
    protected Intent musicIntent;
    protected boolean playMusic;
    protected int hiScore;
    protected static final String PREFS = "prefs";
    protected static final String MUSIC_PREF = "music_pref";
    protected static final String SCORE_PREF = "score_pref";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicIntent = new Intent(this, MusicService.class);
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        playMusic = preferences.getBoolean(MUSIC_PREF, true);
        hiScore = preferences.getInt(SCORE_PREF, -1);
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

    protected void updatePreferences(boolean musicSetting) {
        playMusic = musicSetting;
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(MUSIC_PREF, musicSetting);
        editor.apply();
    }

    protected void onEnterForeground() {
        if (playMusic) {
            startService(musicIntent);
        }
    }

    protected void onSentToBackground() {
        stopService(musicIntent);
    }
}
