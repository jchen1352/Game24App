package org.jeff.game24app.game;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jeff.game24app.R;
import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Operation;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.HintManager;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass of GameActivity that supports hints
 */
public abstract class HintGameActivity extends BaseGameActivity {

    protected int numHints;
    protected HintManager hintManager;
    private AlertDialog hintDialog;
    private TextView numHintsView;
    private TextView hintMessage;
    private Button showHintButton, moreHintsButton;
    protected ImageButton hintButton;

    private static final boolean HAX_MODE = false; //hint button directly shows hint (for debug)

    private static final String HINT_PREF = "hint_pref";
    private static final int MAX_HINTS = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hintManager = new HintManager(this);

        hintButton = (ImageButton) findViewById(R.id.hint_button);
        hintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating()) return;
                playTapSound();
                if (!HAX_MODE) {
                    onHintClicked();
                } else {
                    numHints++;
                    showHint();
                }
            }
        });

        numHintsView = (TextView) findViewById(R.id.num_hints);
        numHints = getSharedPreferences(PREFS, 0).getInt(HINT_PREF, MAX_HINTS);
        numHints = 5;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_hint, null);
        hintMessage = (TextView) layout.findViewById(R.id.hint_message);
        hintMessage.setText(getString(R.string.hint_message, numHints, MAX_HINTS));
        showHintButton = (Button) layout.findViewById(R.id.use_hint);
        showHintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numHints > 0) {
                    playTapSound();
                    hintDialog.dismiss();
                    showHint();
                }
            }
        });
        moreHintsButton = (Button) layout.findViewById(R.id.more_hints);
        moreHintsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numHints < MAX_HINTS) {
                    playTapSound();
                    onMoreHintsClicked();
                }
            }
        });
        builder.setView(layout);
        hintDialog = builder.create();

        onNumHintsChanged();
    }

    protected void showHint() {
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
        for (OperationTile tile : opTiles) {
            if (tile.isActive()) {
                tile.performClick();
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
            if (tile.getOp() == hint.getOp()) {
                hintOp = tile;
            }
        }
        hintManager.startHint(hintNum0, hintOp, hintNum1);
    }

    protected void onNumHintsChanged() {
        if (numHintsView != null) {
            numHintsView.setText(getString(R.string.num_hints, numHints));
        }
        if (hintMessage != null) {
            hintMessage.setText(getString(R.string.hint_message, numHints, MAX_HINTS));
        }
        if (showHintButton != null) {
            showHintButton.setEnabled(numHints > 0);
        }
        if (moreHintsButton != null) {
            moreHintsButton.setEnabled(numHints < MAX_HINTS);
        }
        getSharedPreferences(PREFS, 0).edit().putInt(HINT_PREF, numHints).apply();
    }

    protected abstract void onHintClicked();

    protected abstract void onMoreHintsClicked();

    protected void showHintDialog() {
        hintDialog.show();
    }

    protected void dismissHintDialog() {
        if (hintDialog != null && hintDialog.isShowing()) {
            hintDialog.dismiss();
        }
    }
}
