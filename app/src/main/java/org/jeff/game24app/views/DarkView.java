package org.jeff.game24app.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A view that is dark and translucent with a rectangular area
 * that is fully transparent. Used to show a hint.
 */
public class DarkView extends View {

    /** The area that is transparent **/
    private Rect area;
    private Paint transparent;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private boolean click = false;
    private View hintNum0, hintNum1, hintOp, numGroup, opGroup;
    public enum Mode {
        NUM0, OP, NUM1, INACTIVE
    }
    private Mode mode;

    private static final int darkColor = Color.argb(100, 0, 0, 0);

    public DarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        transparent = new Paint();
        transparent.setColor(Color.TRANSPARENT);
        transparent.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        area = new Rect();
        mode = Mode.INACTIVE;
    }

    public void setArea(int left, int top, int right, int bottom) {
        area.left = left;
        area.top = top;
        area.right = right;
        area.bottom = bottom;
        invalidate();
    }

    /**
     * Sets the area based on which mode it is in.
     */
    private void setModeArea() {
        int left, top, right, bottom;
        switch (mode) {
            case NUM0:
                left = hintNum0.getLeft() + numGroup.getLeft();
                top = hintNum0.getTop() + numGroup.getTop();
                right = hintNum0.getRight() + numGroup.getLeft();
                bottom = hintNum0.getBottom() + numGroup.getTop();
                break;
            case OP:
                left = hintOp.getLeft() + opGroup.getLeft();
                top = hintOp.getTop() + opGroup.getTop();
                right = hintOp.getRight() + opGroup.getLeft();
                bottom = hintOp.getBottom() + opGroup.getTop();
                break;
            case NUM1:
                left = hintNum1.getLeft() + numGroup.getLeft();
                top = hintNum1.getTop() + numGroup.getTop();
                right = hintNum1.getRight() + numGroup.getLeft();
                bottom = hintNum1.getBottom() + numGroup.getTop();
                break;
            case INACTIVE:
            default:
                left = 0;
                top = 0;
                right = 0;
                bottom = 0;
                break;
        }
        setArea(left, top, right, bottom);
    }

    public void startHint() {
        mode = Mode.NUM0;
        setModeArea();
    }

    public void setViews(View num0, View op, View num1, View nums, View ops) {
        hintNum0 = num0;
        hintOp = op;
        hintNum1 = num1;
        numGroup = nums;
        opGroup = ops;
    }

    /**
     * Returns if the coordinate is in the transparent area
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if in bounds, false otherwise
     */
    private boolean inArea(float x, float y) {
        return area.left <= x && x < area.right && area.top <= y && y < area.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null || bitmapCanvas == null) {
            if (bitmap != null) bitmap.recycle();
            bitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(),
                    Bitmap.Config.ARGB_4444);
            bitmapCanvas = new Canvas(bitmap);
        }
        bitmapCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        bitmapCanvas.drawColor(darkColor);
        bitmapCanvas.drawRect(area, transparent);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (inArea(x, y)) {
                    click = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (click && !inArea(x, y)) {
                    click = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (click && inArea(x, y)) {
                    performClick();
                    click = false;
                }
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        //Only do when click within the area
        if (click) {
            //Order of hint is: num0, op, num1
            switch (mode) {
                case NUM0:
                    hintNum0.performClick();
                    mode = Mode.OP;
                    break;
                case OP:
                    hintOp.performClick();
                    mode = Mode.NUM1;
                    break;
                case NUM1:
                    hintNum1.performClick();
                    mode = Mode.INACTIVE;
                    break;
                case INACTIVE:
                default:
                    mode = Mode.INACTIVE;
                    break;
            }
            setModeArea();
            if (mode == Mode.INACTIVE) {
                setVisibility(GONE);
            }
        }
        return true;
    }
}
