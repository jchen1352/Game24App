package org.jeff.game24app.game;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jeff.game24app.BaseApplication;
import org.jeff.game24app.HomeActivity;
import org.jeff.game24app.OnlineActivity;
import org.jeff.game24app.R;
import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;

/**
 * The concrete activity that handles online mode.
 * Features of online mode: synced puzzles between two players, keeps score
 */
public class OnlineGameActivity extends BaseGameActivity {

    private boolean isHost;
    private String unique_id = Settings.Secure.getString(BaseApplication.getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);
    private String room_id;
    private DatabaseReference winnerReference, puzzleReference, readyReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.time).setVisibility(View.GONE);
        findViewById(R.id.score).setVisibility(View.GONE);
        findViewById(R.id.hint_button).setVisibility(View.GONE);
        findViewById(R.id.num_hints).setVisibility(View.GONE);

        Intent intent = getIntent();
        Log.d("OnlineGame", isHost ? "isHost" : "not isHost");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference().child("online");
        room_id = Integer.toString(intent.getIntExtra(OnlineActivity.ROOM_ID, -1));
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
                    if (dataSnapshot.getValue() != unique_id) {
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
                    if ((boolean) dataSnapshot.getValue()) {
                        shrinkNumTiles();
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
        isHost = intent.getBooleanExtra(HomeActivity.IS_HOST, false);
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
                    mutableData.setValue(unique_id);
                    return Transaction.success(mutableData);
                }
                //Don't update if not winner
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getValue(String.class);
                if (id == null) {
                    Log.d("OnlineGameActivity", "Winner updated to null");
                    return;
                }
                if (id.equals(unique_id)) {
                    Log.d("OnlineGameActivity", "Winner updated to this");
                    return;
                }
                Log.d("OnlineGameActivity", "Winner updated to other");
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
        readyReference.setValue(true);
    }

    @Override
    protected void setupPuzzle() {
        super.setupPuzzle();
        winnerReference.setValue(null);
        puzzleReference.setValue(null);
        readyReference.setValue(false);
    }
}
