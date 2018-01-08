package org.jeff.game24app.views;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * A button that appears on the home screen.
 */
public class HomeButton extends AppCompatButton {

    public HomeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        FontHelper.setCustomFont(this, context, attrs);
    }
}
