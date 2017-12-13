package org.jeff.game24app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;
import org.jeff.game24app.tiles.TileManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GameActivity extends BaseActivity {

    private NumberTile tile0, tile1, tile2, tile3;
    private NumberTile[] numTiles;
    private OperationTile tileAdd, tileSub, tileMul, tileDiv;
    private OperationTile[] opTiles;
    private TileManager tileManager;
    private ImageButton settingsButton, restartButton, hintButton;
    private Button replayButton, returnButton;
    private boolean timeTrialMode;
    private TextView scoreView, finalScoreView;
    private int score;
    private TextView time;
    private static final long TIME_LIMIT = 1000 * 60 * 5; // 5 minutes
    private static final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private CountDownTimer timer;
    private AlertDialog gameOverDialog;
    private Game24Generator generator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setupTiles();

        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        restartButton = (ImageButton) findViewById(R.id.restart_button);
        hintButton = (ImageButton) findViewById(R.id.hint_button);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettings();
            }
        });
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartPuzzle();
            }
        });

        Intent intent = getIntent();
        generator = new Game24Generator(intent.getBooleanExtra(HomeActivity.GEN_FRAC, false));
        timeTrialMode = intent.getBooleanExtra(HomeActivity.TIME_TRIAL, false);
        scoreView = (TextView) findViewById(R.id.score);
        time = (TextView) findViewById(R.id.time);
        if (!timeTrialMode) {
            scoreView.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
        } else {
            setupTimer();
            setupGameOverDialog();
            setupTimeTrial();
        }
        newPuzzle();
    }

    private void setupTimeTrial() {
        score = -1; //because incremented once when newPuzzle is initially called
        scoreView.setText(getResources().getString(R.string.score, score));
        timer.cancel();
        timer.start();
    }

    private void setupTimer() {
        timer = new CountDownTimer(TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time.setText(sdf.format(new Date(millisUntilFinished)));
            }

            @Override
            public void onFinish() {
                time.setText(sdf.format(new Date(0)));
                gameOverDialog.show();
            }
        };
    }

    private void setupGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_gameover, null);
        replayButton = (Button) layout.findViewById(R.id.restart_button);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupTimeTrial();
                newPuzzle();
                gameOverDialog.dismiss();
            }
        });
        returnButton = (Button) layout.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOverDialog.dismiss();
                finish();
            }
        });
        finalScoreView = (TextView) layout.findViewById(R.id.score);
        builder.setView(layout);
        builder.setCancelable(false);
        gameOverDialog = builder.create();
    }

    private void setupTiles() {
        tile0 = (NumberTile) findViewById(R.id.tile0);
        tile1 = (NumberTile) findViewById(R.id.tile1);
        tile2 = (NumberTile) findViewById(R.id.tile2);
        tile3 = (NumberTile) findViewById(R.id.tile3);
        numTiles = new NumberTile[] {tile0, tile1, tile2, tile3};
        tileAdd = (OperationTile) findViewById(R.id.tile_add);
        tileSub = (OperationTile) findViewById(R.id.tile_subtract);
        tileMul = (OperationTile) findViewById(R.id.tile_multiply);
        tileDiv = (OperationTile) findViewById(R.id.tile_divide);
        opTiles = new OperationTile[] {tileAdd, tileSub, tileMul, tileDiv};
        tileManager = new TileManager(this);
        for (NumberTile tile : numTiles) {
            tile.setOnClickListener(tileManager.getNumListener());
        }

        for (OperationTile tile : opTiles) {
            tile.setOnClickListener(tileManager.getOpListener());
        }
    }

    private void setupPuzzle(Rational[] puzzle) {
        for (int i = 0; i < numTiles.length; i++) {
            numTiles[i].setExists(true);
            numTiles[i].unselect();
            numTiles[i].setValue(puzzle[i]);
        }
        for (OperationTile tile : opTiles) {
            tile.unselect();
        }
        tileManager.reset();
    }

    public void showSettings() {

    }

    public void newPuzzle() {
        setupPuzzle(generator.generatePuzzle());
        if (timeTrialMode) {
            score++;
            scoreView.setText(getResources().getString(R.string.score, score));
            finalScoreView.setText(getResources().getString(R.string.game_over_score, score));
        }
    }

    public void restartPuzzle() {
        setupPuzzle(generator.restartPuzzle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeTrialMode) {
            timer.cancel();
        }
    }
}
