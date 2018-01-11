package org.jeff.game24app.game;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jeff.game24app.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The concrete activity that handles timed play mode.
 * Features of timed play mode: 5 minutes to solve as many as possible,
 * no watching ads for new hints (but can still use hints)
 */
public class TimedGameActivity extends HintGameActivity {

    private TextView timeView, scoreView, finalScoreView, finalHiScoreView;
    private int score;
    private static final long TIME_LIMIT = 1000 * 30 * 1; // 5 minutes, shorter when testing
    private static final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private CountDownTimer timer;
    private AlertDialog gameOverDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scoreView = (TextView) findViewById(R.id.score);
        timeView = (TextView) findViewById(R.id.time);

        timer = new CountDownTimer(TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeView.setText(sdf.format(new Date(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                timeView.setText(sdf.format(new Date(0)));
                showGameOverDialog();
            }
        };

        //Set up gameOverDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_gameover, null);
        Button replayButton = (Button) layout.findViewById(R.id.restart_button);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTapSound();
                hintManager.reset();
                setupTimeTrial();
                score = -1; //Because setupNewPuzzle increments score
                setupNewPuzzle();
                gameOverDialog.dismiss();
            }
        });
        Button returnButton = (Button) layout.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTapSound();
                gameOverDialog.dismiss();
                finish();
            }
        });
        finalScoreView = (TextView) layout.findViewById(R.id.score);
        finalHiScoreView = (TextView) layout.findViewById(R.id.hi_score);

        builder.setView(layout);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        gameOverDialog = builder.create();
        gameOverDialog.setCanceledOnTouchOutside(false);

        setupTimeTrial();
    }

    @Override
    protected void setupNewPuzzle() {
        super.setupNewPuzzle();
        score++;
        scoreView.setText(getResources().getString(R.string.score, score));
    }

    private void setupTimeTrial() {
        score = 0;
        scoreView.setText(getResources().getString(R.string.score, score));
        timer.cancel();
        timer.start();
    }

    private void showGameOverDialog() {
        dismissHintDialog();
        finalScoreView.setText(getString(R.string.game_over_score, score));
        if (score > hiScore) {
            hiScore = score;
            finalHiScoreView.setText(getString(R.string.new_hi_score));
            SharedPreferences preferences = getSharedPreferences(PREFS, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(SCORE_PREF, hiScore);
            editor.apply();
        } else {
            finalHiScoreView.setText(getString(R.string.game_over_hi_score, hiScore));
        }
        gameOverDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    protected void onHintClicked() {
        if (numHints > 0) {
            showHint();
        }
    }

    /**
     * Do nothing.
     */
    @Override
    protected void onMoreHintsClicked() {

    }

    @Override
    protected void onNumHintsChanged() {
        super.onNumHintsChanged();
        if (numHints <= 0) {
            hintButton.setEnabled(false);
        } else {
            hintButton.setEnabled(true);
        }
    }
}
