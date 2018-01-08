package org.jeff.game24app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class HomeActivity extends BaseActivity {

    /** Intent key that determines generating fraction puzzles **/
    public static final String GEN_FRAC = "gen_frac";
    /** Intent key that determines time trial mode for game **/
    public static final String TIME_TRIAL = "time_trial";

    public static final String ONLINE = "online";

    public static final String MAKE_GAME = "make_game";

    public static final String IS_HOST = "is_host";
    private ImageButton start, settings, help;
    private Button timeTrial, freePlay, difficulty, back;
    /** False is classic, true is fractional **/
    private boolean difficultyMode;
    private boolean atSelectionScreen;
    private ToggleButton musicButton, soundButton;
    private AlertDialog settingsDialog;
    private Animator fadeIn, fadeOut;
    private boolean fadingOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        start = (ImageButton) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClicked();
            }
        });

        settings = (ImageButton) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClicked();
            }
        });
        setupSettingsDialog();

        help = (ImageButton) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHelpClicked();
            }
        });

        timeTrial = (Button) findViewById(R.id.time_trial);
        timeTrial.setVisibility(View.GONE);
        timeTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeTrialClicked();
            }
        });

        freePlay = (Button) findViewById(R.id.free_play);
        freePlay.setVisibility(View.GONE);
        freePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFreePlayClicked();
            }
        });

        difficulty = (Button) findViewById(R.id.difficulty);
        difficulty.setVisibility(View.GONE);
        difficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDifficultyClicked();
            }
        });
        setDifficultyText();

        back = (Button) findViewById(R.id.back);
        back.setVisibility(View.GONE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        atSelectionScreen = false;
        difficultyMode = false;
    }

    private void setupSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_settings, null);
        musicButton = (ToggleButton) layout.findViewById(R.id.music);
        musicButton.setChecked(playMusic);
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic = ((ToggleButton)v).isChecked();
                if (playMusic) {
                    startService(musicIntent);
                } else {
                    stopService(musicIntent);
                }
                updatePreferences(playMusic);
            }
        });
        soundButton = (ToggleButton) layout.findViewById(R.id.sound);
        builder.setView(layout);
        settingsDialog = builder.create();
    }

    private void onStartClicked() {
        fadeOut(start);
        fadeOut(settings);
        fadeOut(help);
        atSelectionScreen = true;
    }

    private void onSettingsClicked() {
        settingsDialog.show();
    }

    private void onHelpClicked() {

    }

    private void onTimeTrialClicked() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GEN_FRAC, difficultyMode);
        intent.putExtra(TIME_TRIAL, true);
        intent.putExtra(ONLINE, false);
        intent.putExtra(IS_HOST, false);
        startActivity(intent);
    }

    private void onFreePlayClicked() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GEN_FRAC, difficultyMode);
        intent.putExtra(TIME_TRIAL, false);
        intent.putExtra(ONLINE, false);
        intent.putExtra(IS_HOST, false);
        startActivity(intent);
    }

    private void onMakeOnlineGameClicked() {
        Intent intent = new Intent(this, OnlineActivity.class);
        intent.putExtra(GEN_FRAC, difficultyMode);
        intent.putExtra(MAKE_GAME, true);
        startActivity(intent);
    }

    private void onJoinOnlineGameClicked() {
        Intent intent = new Intent(this, OnlineActivity.class);
        intent.putExtra(GEN_FRAC, difficultyMode);
        intent.putExtra(MAKE_GAME, false);
        startActivity(intent);
    }

    private void onDifficultyClicked() {
        difficultyMode = !difficultyMode;
        setDifficultyText();
    }

    private void setDifficultyText() {
        String fractional = getString(R.string.fractions);
        String classic = getString(R.string.classic);
        difficulty.setText(getString(R.string.difficulty, difficultyMode ? fractional : classic));
    }

    public void back() {
        fadeOut(timeTrial);
        fadeOut(freePlay);
        fadeOut(difficulty);
        fadeOut(back);
        atSelectionScreen = false;
    }

    private void fadeTransition() {
        if (atSelectionScreen) {
            fadeIn(timeTrial);
            fadeIn(freePlay);
            fadeIn(difficulty);
            fadeIn(back);
        } else {
            fadeIn(start);
            fadeIn(settings);
            fadeIn(help);
        }
        fadingOut = false;
    }

    private void fadeIn(final View v) {
        Animator a = AnimatorInflater.loadAnimator(this, R.animator.grow);
        a.setTarget(v);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setClickable(true);
            }
        });
        a.start();
    }

    private void fadeOut(final View v) {
        fadingOut = true;
        Animator a = AnimatorInflater.loadAnimator(this, R.animator.shrink);
        a.setTarget(v);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                v.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setVisibility(View.GONE);
                fadeTransition();
            }
        });
        a.start();
    }

    @Override
    public void onBackPressed() {
        if (atSelectionScreen) {
            back();
        }
        else {
            super.onBackPressed();
        }
    }
}
