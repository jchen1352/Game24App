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
    private NumberTile[] numTiles;
    private OperationTile tileAdd, tileSub, tileMul, tileDiv;
    private OperationTile[] opTiles;
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
        numTiles = new NumberTile[] {tile0, tile1, tile2, tile3};
        tileAdd = (OperationTile) findViewById(R.id.tile_add);
        tileSub = (OperationTile) findViewById(R.id.tile_subtract);
        tileMul = (OperationTile) findViewById(R.id.tile_multiply);
        tileDiv = (OperationTile) findViewById(R.id.tile_divide);
        opTiles = new OperationTile[] {tileAdd, tileSub, tileMul, tileDiv};
        tileManager = new TileManager(this);
        for (NumberTile tile : numTiles) {
            tile.setOnClickListener(tileManager.getNumListener());
        }

        for (OperationTile tile : opTiles) {
            tile.setOnClickListener(tileManager.getOpListener());
        }
    }

    private void setupPuzzle(Rational[] puzzle) {
        for (int i = 0; i < numTiles.length; i++) {
            numTiles[i].setExists(true);
            numTiles[i].unselect();
            numTiles[i].setValue(puzzle[i]);
        }
        for (OperationTile tile : opTiles) {
            tile.unselect();
        }
        tileManager.reset();
    }

    public void newPuzzle() {
        setupPuzzle(generator.generateIntPuzzle());
    }

    public void restartPuzzle() {
        setupPuzzle(generator.restartPuzzle());
    }

}
