package org.jeff.game24app;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.Button;

import org.jeff.game24app.animations.ViewAnimatorFactory;

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
