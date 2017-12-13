package org.jeff.game24app;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import org.jeff.game24app.views.HomeButton;
import org.jeff.game24app.views.HomeRadioGroup;

public class HomeActivity extends BaseActivity {

    /** Intent key that determines generating fraction puzzles **/
    public static final String GEN_FRAC = "gen_frac";
    /** Intent key that determines time trial mode for game **/
    public static final String TIME_TRIAL = "time_trial";
    private HomeButton start, settings, timeTrial, freePlay, back;
    private HomeRadioGroup difficulty;
    private boolean atSelectionScreen;
    private ToggleButton musicButton, soundButton;
    private AlertDialog settingsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        start = (HomeButton) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClicked();
            }
        });

        settings = (HomeButton) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSettingsClicked();
            }
        });
        setupSettingsDialog();

        timeTrial = (HomeButton) findViewById(R.id.time_trial);
        timeTrial.setVisibility(View.GONE);
        timeTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeTrialClicked();
            }
        });

        freePlay = (HomeButton) findViewById(R.id.free_play);
        freePlay.setVisibility(View.GONE);
        freePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFreePlayClicked();
            }
        });

        difficulty = (HomeRadioGroup) findViewById(R.id.difficulty);
        difficulty.setVisibility(View.GONE);

        back = (HomeButton) findViewById(R.id.back);
        back.setVisibility(View.GONE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        atSelectionScreen = false;
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
        timeTrial.fadeIn();
        freePlay.fadeIn();
        difficulty.fadeIn();
        back.fadeIn();
        start.fadeOut();
        settings.fadeOut();
        atSelectionScreen = true;
    }

    private void onSettingsClicked() {
        settingsDialog.show();
    }

    private void onTimeTrialClicked() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GEN_FRAC, difficulty.getCheckedRadioButtonId() == R.id.fractions);
        intent.putExtra(TIME_TRIAL, true);
        startActivity(intent);
    }

    private void onFreePlayClicked() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GEN_FRAC, difficulty.getCheckedRadioButtonId() == R.id.fractions);
        intent.putExtra(TIME_TRIAL, false);
        startActivity(intent);
    }

    public void back() {
        start.fadeIn();
        settings.fadeIn();
        timeTrial.fadeOut();
        freePlay.fadeOut();
        difficulty.fadeOut();
        back.fadeOut();
        atSelectionScreen = false;
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
