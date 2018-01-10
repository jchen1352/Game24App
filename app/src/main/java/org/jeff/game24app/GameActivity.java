package org.jeff.game24app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Operation;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.HintManager;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;
import org.jeff.game24app.tiles.TileManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The main activity responsible for the game. Handles both time trial
 * and free play mode.
 */
public class GameActivity extends BaseActivity implements RewardedVideoAdListener {

    private NumberTile[] numTiles;
    private OperationTile[] opTiles;
    private TileManager tileManager;
    private HintManager hintManager;
    private View numTileGroup;
    private Animator numShrinkAnimator, numGrowAnimator, victoryAnimator, shinyAnimator;
    private ImageView shiny;
    private boolean timeTrialMode;
    private TextView scoreView, finalScoreView, finalHiScoreView;
    private int score;
    private TextView time;
    private static final long TIME_LIMIT = 1000 * 30 * 1; // 5 minutes, shorter when testing
    private static final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private CountDownTimer timer;
    private TextView numHintsView;
    private int numHints;
    private TextView hintMessage;
    private Button hintButton, moreHints;
    private AlertDialog hintDialog, gameOverDialog;
    private Game24Generator generator;
    private boolean fracMode;
    private Rational[] nextPuzzle;
    /**
     * Shared preference key for saved classic puzzle
     */
    private static final String CLASSIC_PREF = "puzzle_classic_pref";
    /**
     * Shared preference key for saved fractional puzzle
     */
    private static final String FRAC_PREF = "puzzle_frac_pref";
    private static final String HINT_PREF = "hint_pref";
    private static final int MAX_HINTS = 5;
    private static final boolean HAX_MODE = false; //hint button directly shows hint (for debug)

    private RewardedVideoAd ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setupTiles();
        hintManager = new HintManager(this);

