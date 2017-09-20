package org.jeff.game24app.tiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.jeff.game24app.R;
import org.jeff.game24app.solver.Rational;

/**
 * A tile that handles a number.
 */
public class NumberTile extends BaseTile {

    private Rational value;
    private boolean exists;
    private static final int PAD = 100, FRAC_HEIGHT = 20, FRAC_PAD = 15;
    /** Width to height ratio **/
    private static final float WH_RATIO = 195.5f/335;
    /** Gap between two digits as a percentage of width **/
    private static final float GAP = .1f;

    private Rect dims;
    private Rect[] numeratorBounds;
    private Rect[] denominatorBounds;
    private boolean boundsValid;

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
        boundsValid = false;
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
                if (denominator == 1) {
                    dims.left = PAD;
                    dims.top = PAD;
                    dims.right = getWidth() - PAD;
                    dims.bottom = getHeight() - PAD;
                    numeratorBounds = getPicBounds(dims, numerator);
                } else {
                    dims.left = PAD;
                    dims.top = PAD;
                    dims.right = getWidth() - PAD;
                    dims.bottom = getHeight()/2 - FRAC_PAD;
                    numeratorBounds = getPicBounds(dims, numerator);
                    dims.left = PAD;
                    dims.top = getHeight()/2 + FRAC_PAD;
                    dims.right = getWidth() - PAD;
                    dims.bottom = getHeight() - PAD;
                    denominatorBounds = getPicBounds(dims, denominator);
                }
                boundsValid = true;
            }

            Drawable pic;
            //Draw numerator
            for (int i = 0; i < numeratorBounds.length; i++) {
                pic = getResources().getDrawable(getDigitID(numerator % 10), null);
                pic.setBounds(numeratorBounds[numeratorBounds.length-i-1]);
                pic.draw(canvas);
                numerator /= 10;
            }

            //Draw denominator if necessary
            if (denominator != 1) {
                pic = getResources().getDrawable(R.drawable.ic_fraction_bar, null);
                pic.setBounds(PAD, getHeight()/2 - FRAC_HEIGHT/2,
                        getWidth() - PAD, getHeight()/2 + FRAC_HEIGHT/2);
                pic.draw(canvas);
                for (int i = 0; i < denominatorBounds.length; i++) {
                    pic = getResources().getDrawable(getDigitID(denominator % 10), null);
                    pic.setBounds(denominatorBounds[denominatorBounds.length-i-1]);
                    pic.draw(canvas);
                    denominator /= 10;
                }
            }
        }
    }

    /**
     * Helper function that returns an array of Rects that contain
     * bounds for the number pics within constraints
     * @param constraints Where the number must fit
     * @param number The number to display, must be positive
     * @return The bounds for each digit pic in number
     */
    private Rect[] getPicBounds(Rect constraints, int number) {
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
        Rect[] bounds = new Rect[numDigits];
        for (int i = 0; i < numDigits; i++) {
            int left = startLeft + i * (widthDigit + gap);
            int right = left + widthDigit;
            int bottom = top + height;
            bounds[i] = new Rect(left, top, right, bottom);
        }
        return bounds;
    }

    /**
     * Helper function that returns the resource id for the digit.
     * @param digit Must be a digit between 0 and 9
     * @return The drawable resource id associated with the digit
     */
    private int getDigitID(int digit) {
        switch (digit) {
            case 1:
                return R.drawable.ic_digit_1;
            case 2:
                return R.drawable.ic_digit_2;
            case 3:
                return R.drawable.ic_digit_3;
            case 4:
                return R.drawable.ic_digit_4;
            case 5:
                return R.drawable.ic_digit_5;
            case 6:
                return R.drawable.ic_digit_6;
            case 7:
                return R.drawable.ic_digit_7;
            case 8:
                return R.drawable.ic_digit_8;
            case 9:
                return R.drawable.ic_digit_9;
            case 0:
            default:
                return R.drawable.ic_digit_0;
        }
    }

    private int getNumDigits(int number) {
        //number should not be negative
        return number <= 0 ? 1 : (int)(Math.log10(number) + 1);
    }
}
