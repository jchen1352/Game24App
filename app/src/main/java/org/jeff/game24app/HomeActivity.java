package org.jeff.game24app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HomeActivity extends BaseActivity {

    /** Intent key that determines generating fraction puzzles **/
    public static final String GEN_FRAC = "gen_frac";
    /** Intent key that determines time trial mode for game **/
    public static final String TIME_TRIAL = "time_trial";
    private ImageButton start, settings, help;
    private Button timeTrial, freePlay, difficulty;
    /** False is classic, true is fractional **/
    private boolean difficultyMode;
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

        difficultyMode = false;
        setDifficultyText();

        final ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout);
        layout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                //Make the title spread out across the view (at least mostly)
                TextView titleTop = (TextView) findViewById(R.id.title_top);
                Rect bounds = new Rect();
                Paint paint = titleTop.getPaint();
                String text = titleTop.getText().toString();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int textWidth = bounds.width();
                int viewWidth = titleTop.getWidth();
                float ems = (viewWidth - textWidth) / titleTop.getTextSize();
                titleTop.setLetterSpacing(ems / text.length());
                TextView titleBottom = (TextView) findViewById(R.id.title_bottom);
                paint = titleBottom.getPaint();
                text = titleBottom.getText().toString();
                paint.getTextBounds(text, 0, text.length(), bounds);
                textWidth = bounds.width();
                viewWidth = titleBottom.getWidth();
                ems = (viewWidth - textWidth) / titleBottom.getTextSize();
                titleBottom.setLetterSpacing(ems / text.length());
            }
        });
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
        startActivity(intent);
    }

    private void onFreePlayClicked() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GEN_FRAC, difficultyMode);
        intent.putExtra(TIME_TRIAL, false);
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

    private void fadeTransition() {
        fadeIn(timeTrial);
        fadeIn(freePlay);
        fadeIn(difficulty);
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
}
