package org.jeff.game24app;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings.Secure;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Operation;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;
import org.jeff.game24app.tiles.TileManager;
import org.jeff.game24app.views.DarkView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GameActivity extends BaseActivity {

    private NumberTile[] numTiles;
    private OperationTile[] opTiles;
    private TileManager tileManager;
    private View numTileGroup, opTileGroup;
    private Animator numShrinkAnimator, numGrowAnimator;
    private DarkView darkView;
    private ImageView star1, star2;
    private boolean timeTrialMode;
    private TextView scoreView;
    private int score;
    private TextView time;
    private static final long TIME_LIMIT = 1000 * 30 * 1; // 5 minutes, shorter when testing
    private static final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private CountDownTimer timer;
    private AlertDialog gameOverDialog;
    private Game24Generator generator;
    private Rational[] nextPuzzle;

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private boolean onlineMode;
    private boolean isHost;
    private String unique_id = Secure.getString(MyApp.getContext().getContentResolver(),
            Secure.ANDROID_ID);
    private int room_id;
    private DatabaseReference winnerReference;
    private boolean firstGame;

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
        star1 = (ImageView) findViewById(R.id.star1);
        star2 = (ImageView) findViewById(R.id.star2);

        Intent intent = getIntent();
        generator = new Game24Generator(intent.getBooleanExtra(HomeActivity.GEN_FRAC, false));
        timeTrialMode = intent.getBooleanExtra(HomeActivity.TIME_TRIAL, false);
        onlineMode = intent.getBooleanExtra(HomeActivity.ONLINE, false);
        isHost = intent.getBooleanExtra(HomeActivity.IS_HOST, false);
        scoreView = (TextView) findViewById(R.id.score);
        time = (TextView) findViewById(R.id.time);
        if (onlineMode) {
            database = FirebaseDatabase.getInstance();
            reference = database.getReference().child("online");
            room_id = intent.getIntExtra(OnlineActivity.ROOM_ID, -1);
            winnerReference = reference.child(Integer.toString(room_id)).child("winner");
            winnerListenerSetup();
        }
        if (!timeTrialMode) {
            scoreView.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
        } else {
            setupTimer();
            setupTimeTrial();
        }
        nextPuzzle = generator.generatePuzzle();
        firstGame = true;

        score = 0;
        scoreView.setText(getResources().getString(R.string.score, score));
    }

    public void onlineHostSetup () {
        Long longPuzzle = Long.valueOf(generator.hashToInt(nextPuzzle));
        reference.child(Integer.toString(room_id)).child("puzzle").setValue(longPuzzle);
    }

    public void onlineNonhostSetup () {
        if (firstGame) {
            firstGame = false;
            nextPuzzle = generator.reverseHash(getIntent().getIntExtra(OnlineActivity.PUZZLE, -1));
        }
    }

    public void winnerListenerSetup () {
        ValueEventListener winnerListener = new ValueEventListener() {
            boolean winnerFound = false;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.getValue() != unique_id) {
                        isHost = false;
                        winnerFound = true;
                        // add a toast
                    }
                } else if (winnerFound) {
                    winnerFound = false;
                    firstGame = false;
                    findNewRound();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Find_Winner", databaseError.toException());
            }
        };
        winnerReference.addValueEventListener(winnerListener);
    }
    private void findNewRound() {
        DatabaseReference roomPuzzle = reference.child(Integer.toString(room_id)).child("puzzle");
        ValueEventListener p2PuzzleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    nextPuzzle = generator.reverseHash(((Long)(dataSnapshot.getValue())).intValue());
                    shrinkNumTiles();
                } else {
                    //return an error
                    //Log.w("dbError: Find_Puzzle2", );
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Find_Puzzle2", databaseError.toException());
            }
        };
        roomPuzzle.addListenerForSingleValueEvent(p2PuzzleListener);
    }


    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        for (NumberTile tile : numTiles) {
            tile.setVisibility(View.VISIBLE);
        }
        setupPuzzle();
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

    private void setupPuzzle() {
        if (onlineMode) {
            if (isHost) {
                onlineHostSetup();
            } else {
                onlineNonhostSetup();
            }
        }
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
        if (onlineMode) {
            onlineWinnerSetup();
        } else {
            nextPuzzle = generator.generatePuzzle();
        }
        shrinkNumTiles();
        if (timeTrialMode) {
            score++;
            scoreView.setText(getResources().getString(R.string.score, score));
        }
    }

    private void onlineWinnerSetup () {
        isHost = true;
        winnerReference.setValue(unique_id);
        nextPuzzle = generator.generatePuzzle();
        onlineHostSetup();
        winnerReference.setValue(null);
    }

    public void restartPuzzle() {
        shrinkNumTiles();
    }

    private void shrinkNumTiles() {
        numShrinkAnimator.start();
    }

    /**
     * Displays a hint for the current numbers, called when clicking the hint button.
     * Also unselects everything.
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

    /**
     * Displays a victory animation when the puzzle is complete.
     * @param tile the tile to animate around
     */
    public void victoryAnim(NumberTile tile) {
        star1.setVisibility(View.VISIBLE);
        star2.setVisibility(View.VISIBLE);
        final int x = tile.getLeft()+numTileGroup.getLeft()+tile.getWidth()/2-star1.getWidth()/2;
        final int y = tile.getTop() + numTileGroup.getTop();
        final int width = tile.getWidth();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                star1.setX(x - value * width/2);
                star1.setY(y + width/2 * (value * (value - 1.3f))); //quadratic curve
                star1.setRotation(-value * 180);
                star2.setX(x + value * width/2);
                star2.setY(y + width/2 * (value * (value - 1.3f))); //quadratic curve
                star2.setRotation(value * 180);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                star1.setVisibility(View.GONE);
                star2.setVisibility(View.GONE);
                newPuzzle();
            }
        });
        animator.setDuration(500);
        animator.start();
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
                darkView.setVisibility(View.GONE);
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
        TextView finalScoreView = (TextView) layout.findViewById(R.id.score);
        finalScoreView.setText(getString(R.string.game_over_score, score));
        TextView finalHiScoreView = (TextView) layout.findViewById(R.id.hi_score);
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
    }

    public void showSettings() {

    }
}
