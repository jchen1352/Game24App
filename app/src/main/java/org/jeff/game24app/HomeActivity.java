package org.jeff.game24app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import org.jeff.game24app.animations.ViewAnimatorFactory;

public class HomeActivity extends AppCompatActivity {

    private HomeButton start, timeTrial, freePlay, back;
    private RadioGroup difficulty;
    //Temporary for animating the radio group
    private Animator difficultyFadeIn, difficultyFadeOut;
    private boolean atSelectionScreen;

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
                difficulty.setVisibility(View.VISIBLE);
                difficultyFadeIn.start();
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
                startActivity(intent);
            }
        });

        difficulty = (RadioGroup) findViewById(R.id.difficulty);
        difficulty.setVisibility(View.GONE);
        ViewAnimatorFactory factory = new ViewAnimatorFactory(difficulty);
        difficultyFadeIn = factory.getFadeInAnimator();
        difficultyFadeOut = factory.getFadeOutAnimator();

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

    public void back() {
        start.fadeIn();
        timeTrial.fadeOut();
        freePlay.fadeOut();
        difficultyFadeOut.start();
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
