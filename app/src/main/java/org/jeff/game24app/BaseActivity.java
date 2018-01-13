package org.jeff.game24app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.UUID;

/**
 * An Activity that other activities extend. Provides mainly support for audio.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static int sessionDepth = 0;
    protected Intent musicIntent;
    private SoundManager soundManager;
    protected boolean playMusic;
    protected boolean playSound;
    protected static final String PREFS = "prefs";
    protected static final String MUSIC_PREF = "music_pref";
    protected static final String SOUND_PREF = "sound_pref";
    protected static final String CLASSIC_SOLVED_PREF = "classic_solved_pref";
    protected static final String FRAC_SOLVED_PREF = "frac_solved_pref";
    protected static final String CLASSIC_SCORE_PREF = "classic_score_pref";
    protected static final String FRAC_SCORE_PREF = "frac_score_pref";
    protected static final String NUM_WON_PREF = "num_won_pref";
    protected static final String NUM_PLAYED_PREF = "num_played_pref";

    private static String uniqueID;
    private static final String UNIQUE_ID_PREF = "pref_unique_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicIntent = new Intent(this, MusicService.class);
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        soundManager = new SoundManager(this);
        playMusic = preferences.getBoolean(MUSIC_PREF, true);
        playSound = preferences.getBoolean(SOUND_PREF, true);

        if (uniqueID == null) {
            uniqueID = preferences.getString(UNIQUE_ID_PREF, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(UNIQUE_ID_PREF, uniqueID);
                editor.apply();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        soundManager.initialize(this);
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
        soundManager.destroy();
    }

    protected void onEnterForeground() {
        if (playMusic) {
            startService(musicIntent);
        }
    }

    protected void onSentToBackground() {
        stopService(musicIntent);
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

    public void playFailSound() {
        if (playSound) {
            soundManager.playSound(SoundManager.FAIL);
        }
    }

    public static String getUniqueID() {
        return uniqueID;
    }
}
