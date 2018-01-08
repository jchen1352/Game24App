package org.jeff.game24app;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jeff.game24app.solver.Rational;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OnlineActivity extends BaseActivity {

    public static final String PUZZLE = "puzzle";
    public static final String ROOM_ID = "room_id";

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String unique_id = Settings.Secure.getString(MyApp.getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);
    private int room_id;
    private boolean makeGame;
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeGame = getIntent().getBooleanExtra(HomeActivity.GEN_FRAC, true);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("online");
        if (makeGame) {
            getUniqueRoomID();
        } else {
            //get user to input room_id
            setupJoinOnline();
        }
    }
    private void setupJoinOnline() {
        reference.child(Integer.toString(room_id)).child("p2").setValue(unique_id);
        DatabaseReference roomPuzzle = reference.child(Integer.toString(room_id)).child("puzzle");
        ValueEventListener p2PuzzleListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    startGame(false, ((Long)(dataSnapshot.getValue())).intValue());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Find_Puzzle", databaseError.toException());
            }
        };
        roomPuzzle.addValueEventListener(p2PuzzleListener);
    }

    private void setupMakeOnline() {
        DatabaseReference roomP2Reference = reference.child(Integer.toString(room_id)).child("p2");
        reference.child(Integer.toString(room_id)).child("p1").setValue(unique_id);
        //reference.child(Integer.toString(room_id)).child("squares").setValue(Arrays.asList(nextPuzzle));
        ValueEventListener room2Listener = new ValueEventListener() {
            private boolean changed = false;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && !changed) {
                    changed = true;
                    startGame(true, 0);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Find_Opponent", databaseError.toException());
            }
        };
        roomP2Reference.addValueEventListener(room2Listener);
    }

    private void getUniqueRoomID() {
        room_id = 100000 + random.nextInt(900000);
        DatabaseReference roomP1Reference = reference.child(Integer.toString(room_id)).child("p1");
        ValueEventListener roomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    setupMakeOnline();
                } else {
                    getUniqueRoomID();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Room_ID", databaseError.toException());
            }
        };
        roomP1Reference.addListenerForSingleValueEvent(roomListener);
    }

    private void startGame(boolean isHost, int puzzle) {
        Intent intent = new Intent(this, GameActivity.class);
        if (!isHost) {
            intent.putExtra(PUZZLE, puzzle);
        }
        intent.putExtra(ROOM_ID, room_id);
        intent.putExtra(HomeActivity.GEN_FRAC, getIntent().getBooleanExtra(HomeActivity.GEN_FRAC, false));
        intent.putExtra(HomeActivity.TIME_TRIAL, false);
        intent.putExtra(HomeActivity.ONLINE, true);
        intent.putExtra(HomeActivity.IS_HOST, isHost);
        startActivity(intent);
    }

}
