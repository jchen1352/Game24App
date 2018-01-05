package org.jeff.game24app.views;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import org.jeff.game24app.R;

/**
 * A button that appears on the home screen.
 */
public class HomeButton extends AppCompatButton {

    public HomeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        FontHelper.setCustomFont(this, context, attrs);
    }
}
