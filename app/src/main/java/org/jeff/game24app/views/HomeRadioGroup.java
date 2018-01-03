package org.jeff.game24app.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import org.jeff.game24app.R;

/**
 * A radio group that appears on the home screen.
 */
public class HomeRadioGroup extends RadioGroup {

    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;

    public HomeRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        fadeInAnimator = AnimatorInflater.loadAnimator(context, R.animator.fade_in);
        fadeInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setClickable(true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setVisibility(VISIBLE);
            }
        });
        fadeInAnimator.setTarget(this);
        fadeOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.fade_out);
        fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(GONE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                setClickable(false);
            }
        });
        fadeOutAnimator.setTarget(this);
    }

    /**
     * Start the animation that makes this fade in.
     */
    public void fadeIn() {
        fadeInAnimator.start();
    }

    /**
     * Start the animation that makes this fade out.
     */
    public void fadeOut() {
        fadeOutAnimator.start();
    }
}
