package org.jeff.game24app.views;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

import org.jeff.game24app.animations.ViewAnimatorFactory;

/**
 * A radio group that appears on the home screen.
 */
public class HomeRadioGroup extends RadioGroup {

    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;

    public HomeRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewAnimatorFactory factory = new ViewAnimatorFactory(this);
        fadeInAnimator = factory.getFadeInAnimator();
        fadeOutAnimator = factory.getFadeOutAnimator();
    }

    /**
     * Start the animation that makes this fade in.
     */
    public void fadeIn() {
        setVisibility(VISIBLE);
        fadeInAnimator.start();
    }

    /**
     * Start the animation that makes this fade out.
     */
    public void fadeOut() {
        fadeOutAnimator.start();
    }
}
