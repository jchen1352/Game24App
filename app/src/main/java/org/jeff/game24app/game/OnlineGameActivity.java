package org.jeff.game24app.game;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jeff.game24app.OnlineActivity;
import org.jeff.game24app.R;
import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The concrete activity that handles online mode.
 * Features of online mode: synced puzzles between two players, keeps score
 */
public class OnlineGameActivity extends BaseGameActivity {

    private boolean isHost;
    private static String uniqueID = getUniqueID();
    private DatabaseReference winnerReference, puzzleReference, readyReference;
    private boolean restartReady;

    @IntDef({READY, GAME_OVER, RESTART_WAIT, RESTART_FINISH})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    public static final int READY = 0;
    public static final int GAME_OVER = 1;
    public static final int RESTART_WAIT = 2;
    public static final int RESTART_FINISH = 3;

    private int score;
    private TextView scoreView;
    private static final int MAX_SCORE = 5;
    private AlertDialog gameOverDialog;
    private TextView gameOverTitle, gameOverMessage;
    private Button restartButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.time).setVisibility(View.GONE);
        findViewById(R.id.hint_button).setVisibility(View.GONE);
        findViewById(R.id.num_hints).setVisibility(View.GONE);

        scoreView = findViewById(R.id.score);
        score = 0;
        scoreView.setText(getString(R.string.score, score));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Reuse layout from TimedGameActivity
        View layout = getLayoutInflater().inflate(R.layout.dialog_gameover, null);
        gameOverTitle = layout.findViewById(R.id.title);
        gameOverMessage = layout.findViewById(R.id.score);
        layout.findViewById(R.id.hi_score).setVisibility(View.GONE);
        restartButton = layout.findViewById(R.id.restart_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTapSound();
                if (restartReady) {
                    restartReady = false;
                    readyReference.setValue((long) RESTART_FINISH);
                    isHost = false;
                    restartGame();
                } else {
                    restartReady = true;
                    waitForRestart();
                }
                gameOverMessage.setText(R.string.wait_room);
                gameOverMessage.setVisibility(View.VISIBLE);
                v.setEnabled(false);
            }
        });
        Button returnButton = layout.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playTapSound();
                gameOverDialog.dismiss();
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.setView(layout);
        gameOverDialog = builder.create();
        gameOverDialog.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("online");
        String room_id = Integer.toString(intent.getIntExtra(OnlineActivity.ROOM_ID, -1));
        puzzleReference = reference.child(room_id).child("puzzle");
        puzzleReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    int puzzle = ((Long) dataSnapshot.getValue()).intValue();
                    nextPuzzle = Game24Generator.reverseHash(puzzle);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: puzzle change", databaseError.toException());
            }
        });

        winnerReference = reference.child(room_id).child("winner");
        winnerReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (!dataSnapshot.getValue().equals(uniqueID)) {
                        //Opponent is winner
                        isHost = false;
                        //To disable further clicks
                        setIsAnimating(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Find_Winner", databaseError.toException());
            }
        });

        readyReference = reference.child(room_id).child("ready");
        readyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    @State int value = ((Long) dataSnapshot.getValue()).intValue();
                    switch (value) {
                        case READY:
                            shrinkNumTiles();
                            break;
                        case GAME_OVER:
                            showGameOverDialog();
                            break;
                        case RESTART_WAIT:
                            if (!restartReady) {
                                restartReady = true;
                            }
                            break;
                        case RESTART_FINISH:
                            if (restartReady) {
                                restartGame();
                                restartReady = false;
                            }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: ready failed", databaseError.toException());
            }
        });

        if (isHost) {
            //nextPuzzle initialized in getInitialPuzzle
            puzzleReference.setValue((long) Game24Generator.hashToInt(nextPuzzle));
        }
    }

    @Override
    public Rational[] getInitialPuzzle() {
        Intent intent = getIntent();
        isHost = intent.getBooleanExtra(OnlineActivity.IS_HOST_KEY, false);
        if (isHost) {
            return generator.generatePuzzle();
        }
        return Game24Generator.reverseHash(intent.getIntExtra(OnlineActivity.PUZZLE, 0));
    }

    @Override
    public Rational[] getNewPuzzle() {
        return nextPuzzle;
    }

    @Override
    public void victory(NumberTile tile) {
        super.victory(tile);

        winnerReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                String id = mutableData.getValue(String.class);
                if (id == null) {
                    //Update winner if no winner
                    mutableData.setValue(uniqueID);
                    return Transaction.success(mutableData);
                }
                //Don't update if not winner
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.w("victory", databaseError.toException());
                }
                String id = dataSnapshot.getValue(String.class);
                if (id == null) {

                    return;
                }
                if (id.equals(uniqueID)) {

                    incrementScore();
                    return;
                }

            }
        });

        final int possiblePuzzle = Game24Generator.hashToInt(generator.generatePuzzle());
        puzzleReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue((long) possiblePuzzle);
                    return Transaction.success(mutableData);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    @Override
    protected void startNewPuzzle() {
        super.startNewPuzzle();
        readyReference.setValue(READY);
    }

    @Override
    protected void setupPuzzle() {
        super.setupPuzzle();
        winnerReference.setValue(null);
        puzzleReference.setValue(null);
        readyReference.setValue(null);
    }

    private void incrementScore() {
        score++;
        scoreView.setText(getString(R.string.score, score));
        if (score >= MAX_SCORE) {
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        readyReference.setValue(GAME_OVER);
        boolean win = score >= MAX_SCORE;
        gameOverTitle.setText(win ? getString(R.string.online_win) : getString(R.string.online_lose));
        gameOverMessage.setVisibility(View.GONE);
        restartButton.setEnabled(true);
        gameOverDialog.show();

        SharedPreferences preferences = getSharedPreferences(PREFS, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(NUM_PLAYED_PREF, preferences.getInt(NUM_PLAYED_PREF, 0) + 1);
        if (win) {
            editor.putInt(NUM_WON_PREF, preferences.getInt(NUM_WON_PREF, 0) + 1);
        }
        editor.apply();
    }

    private void waitForRestart() {
        readyReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue((long) RESTART_WAIT);

                    return Transaction.success(mutableData);
                }
                int value = ((Long) mutableData.getValue()).intValue();
                if (value == RESTART_WAIT) {
                    mutableData.setValue((long) RESTART_FINISH);

                    return Transaction.success(mutableData);
                }
                mutableData.setValue((long) RESTART_WAIT);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.w("restart", databaseError.toException());
                }
                if (dataSnapshot.getValue() == null) {

                    return;
                }
                int value = ((Long) dataSnapshot.getValue()).intValue();
                if (value == RESTART_WAIT) {

                    isHost = true;
                }
                if (value == RESTART_FINISH) {

                    restartReady = false;
                    isHost = false;
                    restartGame();
                }

            }
        });
    }

    private void restartGame() {
        gameOverDialog.dismiss();
        score = 0;
        scoreView.setText(getString(R.string.score, score));
        if (isHost) {
            nextPuzzle = generator.generatePuzzle();
            long puzzle = (long) Game24Generator.hashToInt(nextPuzzle);
            puzzleReference.setValue(puzzle, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.w("restart", databaseError.toException());
                    } else {

                        startNewPuzzle();
                    }
                }
            });
        }
    }
}
