package org.jeff.game24app.tiles;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import org.jeff.game24app.R;
import org.jeff.game24app.animations.ViewAnimatorGen;
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
    private static final float WH_RATIO = 195.5f/335;
    /** Gap between two digits as a percentage of width **/
    private static final float GAP = .1f;

    private Rect dims;
    private Rect[] numeratorBounds;
    private Rect[] denominatorBounds;
    private boolean boundsValid;

    private static Paint digitColor;

    private Animator shrinkAnimator, growAnimator;

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
                    numeratorBounds = getPicBounds(dims, numerator);
                } else {
                    dims.left = pad;
                    dims.top = pad;
                    dims.right = sideLength - pad;
                    dims.bottom = sideLength/2 - (int) (sideLength * FRAC_PAD);
                    numeratorBounds = getPicBounds(dims, numerator);
                    dims.left = pad;
                    dims.top = sideLength/2 + (int) (sideLength * FRAC_PAD);
                    dims.right = sideLength - pad;
                    dims.bottom = sideLength - pad;
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
                for (int i = 0; i < denominatorBounds.length; i++) {
                    pic = getResources().getDrawable(getDigitID(denominator % 10), null);
                    pic.setBounds(denominatorBounds[denominatorBounds.length-i-1]);
                    pic.draw(canvas);
                    denominator /= 10;
                }
                //Get farthest left and right bounds for numerator/denominator
                //to determine how long fraction bar should be
                int left = Math.min(numeratorBounds[0].left, denominatorBounds[0].left);
                int right = Math.max(numeratorBounds[numeratorBounds.length-1].right,
                        denominatorBounds[denominatorBounds.length-1].right);
                float heightOffset = getHeight() * FRAC_HEIGHT / 2;
                canvas.drawRect(left, getHeight()/2 - heightOffset,
                        right, getHeight()/2 + heightOffset, digitColor);

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
        int numDigits = number <= 0 ? 1 : (int)(Math.log10(number) + 1);
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

    public void setShrinkAnimator(Animator a) {
        shrinkAnimator = a;
    }

    public void setGrowAnimator(Animator a) {
        growAnimator = a;
    }

    public void shrink() {
        shrinkAnimator.start();
    }

    public void grow() {
        setVisibility(VISIBLE);
        growAnimator.start();
    }
}
