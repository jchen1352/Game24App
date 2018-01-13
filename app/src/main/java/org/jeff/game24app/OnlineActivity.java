package org.jeff.game24app;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jeff.game24app.game.OnlineGameActivity;

import java.util.Random;

public class OnlineActivity extends BaseActivity {

    public static final String PUZZLE = "puzzle";
    public static final String ROOM_ID = "room_id";
    public static final String IS_HOST_KEY = "is_host";

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private static String uniqueID = getUniqueID();
    private int room_id = -1;
    Random random = new Random();

    private Button createGame, joinGame, submitRoom;
    private EditText roomNumber;
    private TextView roomText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("online");

        createGame = findViewById(R.id.create_game);
        createGame.setVisibility(View.GONE);
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateGameClicked();
            }
        });

        joinGame = findViewById(R.id.join_game);
        joinGame.setVisibility(View.GONE);
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinGameClicked();
            }
        });

        submitRoom = findViewById(R.id.submit_room);
        submitRoom.setVisibility(View.GONE);
        submitRoom.setClickable(false);

        roomNumber = findViewById(R.id.room_number);
        roomNumber.setVisibility(View.GONE);
        roomNumber.setClickable(false);

        roomText = findViewById(R.id.room_text);
        roomText.setVisibility(View.GONE);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        createGame.setVisibility(View.VISIBLE);
        joinGame.setVisibility(View.VISIBLE);
        fadeIn(createGame);
        fadeIn(joinGame);
    }

    private void onCreateGameClicked() {
        submitRoom.setVisibility(View.GONE);
        roomText.setVisibility(View.VISIBLE);
        roomNumber.setVisibility(View.VISIBLE);
        roomNumber.setInputType(InputType.TYPE_NULL);
        createGame.setEnabled(false);
        joinGame.setEnabled(true);

        roomText.setText(getString(R.string.load_room));
        getUniqueRoomID();
    }

    private void onJoinGameClicked() {
        submitRoom.setVisibility(View.VISIBLE);
        roomText.setVisibility(View.VISIBLE);
        roomNumber.setVisibility(View.VISIBLE);
        roomNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        roomNumber.setClickable(true);
        roomNumber.setText("");
        joinGame.setEnabled(false);
        createGame.setEnabled(true);

        roomText.setText(getString(R.string.enter_room));

        submitRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitGameClicked();
            }
        });
    }

    private void onSubmitGameClicked() {
        roomText.setText(getString(R.string.check_room));
        String s = roomNumber.getText().toString();
        room_id = s.equals("") ? 0 : Integer.parseInt(s);
        checkRoom();
    }

    private void checkRoom() {
        DatabaseReference roomP1Reference = reference.child(Integer.toString(room_id)).child("p1");
        ValueEventListener room1Listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    setupJoinOnline();
                } else {
                    roomText.setText(getString(R.string.bad_room));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("dbError: Find_Opponent", databaseError.toException());
            }
        };
        roomP1Reference.addListenerForSingleValueEvent(room1Listener);
    }

    private void setupJoinOnline() {
        reference.child(Integer.toString(room_id)).child("p2").setValue(uniqueID);
        DatabaseReference roomPuzzle = reference.child(Integer.toString(room_id)).child("puzzle");
        ValueEventListener p2PuzzleListener = new ValueEventListener() {
            private boolean changed = false;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && !changed) {
                    changed = true;
                    startGame(false, ((Long) (dataSnapshot.getValue())).intValue());
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
        roomNumber.setText(Integer.toString(room_id));
        roomText.setText(getString(R.string.wait_room));

        DatabaseReference roomP2Reference = reference.child(Integer.toString(room_id)).child("p2");
        reference.child(Integer.toString(room_id)).child("p1").setValue(uniqueID);
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
        Intent intent = new Intent(this, OnlineGameActivity.class);
        if (!isHost) {
            intent.putExtra(PUZZLE, puzzle);
        }
        intent.putExtra(ROOM_ID, room_id);
        intent.putExtra(HomeActivity.GEN_FRAC, getIntent().getBooleanExtra(HomeActivity.GEN_FRAC, false));
        intent.putExtra(IS_HOST_KEY, isHost);
        startActivity(intent);
    }

    private void fadeIn(View v) {
        Animator a = AnimatorInflater.loadAnimator(this, R.animator.grow);
        a.setTarget(v);
        a.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (room_id >= 0) {
            reference.child(Integer.toString(room_id)).setValue(null);
        }
    }
}
