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

    public static final String GEN_FRAC = "gen_frac";
    private HomeButton start, timeTrial, freePlay, back;
    private HomeRadioGroup difficulty;
    private boolean atSelectionScreen;
    private ToggleButton musicButton, soundButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        start = (HomeButton) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeTrial.fadeIn();
                freePlay.fadeIn();
                difficulty.fadeIn();
                back.fadeIn();
                start.fadeOut();
                atSelectionScreen = true;
            }
        });

        timeTrial = (HomeButton) findViewById(R.id.time_trial);
        timeTrial.setVisibility(View.GONE);
        timeTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        freePlay = (HomeButton) findViewById(R.id.free_play);
        freePlay.setVisibility(View.GONE);
        freePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, GameActivity.class);
                intent.putExtra(GEN_FRAC, difficulty.getCheckedRadioButtonId() == R.id.fractions);
                startActivity(intent);
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

        musicButton = (ToggleButton) findViewById(R.id.music);
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
        soundButton = (ToggleButton) findViewById(R.id.sound);

        atSelectionScreen = false;
    }

    public void back() {
        start.fadeIn();
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
