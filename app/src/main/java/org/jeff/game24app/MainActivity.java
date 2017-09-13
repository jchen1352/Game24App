package org.jeff.game24app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;
import org.jeff.game24app.tiles.TileManager;

public class MainActivity extends AppCompatActivity {

    private NumberTile tile0, tile1, tile2, tile3;
    private OperationTile tileAdd, tileSub, tileMul, tileDiv;
    private TileManager tileManager;
    private ImageButton newButton, restartButton, hintButton;
    private Game24Generator generator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupTiles();

        generator = new Game24Generator();

        newButton = (ImageButton) findViewById(R.id.new_button);
        restartButton = (ImageButton) findViewById(R.id.restart_button);
        hintButton = (ImageButton) findViewById(R.id.hint_button);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPuzzle();
            }
        });

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartPuzzle();
            }
        });

        newPuzzle();
    }

    private void setupTiles() {
        tile0 = (NumberTile) findViewById(R.id.tile0);
        tile1 = (NumberTile) findViewById(R.id.tile1);
        tile2 = (NumberTile) findViewById(R.id.tile2);
        tile3 = (NumberTile) findViewById(R.id.tile3);
        tileAdd = (OperationTile) findViewById(R.id.tile_add);
        tileSub = (OperationTile) findViewById(R.id.tile_subtract);
        tileMul = (OperationTile) findViewById(R.id.tile_multiply);
        tileDiv = (OperationTile) findViewById(R.id.tile_divide);
        tileManager = new TileManager();
        tile0.setOnClickListener(tileManager.getNumListener());
        tile1.setOnClickListener(tileManager.getNumListener());
        tile2.setOnClickListener(tileManager.getNumListener());
        tile3.setOnClickListener(tileManager.getNumListener());
        tileAdd.setOnClickListener(tileManager.getOpListener());
        tileSub.setOnClickListener(tileManager.getOpListener());
        tileMul.setOnClickListener(tileManager.getOpListener());
        tileDiv.setOnClickListener(tileManager.getOpListener());
    }

    private void setupPuzzle(Rational[] puzzle) {
        tile0.setExists(true);
        tile1.setExists(true);
        tile2.setExists(true);
        tile3.setExists(true);
        tile0.unselect();
        tile1.unselect();
        tile2.unselect();
        tile3.unselect();
        tile0.setValue(puzzle[0]);
        tile1.setValue(puzzle[1]);
        tile2.setValue(puzzle[2]);
        tile3.setValue(puzzle[3]);
        tileAdd.unselect();
        tileSub.unselect();
        tileMul.unselect();
        tileDiv.unselect();
        tileManager.reset();
    }

    private void newPuzzle() {
        setupPuzzle(generator.generateIntPuzzle());
    }

    private void restartPuzzle() {
        setupPuzzle(generator.restartPuzzle());
    }
}
