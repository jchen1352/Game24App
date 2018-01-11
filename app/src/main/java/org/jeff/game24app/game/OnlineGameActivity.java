package org.jeff.game24app.game;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jeff.game24app.BaseApplication;
import org.jeff.game24app.OnlineActivity;
import org.jeff.game24app.solver.Game24Generator;

/**
 * The concrete activity that handles online mode.
 * Features of online mode: synced puzzles between two players, keeps score
 */
public class OnlineGameActivity extends BaseGameActivity {

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private boolean onlineMode;
    private boolean isHost;
    private String unique_id = Settings.Secure.getString(BaseApplication.getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);
    private int room_id;
    private DatabaseReference winnerReference;
    private boolean firstGame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("online");
        room_id = intent.getIntExtra(OnlineActivity.ROOM_ID, -1);
        winnerReference = reference.child(Integer.toString(room_id)).child("winner");
        winnerListenerSetup();
        firstGame = true;
    }

    public void onlineHostSetup() {
        Long longPuzzle = Long.valueOf(generator.hashToInt(nextPuzzle));
        reference.child(Integer.toString(room_id)).child("puzzle").setValue(longPuzzle);
    }

    public void onlineNonhostSetup() {
        if (firstGame) {
            firstGame = false;
            nextPuzzle = generator.reverseHash(getIntent().getIntExtra(OnlineActivity.PUZZLE, -1));
        }
    }

    public void winnerListenerSetup() {
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
                    nextPuzzle = Game24Generator.reverseHash(((Long) (dataSnapshot.getValue())).intValue());
                    numShrinkAnimator.start();
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

    private void onlineWinnerSetup() {
        isHost = true;
        winnerReference.setValue(unique_id);
        nextPuzzle = generator.generatePuzzle();
        onlineHostSetup();
        winnerReference.setValue(null);
    }

    @Override
    protected void setupNewPuzzle() {
        super.setupNewPuzzle();
        onlineWinnerSetup();

    }

    @Override
    protected void setupPuzzle() {
        onlineHostSetup();
        onlineNonhostSetup();
        super.setupPuzzle();
    }
}
