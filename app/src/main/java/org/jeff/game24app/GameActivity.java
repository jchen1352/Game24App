package org.jeff.game24app;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jeff.game24app.animations.ViewAnimatorGen;
import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Operation;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;
import org.jeff.game24app.tiles.TileManager;
import org.jeff.game24app.views.DarkView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameActivity extends BaseActivity
        implements ViewAnimatorGen.ShrinkFinishListener{

    private NumberTile[] numTiles;
    private OperationTile[] opTiles;
    private TileManager tileManager;
    private View numTileGroup, opTileGroup;
    private ViewAnimatorGen numAnimatorGen;
    private Animator numShrinkAnimator, numGrowAnimator;
    private DarkView darkView;
    private boolean timeTrialMode;
    private TextView scoreView, finalScoreView, finalHiScoreView;
    private int score;
    private TextView time;
    private static final long TIME_LIMIT = 1000 * 30 * 1; // 5 minutes, shorter when testing
    private static final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private CountDownTimer timer;
    private AlertDialog gameOverDialog;
    private Game24Generator generator;
    private Rational[] nextPuzzle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setupTiles();

        ImageButton settingsButton = (ImageButton) findViewById(R.id.settings_button);
        ImageButton restartButton = (ImageButton) findViewById(R.id.restart_button);
        ImageButton hintButton = (ImageButton) findViewById(R.id.hint_button);

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
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHint();
            }
        });
        darkView = (DarkView) findViewById(R.id.dark_view);

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
            setupTimeTrial();
        }
        nextPuzzle = generator.generatePuzzle();
        onShrinkFinish();
    }

    private void setupTiles() {
        NumberTile tile0 = (NumberTile) findViewById(R.id.tile0);
        NumberTile tile1 = (NumberTile) findViewById(R.id.tile1);
        NumberTile tile2 = (NumberTile) findViewById(R.id.tile2);
        NumberTile tile3 = (NumberTile) findViewById(R.id.tile3);
        numTiles = new NumberTile[] {tile0, tile1, tile2, tile3};
        OperationTile tileAdd = (OperationTile) findViewById(R.id.tile_add);
        OperationTile tileSub = (OperationTile) findViewById(R.id.tile_subtract);
        OperationTile tileMul = (OperationTile) findViewById(R.id.tile_multiply);
        OperationTile tileDiv = (OperationTile) findViewById(R.id.tile_divide);
        opTiles = new OperationTile[] {tileAdd, tileSub, tileMul, tileDiv};
        numTileGroup = findViewById(R.id.num_tile_group);
        opTileGroup = findViewById(R.id.op_tile_group);
        numAnimatorGen = new ViewAnimatorGen(numTileGroup);
        numAnimatorGen.setShrinkFinishListener(this);
        numShrinkAnimator = numAnimatorGen.getGroupShrinkAnimator();
        numGrowAnimator = numAnimatorGen.getGroupGrowAnimator();
        tileManager = new TileManager(this);
        for (NumberTile tile : numTiles) {
            tile.setOnClickListener(tileManager.getNumListener());
        }

        for (OperationTile tile : opTiles) {
            tile.setOnClickListener(tileManager.getOpListener());
        }
    }

    private void setupPuzzle() {
        for (int i = 0; i < numTiles.length; i++) {
            numTiles[i].setExists(true);
            numTiles[i].unselect();
            numTiles[i].setValue(nextPuzzle[i]);
        }
        numGrowAnimator.start();
        for (OperationTile tile : opTiles) {
            tile.unselect();
        }
        tileManager.reset();
    }

    public void newPuzzle() {
        nextPuzzle = generator.generatePuzzle();
        shrinkNumTiles();
        if (timeTrialMode) {
            score++;
            scoreView.setText(getResources().getString(R.string.score, score));
        }
    }

    public void restartPuzzle() {
        shrinkNumTiles();
    }

    private void shrinkNumTiles() {
        numShrinkAnimator.start();
    }

    /**
     * When shrinking is finished for all number tiles, setup puzzle.
     */
    @Override
    public void onShrinkFinish() {
        setupPuzzle();
    }

    /**
     * Displays a hint for the current numbers, called when clicking the hint button.
     * Currently will make the tiles involved bobble. Also unselects everything.
     */
    public void showHint() {
        List<Rational> puzzleList = new ArrayList<>(4);
        for (NumberTile tile : numTiles) {
            if (tile.exists()) {
                if (tile.isActive()) {
                    tile.performClick();
                }
                puzzleList.add(tile.getValue());
            }
        }
        Rational[] puzzle = puzzleList.toArray(new Rational[puzzleList.size()]);
        Operation hint = Game24Generator.getHint(puzzle);
        if (hint == null) {
            return;
        }
        NumberTile hintNum0 = numTiles[0], hintNum1 = numTiles[0];
        OperationTile hintOp = opTiles[0];
        for (NumberTile tile : numTiles) {
            if (tile.exists() && tile.getValue().equals(hint.getNum0())) {
                hintNum0 = tile;
            }
        }
        for (NumberTile tile : numTiles) {
            if (tile.exists() && tile.getValue().equals(hint.getNum1()) && tile != hintNum0) {
                hintNum1 = tile;
            }
        }
        for (OperationTile tile : opTiles) {
            if (tile.isActive()) {
                tile.performClick();
            }
            if (tile.getOp() == hint.getOp()) {
                hintOp = tile;
            }
        }
        darkView.setVisibility(View.VISIBLE);
        darkView.setViews(hintNum0, hintOp, hintNum1, numTileGroup, opTileGroup);
        darkView.startHint();
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
                setupGameOverDialog();
                gameOverDialog.show();
            }
        };
    }

    private void setupGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_gameover, null);
        Button replayButton = (Button) layout.findViewById(R.id.restart_button);
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupTimeTrial();
                newPuzzle();
                gameOverDialog.dismiss();
            }
        });
        Button returnButton = (Button) layout.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameOverDialog.dismiss();
                finish();
            }
        });
        finalScoreView = (TextView) layout.findViewById(R.id.score);
        finalScoreView.setText(getString(R.string.game_over_score, score));
        finalHiScoreView = (TextView) layout.findViewById(R.id.hi_score);
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
        builder.setView(layout);
        builder.setCancelable(false);
        gameOverDialog = builder.create();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeTrialMode) {
            timer.cancel();
        }
        tileManager.removeActivity();
        numAnimatorGen.removeShrinkFinishListener();
        numAnimatorGen = null;
        numShrinkAnimator = null;
        numGrowAnimator = null;
    }

    public void showSettings() {

    }
}
