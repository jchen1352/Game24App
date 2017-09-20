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
import org.jeff.game24app.animations.TileAnimatorFactory;

/**
 * The base View that encompasses the tiles in the game.
 */
public abstract class BaseTile extends View {

    private Rect boundingRect;
    protected boolean isSelected;
    private Animator animator;

    public BaseTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        boundingRect = new Rect();
        isSelected = false;
        animator = new TileAnimatorFactory(this).getAnimator();
    }

    /**
     * Change isSelected and adjust drawing appropriately
     */
    public void toggle() {
        if (isSelected) {
            isSelected = false;
            animator.end();
            //Starting and canceling necessary to reset view
            animator.start();
            animator.cancel();
        } else {
            isSelected = true;
            animator.start();
        }
        invalidate();
    }

    public void unselect() {
        if (isSelected) {
            toggle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable tile = getResources().getDrawable(R.drawable.ic_tile, null);
        tile.setBounds(0, 0, getWidth(), getHeight());
        tile.draw(canvas);
    }

}
