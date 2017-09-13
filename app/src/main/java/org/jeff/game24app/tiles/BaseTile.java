package org.jeff.game24app.tiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public abstract class BaseTile extends View {

    private Paint textPaint;
    private Paint borderPaint;
    private Rect boundingRect;
    protected boolean isSelected;

    public BaseTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(150);

        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10f);

        boundingRect = new Rect();

        isSelected = false;
    }

    protected abstract String getString();

    /**
     * Change isSelected and adjust drawing appropriately (change color)
     */
    public void toggle() {
        if (isSelected) {
            isSelected = false;
            textPaint.setColor(Color.BLACK);
            borderPaint.setColor(Color.BLACK);
        } else {
            isSelected = true;
            textPaint.setColor(Color.GREEN);
            borderPaint.setColor(Color.GREEN);
        }
        invalidate();
    }

    public void unselect() {
        if (isSelected) {
            toggle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String string = getString();
        textPaint.getTextBounds(string, 0, string.length(), boundingRect);
        int x = getWidth()/2 - boundingRect.width()/2;
        int y = getHeight()/2 + boundingRect.height()/2;
        canvas.drawText(string, x, y, textPaint);
        canvas.drawRect(1f, 1f, (float)getWidth()-1, (float)getHeight()-1, borderPaint);
    }
}
