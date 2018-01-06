package org.jeff.game24app.views;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class HomeTextView extends AppCompatTextView {

    public HomeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        FontHelper.setCustomFont(this, context, attrs);
    }
}
