package org.jeff.game24app.views;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * A button with a certain font.
 */
public class FontButton extends AppCompatButton {

    public FontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        FontHelper.setCustomFont(this, context, attrs);
    }
}
