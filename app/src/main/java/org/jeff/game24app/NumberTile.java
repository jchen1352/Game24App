package org.jeff.game24app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class NumberTile extends BaseTile {

    private Rational value;
    private boolean exists;

    public NumberTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        exists = true;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.NumberTile, 0, 0);
        int numerator = a.getInteger(R.styleable.NumberTile_numerator,1);
        int denominator = a.getInteger(R.styleable.NumberTile_denominator,1);
        a.recycle();
        value = new Rational(numerator, denominator);
    }

    public Rational getValue() {
        return value;
    }

    public void setValue(Rational num) {
        value = num;
        invalidate();
    }

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean e) {
        exists = e;
        invalidate();
    }

    @Override
    protected String getString() {
        return value.toString();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (exists) {
            super.onDraw(canvas);
        }
    }
}
