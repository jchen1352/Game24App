package org.jeff.game24app.animations;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import org.jeff.game24app.tiles.BaseTile;

/**
 * A class that creates an animator for a BaseTile.
 */
public class TileAnimatorFactory {

    private BaseTile tile;
    private static final int DURATION = 600;

    public TileAnimatorFactory(BaseTile t) {
        tile = t;
    }

    /**
     * Creates an animator for a Tile that displays when selected.
     * @return an Animator object
     */
    public Animator getAnimator() {
        //Currently makes the tile get bigger and smaller
        ValueAnimator animator = ValueAnimator.ofFloat(1, 1.1f, 1);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                tile.setScaleX(value);
                tile.setScaleY(value);
            }
        });
        animator.setRepeatCount(Animation.INFINITE);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }
        });
        animator.setDuration(DURATION);
        return animator;
    }
}
