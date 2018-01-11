package org.jeff.game24app;


import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.Random;

public class OnlineActivity extends BaseActivity {

    public static final String PUZZLE = "puzzle";
    public static final String ROOM_ID = "room_id";

    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String unique_id = Settings.Secure.getString(BaseApplication.getContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);
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

        createGame = (Button) findViewById(R.id.create_game);
        createGame.setVisibility(View.GONE);
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateGameClicked();
            }
        });

        joinGame = (Button) findViewById(R.id.join_game);
        joinGame.setVisibility(View.GONE);
        joinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onJoinGameClicked();
            }
        });

        submitRoom = (Button) findViewById(R.id.submit_room);
        submitRoom.setVisibility(View.GONE);
        submitRoom.setClickable(false);

        roomNumber = (EditText) findViewById(R.id.room_number);
        roomNumber.setVisibility(View.GONE);
        roomNumber.setClickable(false);

        roomText = (TextView) findViewById(R.id.room_text);
        roomText.setVisibility(View.GONE);

        fadeIn(createGame);
        fadeIn(joinGame);
    }

    private void onCreateGameClicked() {
        fadeOut(createGame);
        fadeOut(joinGame);
        roomText.setVisibility(View.VISIBLE);
        roomNumber.setVisibility(View.VISIBLE);
        roomNumber.setInputType(InputType.TYPE_NULL);

        roomText.setText(getString(R.string.load_room));
        getUniqueRoomID();
    }

    private void onJoinGameClicked() {
        fadeOut(createGame);
        fadeOut(joinGame);
        fadeIn(submitRoom);
        roomText.setVisibility(View.VISIBLE);
        roomNumber.setVisibility(View.VISIBLE);
        roomNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        roomNumber.setClickable(true);

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
        room_id = Integer.parseInt(roomNumber.getText().toString());
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
        reference.child(Integer.toString(room_id)).child("p2").setValue(unique_id);
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

    private void fadeIn(final View v) {
        Animator a = AnimatorInflater.loadAnimator(this, R.animator.grow);
        a.setTarget(v);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setClickable(true);
            }
        });
        a.start();
    }

    private void fadeOut(final View v) {
        Animator a = AnimatorInflater.loadAnimator(this, R.animator.shrink);
        a.setTarget(v);
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                v.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setVisibility(View.INVISIBLE);
            }
        });
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
