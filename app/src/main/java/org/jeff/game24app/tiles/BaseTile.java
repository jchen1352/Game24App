package org.jeff.game24app.tiles;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.jeff.game24app.R;
import org.jeff.game24app.animations.ViewAnimatorGen;

/**
 * The base View that encompasses the tiles in the game.
 */
public abstract class BaseTile extends View {

    private Rect boundingRect;
    private boolean isActive;
    private Animator bobbleAnimator;
    private Drawable tile;

    public BaseTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        boundingRect = new Rect();
        isActive = false;
        bobbleAnimator = new ViewAnimatorGen(this).getBobbleAnimator();
        tile = getResources().getDrawable(R.drawable.ic_tile, null);
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Change isActive and adjust drawing appropriately
     */
    public void toggle() {
        if (isActive) {
            isActive = false;
            bobbleAnimator.end();
            //Starting and canceling necessary to reset view
            bobbleAnimator.start();
            bobbleAnimator.cancel();
        } else {
            isActive = true;
            bobbleAnimator.start();
        }
        invalidate();
    }

    public void unselect() {
        if (isActive) {
            toggle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        tile.setBounds(0, 0, getWidth(), getHeight());
        tile.draw(canvas);
    }
}
