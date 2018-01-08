package org.jeff.game24app.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * A TextView with a certain font
 */
public class FontTextView extends AppCompatTextView {

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        FontHelper.setCustomFont(this, context, attrs);
    }
}
