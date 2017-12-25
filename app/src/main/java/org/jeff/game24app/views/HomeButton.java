package org.jeff.game24app.views;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import org.jeff.game24app.animations.ViewAnimatorGen;

/**
 * A button that appears on the home screen.
 */
public class HomeButton extends AppCompatButton {

    private static final String FONT_PATH = "fonts/FunCartoon2.ttf";
    private Animator fadeInAnimator;
    private Animator fadeOutAnimator;

    public HomeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface font = Typeface.createFromAsset(context.getAssets(), FONT_PATH);
        setTypeface(font);
        ViewAnimatorGen generator = new ViewAnimatorGen(this);
        fadeInAnimator = generator.getFadeInAnimator();
        fadeOutAnimator = generator.getFadeOutAnimator();
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
