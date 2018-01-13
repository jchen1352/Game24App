package org.jeff.game24app;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Handles all of the sound effects in the game.
 */
public class SoundManager {

    private Context context;
    private MediaPlayer tapPlayer, successPlayer, failPlayer;

    @IntDef({TAP, SUCCESS, FAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Sounds {
    }

    public static final int TAP = 0;
    public static final int SUCCESS = 1;
    public static final int FAIL = 2;

    public SoundManager(Context c) {
        initialize(c);
    }

    /**
     * Initialize every MediaPlayer
     */
    public void initialize(Context c) {
        context = c;
        if (tapPlayer == null) {
            tapPlayer = MediaPlayer.create(context, R.raw.tap_sound);
        }
        if (successPlayer == null) {
            successPlayer = MediaPlayer.create(context, R.raw.success_sound);
        }
        if (failPlayer == null) {
            failPlayer = MediaPlayer.create(context, R.raw.fail_sound);
        }
    }

    /**
     * Plays the MediaPlayer corresponding to the provided sound
     *
     * @param sound
     */
    public void playSound(@Sounds int sound) {
        switch (sound) {
            case TAP:
                playSound(tapPlayer);
                break;
            case SUCCESS:
                playSound(successPlayer);
                break;
            case FAIL:
                playSound(failPlayer);
                break;
        }
    }

    /**
     * Plays a given MediaPlayer
     *
     * @param mp
     */
    private void playSound(MediaPlayer mp) {
        if (mp != null) {
            if (mp.isPlaying()) {
                //restart
                mp.pause();
                mp.seekTo(0);
            }
            mp.start();
        }
    }

    public void destroy() {
        tapPlayer.release();
        tapPlayer = null;
        successPlayer.release();
        successPlayer = null;
        failPlayer.release();
        failPlayer = null;
        context = null;
    }
}