        ImageButton restartButton = (ImageButton) findViewById(R.id.restart_button);
        ImageButton hintButton = (ImageButton) findViewById(R.id.hint_button);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTapSound();
                restartPuzzle();
            }
        });
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTapSound();
                if (!HAX_MODE) {
                    hintDialog.show();
                } else {
                    numHints++;
                    showHint();
                }
            }
        });

        setupGameOverDialog();

        numHintsView = (TextView) findViewById(R.id.num_hints);
        numHints = getSharedPreferences(PREFS, 0).getInt(HINT_PREF, MAX_HINTS);
        setupHintDialog();

        shiny = (ImageView) findViewById(R.id.shiny);
        shinyAnimator = AnimatorInflater.loadAnimator(this, R.animator.shiny);
        shinyAnimator.setTarget(shiny);
        victoryAnimator = AnimatorInflater.loadAnimator(this, R.animator.victory);
        victoryAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                shiny.setVisibility(View.GONE);
                newPuzzle();
            }
        });

        Intent intent = getIntent();
        fracMode = intent.getBooleanExtra(HomeActivity.GEN_FRAC, false);
        generator = new Game24Generator(fracMode);
        timeTrialMode = intent.getBooleanExtra(HomeActivity.TIME_TRIAL, false);
        scoreView = (TextView) findViewById(R.id.score);
        time = (TextView) findViewById(R.id.time);
        if (!timeTrialMode) {
            scoreView.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            getSavedPuzzle();
        } else {
            setupTimer();
            setupTimeTrial();
            //Manually set score to 0 because not calling newPuzzle and score starts at -1
            score = 0;
            scoreView.setText(getResources().getString(R.string.score, score));
            nextPuzzle = generator.generatePuzzle();
        }

        ad = MobileAds.getRewardedVideoAdInstance(this);
        ad.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        for (NumberTile tile : numTiles) {
            tile.setVisibility(View.VISIBLE);
        }
        setupPuzzle();
    }

    private void getSavedPuzzle() {
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        /*
         * Since a puzzle consists of 4 rationals with numerator and denominator at most 10,
         * the puzzle can be encoded in a single integer. The first 4 bits are the 1st
         * numerator, the second 4 bits are the 1st denominator, and so on.
         */
        int puzzleEncoded = preferences.getInt(fracMode ? FRAC_PREF : CLASSIC_PREF, 0);
        if (puzzleEncoded != 0) {
            nextPuzzle = new Rational[4];
            for (int i = 0; i < 4; i++) {
                int numerator = puzzleEncoded >> (28 - (i * 8)) & 0xF;
                int denominator = puzzleEncoded >> (24 - (i * 8)) & 0xF;
                nextPuzzle[i] = new Rational(numerator, denominator);
            }
        } else {
            nextPuzzle = generator.generatePuzzle();
        }
    }

    private void savePuzzle() {
        //See getSavedPuzzle for how a puzzle is encoded
        int puzzleEncoded = 0;
        for (int i = 0; i < 4; i++) {
            puzzleEncoded <<= 4;
            puzzleEncoded |= nextPuzzle[i].getNumerator();
            puzzleEncoded <<= 4;
            puzzleEncoded |= nextPuzzle[i].getDenominator();
        }
        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(fracMode ? FRAC_PREF : CLASSIC_PREF, puzzleEncoded);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!timeTrialMode) {
            savePuzzle();
        }
    }

    private void setupTiles() {
        NumberTile tile0 = (NumberTile) findViewById(R.id.tile0);
        NumberTile tile1 = (NumberTile) findViewById(R.id.tile1);
        NumberTile tile2 = (NumberTile) findViewById(R.id.tile2);
        NumberTile tile3 = (NumberTile) findViewById(R.id.tile3);
        numTiles = new NumberTile[]{tile0, tile1, tile2, tile3};
        OperationTile tileAdd = (OperationTile) findViewById(R.id.tile_add);
        OperationTile tileSub = (OperationTile) findViewById(R.id.tile_subtract);
        OperationTile tileMul = (OperationTile) findViewById(R.id.tile_multiply);
        OperationTile tileDiv = (OperationTile) findViewById(R.id.tile_divide);
        opTiles = new OperationTile[]{tileAdd, tileSub, tileMul, tileDiv};
        numTileGroup = findViewById(R.id.num_tile_group);
        numShrinkAnimator = AnimatorInflater.loadAnimator(this, R.animator.shrink);
        numShrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setupPuzzle();
            }
        });
        numShrinkAnimator.setTarget(numTileGroup);
        numGrowAnimator = AnimatorInflater.loadAnimator(this, R.animator.grow);
        numGrowAnimator.setTarget(numTileGroup);
        tileManager = new TileManager(this);
        for (NumberTile tile : numTiles) {
            tile.setOnClickListener(tileManager.getNumListener());
            tile.setVisibility(View.GONE);
        }
        for (OperationTile tile : opTiles) {
            tile.setOnClickListener(tileManager.getOpListener());
        }
    }

    /**
     * Sets up the stored puzzle by setting the tiles to the right values.
     */
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

    /**
     * Generates and stores a new puzzle, then sets up the puzzle.
     */
    public void newPuzzle() {
        nextPuzzle = generator.generatePuzzle();
        shrinkNumTiles();
        if (timeTrialMode) {
            score++;
            scoreView.setText(getResources().getString(R.string.score, score));
        }
    }

    /**
     * Restarts by setting up the current puzzle.
     */
    public void restartPuzzle() {
        shrinkNumTiles();
    }

    private void shrinkNumTiles() {
        numShrinkAnimator.start();
    }

    public void setupHintDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_hint, null);
        hintMessage = (TextView) layout.findViewById(R.id.hint_message);
        hintMessage.setText(getString(R.string.hint_message, numHints, MAX_HINTS));
        hintButton = (Button) layout.findViewById(R.id.use_hint);
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numHints > 0) {
                    playTapSound();
                    hintDialog.dismiss();
                    showHint();
                }
            }
        });
        moreHints = (Button) layout.findViewById(R.id.more_hints);
        moreHints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numHints < MAX_HINTS) {
                    playTapSound();
                    if (ad.isLoaded()) {
                        ad.show();
                    }
                }
            }
        });
        onNumHintsChanged();
        builder.setView(layout);
        hintDialog = builder.create();
    }

    /**
     * Displays a hint for the current numbers, called when clicking the hint button.
     * Also unselects everything.
     */
    public void showHint() {
        numHints--;
        onNumHintsChanged();
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
        hintManager.startHint(hintNum0, hintOp, hintNum1);
    }

    private void onNumHintsChanged() {
        if (numHintsView != null) {
            numHintsView.setText(getString(R.string.num_hints, numHints));
        }
        if (hintMessage != null) {
            hintMessage.setText(getString(R.string.hint_message, numHints, MAX_HINTS));
        }
        if (hintButton != null) {
            hintButton.setEnabled(numHints > 0);
        }
        if (moreHints != null) {
            moreHints.setEnabled(numHints < MAX_HINTS);
        }
        getSharedPreferences(PREFS, 0).edit().putInt(HINT_PREF, numHints).apply();
    }

    /**
     * Displays a victory animation when the puzzle is complete.
     *
     * @param tile the tile to animate around
     */
    public void victory(NumberTile tile) {
        playSuccessSound();
        float tileWidth = tile.getWidth();
        float x = tile.getLeft() + numTileGroup.getLeft() - tileWidth * .25f;
        float y = tile.getTop() + numTileGroup.getTop() - tileWidth * .25f;
        float width = tileWidth * 1.5f;
        shiny.setVisibility(View.VISIBLE);
        shiny.setX(x);
        shiny.setY(y);
        shiny.getLayoutParams().width = (int) width;
        shiny.getLayoutParams().height = (int) width;
        shiny.requestLayout();
        shinyAnimator.start();
        victoryAnimator.setTarget(tile);
        victoryAnimator.start();
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
                showGameOverDialog();
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
                playTapSound();
                hintManager.reset();
                setupTimeTrial();
                newPuzzle();
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
    }

    private void showGameOverDialog() {
        if (hintDialog.isShowing()) {
            hintDialog.dismiss();
        }
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
        if (timeTrialMode) {
            timer.cancel();
        }
        //There is currently a bug where ad.destroy makes it not work when activity restarts
        //ad.destroy(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ad.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ad.resume(this);
    }

    private void loadRewardedVideoAd() {
        if (!ad.isLoaded()) {
            ad.loadAd(BaseApplication.AD_UNIT_ID, new AdRequest.Builder().build());
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Log.d("GameActivity", "ad loaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Log.d("GameActivity", "ad opened");
    }

    @Override
    public void onRewardedVideoStarted() {
        Log.d("GameActivity", "ad started");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Log.d("GameActivity", "ad closed");
        //preload next ad
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        numHints++;
        onNumHintsChanged();
        Log.d("GameActivity", "ad rewarded");
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Log.d("GameActivity", "ad left app");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Log.d("GameActivity", "ad failed to load");
    }
}
