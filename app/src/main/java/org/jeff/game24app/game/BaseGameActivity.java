package org.jeff.game24app.game;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.jeff.game24app.BaseActivity;
import org.jeff.game24app.HomeActivity;
import org.jeff.game24app.R;
import org.jeff.game24app.solver.Game24Generator;
import org.jeff.game24app.solver.Rational;
import org.jeff.game24app.tiles.NumberTile;
import org.jeff.game24app.tiles.OperationTile;
import org.jeff.game24app.tiles.TileManager;

/**
 * Class that provides basic functionality for the game.
 */
public abstract class BaseGameActivity extends BaseActivity {

    protected NumberTile[] numTiles;
    protected OperationTile[] opTiles;
    private TileManager tileManager;
    private View numTileGroup;
    private Animator numShrinkAnimator, numGrowAnimator, victoryAnimator, shinyAnimator;
    private ImageView shiny;
    private boolean isAnimating;
    protected Game24Generator generator;
    protected boolean fracMode;
    protected Rational[] nextPuzzle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        NumberTile tile0 = (NumberTile) findViewById(R.id.tile0);
        NumberTile tile1 = (NumberTile) findViewById(R.id.tile1);
        NumberTile tile2 = (NumberTile) findViewById(R.id.tile2);
        NumberTile tile3 = (NumberTile) findViewById(R.id.tile3);
        numTiles = new NumberTile[]{tile0, tile1, tile2, tile3};
        OperationTile tileAdd = (OperationTile) findViewById(R.id.tile_add);
        OperationTile tileSub = (OperationTile) findViewById(R.id.tile_subtract);
        OperationTile tileMul = (OperationTile) findViewById(R.id.tile_multiply);
        OperationTile tileDiv = (OperationTile) findViewById(R.id.tile_divide);
        opTiles = new OperationTile[]{tileAdd, tileSub, tileMul, tileDiv};

        tileManager = new TileManager(this);
        for (NumberTile tile : numTiles) {
            tile.setOnClickListener(tileManager.getNumListener());
            tile.setVisibility(View.GONE);
        }
        for (OperationTile tile : opTiles) {
            tile.setOnClickListener(tileManager.getOpListener());
        }

        numTileGroup = findViewById(R.id.num_tile_group);
        numShrinkAnimator = AnimatorInflater.loadAnimator(this, R.animator.shrink);
        numShrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setupPuzzle();
            }
        });
        numShrinkAnimator.setTarget(numTileGroup);
        numGrowAnimator = AnimatorInflater.loadAnimator(this, R.animator.grow);
        numGrowAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimating = false;
            }
        });
        numGrowAnimator.setTarget(numTileGroup);

        ImageButton restartButton = (ImageButton) findViewById(R.id.restart_button);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnimating) return;
                playTapSound();
                numShrinkAnimator.start();
            }
        });

        shiny = (ImageView) findViewById(R.id.shiny);
        shinyAnimator = AnimatorInflater.loadAnimator(this, R.animator.shiny);
        shinyAnimator.setTarget(shiny);
        victoryAnimator = AnimatorInflater.loadAnimator(this, R.animator.victory);
        victoryAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                shiny.setVisibility(View.GONE);
                startNewPuzzle();
            }
        });

        isAnimating = true;

        Intent intent = getIntent();
        fracMode = intent.getBooleanExtra(HomeActivity.GEN_FRAC, false);
        generator = new Game24Generator(fracMode);

        nextPuzzle = getInitialPuzzle();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        for (NumberTile tile : numTiles) {
            tile.setVisibility(View.VISIBLE);
        }
        setupPuzzle();
    }

    /**
     * Sets up the stored puzzle by setting the tiles to the right values.
     */
    protected void setupPuzzle() {
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

    /**
     * Returns what the next puzzle should be.
     *
     * @return
     */
    public Rational[] getNewPuzzle() {
        return generator.generatePuzzle();
    }

    /**
     * Returns what the first puzzle should be.
     *
     * @return
     */
    public Rational[] getInitialPuzzle() {
        return getNewPuzzle();
    }

    protected void startNewPuzzle() {
        nextPuzzle = getNewPuzzle();
        numShrinkAnimator.start();
    }

    protected void shrinkNumTiles() {
        numShrinkAnimator.start();
    }

    /**
     * Displays a victory animation when the puzzle is complete.
     *
     * @param tile the tile to animate around
     */
    public void victory(NumberTile tile) {
        isAnimating = true;
        playSuccessSound();
        float tileWidth = tile.getWidth();
        float x = tile.getLeft() + numTileGroup.getLeft() - tileWidth * .25f;
        float y = tile.getTop() + numTileGroup.getTop() - tileWidth * .25f;
        float width = tileWidth * 1.5f;
        shiny.setVisibility(View.VISIBLE);
        shiny.setX(x);
        shiny.setY(y);
        shiny.getLayoutParams().width = (int) width;
        shiny.getLayoutParams().height = (int) width;
        shiny.requestLayout();
        shinyAnimator.start();
        victoryAnimator.setTarget(tile);
        victoryAnimator.start();
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setIsAnimating(boolean b) {
        isAnimating = b;
    }
}
