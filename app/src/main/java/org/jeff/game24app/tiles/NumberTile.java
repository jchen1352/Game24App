package org.jeff.game24app.tiles;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import org.jeff.game24app.R;
import org.jeff.game24app.solver.Rational;

/**
 * A tile that handles a number.
 */
public class NumberTile extends BaseTile {

    private Rational value;
    private boolean exists;
    /** Padding from tile border as percentage of side length **/
    private static final float PAD = .18f;
    /** Height of fraction bar as percentage of tile side length **/
    private static final float FRAC_HEIGHT = .03f;
    /** Padding from fraction bar as percentage of tile side length **/
    private static final float FRAC_PAD = .05f;
    /** Width to height ratio of a single digit **/
    private static final float WH_RATIO = .75f;
    /** Gap between two digits as a percentage of width **/
    private static final float GAP = .05f;

    private Rect dims;
    private Rect[] numeratorBounds;
    private int numeratorDigits;
    private Rect[] denominatorBounds;
    private int denominatorDigits;
    private boolean boundsValid;
    private static Drawable[] digitPics;
    private static Paint digitColor;

    public NumberTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        exists = true;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.NumberTile, 0, 0);
        int numerator = a.getInteger(R.styleable.NumberTile_numerator,1);
        int denominator = a.getInteger(R.styleable.NumberTile_denominator,1);
        a.recycle();
        value = new Rational(numerator, denominator);
        dims = new Rect();
        //Maximum number size should be 5 digits
        numeratorBounds = new Rect[5];
        denominatorBounds = new Rect[5];
        for (int i = 0; i < 5; i++) {
            numeratorBounds[i] = new Rect();
            denominatorBounds[i] = new Rect();
        }
        numeratorDigits = 0;
        denominatorDigits = 0;
        boundsValid = false;

        if (digitPics == null) {
            digitPics = new Drawable[10];
            Resources res = getResources();
            digitPics[0] = res.getDrawable(R.drawable.ic_digit_0, null);
            digitPics[1] = res.getDrawable(R.drawable.ic_digit_1, null);
            digitPics[2] = res.getDrawable(R.drawable.ic_digit_2, null);
            digitPics[3] = res.getDrawable(R.drawable.ic_digit_3, null);
            digitPics[4] = res.getDrawable(R.drawable.ic_digit_4, null);
            digitPics[5] = res.getDrawable(R.drawable.ic_digit_5, null);
            digitPics[6] = res.getDrawable(R.drawable.ic_digit_6, null);
            digitPics[7] = res.getDrawable(R.drawable.ic_digit_7, null);
            digitPics[8] = res.getDrawable(R.drawable.ic_digit_8, null);
            digitPics[9] = res.getDrawable(R.drawable.ic_digit_9, null);
        }

        if (digitColor == null) {
            digitColor = new Paint();
            digitColor.setColor(ContextCompat.getColor(getContext(), R.color.number_color));
        }
    }

    public Rational getValue() {
        return value;
    }

    public void setValue(Rational num) {
        value = num;
        boundsValid = false;
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
    protected void onDraw(Canvas canvas) {
        if (exists) {
            super.onDraw(canvas);
            int numerator = value.getNumerator();
            int denominator = value.getDenominator();
            if (!boundsValid) {
                //Tile should be a square
                int sideLength = getWidth();
                int pad = (int) (sideLength * PAD);
                if (denominator == 1) {
                    dims.left = pad;
                    dims.top = pad;
                    dims.right = sideLength - pad;
                    dims.bottom = sideLength - pad;
                    getPicBounds(dims, numerator, numeratorBounds);
                    numeratorDigits = getNumDigits(numerator);
                } else {
                    dims.left = pad;
                    dims.top = pad;
                    dims.right = sideLength - pad;
                    dims.bottom = sideLength/2 - (int) (sideLength * FRAC_PAD);
                    getPicBounds(dims, numerator, numeratorBounds);
                    numeratorDigits = getNumDigits(numerator);
                    dims.left = pad;
                    dims.top = sideLength/2 + (int) (sideLength * FRAC_PAD);
                    dims.right = sideLength - pad;
                    dims.bottom = sideLength - pad;
                    getPicBounds(dims, denominator, denominatorBounds);
                    denominatorDigits = getNumDigits(denominator);
                }
                boundsValid = true;
            }

            Drawable pic;
            //Draw numerator
            for (int i = 0; i < numeratorDigits; i++) {
                pic = digitPics[numerator % 10];
                pic.setBounds(numeratorBounds[numeratorDigits-i-1]);
                pic.draw(canvas);
                numerator /= 10;
            }

            //Draw denominator if necessary
            if (denominator != 1) {
                for (int i = 0; i < denominatorDigits; i++) {
                    pic = digitPics[denominator % 10];
                    pic.setBounds(denominatorBounds[denominatorDigits-i-1]);
                    pic.draw(canvas);
                    denominator /= 10;
                }
                //Get farthest left and right bounds for numerator/denominator
                //to determine how long fraction bar should be
                int left = Math.min(numeratorBounds[0].left, denominatorBounds[0].left);
                int right = Math.max(numeratorBounds[numeratorDigits-1].right,
                        denominatorBounds[denominatorDigits-1].right);
                float heightOffset = getHeight() * FRAC_HEIGHT / 2;
                canvas.drawRect(left, getHeight()/2 - heightOffset,
                        right, getHeight()/2 + heightOffset, digitColor);

            }
        }
    }

    /**
     * Helper function that calculates bounds for the number pics within constraints
     * @param constraints Where the number must fit
     * @param number The number to display, must be positive
     * @param outBounds Changed to contain the calculated bounds
     */
    private void getPicBounds(Rect constraints, int number, Rect[] outBounds) {
        int height = constraints.height();
        int widthDigit = (int)(height * WH_RATIO);
        int numDigits = getNumDigits(number);
        int gap = (int)(widthDigit * GAP);
        int width = numDigits * (widthDigit + gap) - gap;
        if (width > constraints.width()) {
            widthDigit = (int)(constraints.width() / (numDigits + GAP * (numDigits-1)));
            gap = (int)(widthDigit * GAP);
            height = (int)(widthDigit / WH_RATIO);
            width = numDigits * (widthDigit + gap) - gap;
        }
        int startLeft = (constraints.right + constraints.left)/2 - width/2;
        int top = (constraints.top + constraints.bottom)/2 - height/2;
        int bottom = top + height;
        for (int i = 0; i < numDigits; i++) {
            //Check length just in case
            if (i < outBounds.length) {
                int left = startLeft + i * (widthDigit + gap);
                int right = left + widthDigit;
                outBounds[i].left = left;
                outBounds[i].top = top;
                outBounds[i].right = right;
                outBounds[i].bottom = bottom;
            }
        }
    }

    private static int getNumDigits(int n) {
        return n <= 0 ? 1 : (int)(Math.log10(n) + 1);
    }
}
