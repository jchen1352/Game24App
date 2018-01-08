package org.jeff.game24app.tiles;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewPropertyAnimator;

import org.jeff.game24app.GameActivity;
import org.jeff.game24app.solver.Operation;
import org.jeff.game24app.solver.Rational;

public class TileManager {

    /**
     * The number of NumberTiles selected
     **/
    private int numsSelectedLen;
    private NumberTile[] numsSelected;
    private int numExists;
    private OperationTile opSelected;
    private View.OnClickListener numListener;
    private View.OnClickListener opListener;
    private GameActivity activity;

    //Refers to sliding animation when completing operation
    private ViewPropertyAnimator animator;
    private boolean animating;
    private static final long ANIM_DURATION = 300;

    public TileManager(GameActivity a) {
        numsSelectedLen = 0;
        numsSelected = new NumberTile[2];
        numExists = 4;
        opSelected = null;
        setupListeners();
        activity = a;
        animating = false;
    }

    private void setupListeners() {
        numListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animating) return;
                NumberTile numTile = (NumberTile) v;
                if (!numTile.exists()) return;
                int selected = numsSelectedLen;
                if (numTile.isActive()) {
                    selected--;
                } else {
                    selected++;
                }
                if (selected <= 2) {
                    activity.playTapSound();
                    numTile.toggle();
                    if (selected < numsSelectedLen) {
                        for (int i = 0; i < numsSelectedLen; i++) {
                            if (numsSelected[i] == numTile) {
                                numsSelected[i] = null;
                                System.arraycopy(numsSelected, i + 1,
                                        numsSelected, i, numsSelected.length - i - 1);
                            }
                        }
                    } else if (selected > numsSelectedLen) {
                        numsSelected[numsSelectedLen] = numTile;
                    }
                    numsSelectedLen = selected;

                    if (readyForOperation()) {
                        completeOperation();
                    }
                }
            }
        };

        opListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animating) return;
                activity.playTapSound();
                OperationTile opTile = (OperationTile) v;
                opTile.toggle();
                if (opTile.isActive()) {
                    if (opSelected != null) {
                        opSelected.toggle();
                    }
                    opSelected = opTile;

                    if (readyForOperation()) {
                        completeOperation();
                    }
                } else {
                    opSelected = null;
                }
            }
        };
    }

    public View.OnClickListener getNumListener() {
        return numListener;
    }

    public View.OnClickListener getOpListener() {
        return opListener;
    }

    private boolean readyForOperation() {
        return numsSelectedLen == 2 && opSelected != null;
    }

    private void completeOperation() {
        final NumberTile numTile0 = numsSelected[0];
        final NumberTile numTile1 = numsSelected[1];

        //delete the first tile and update the second
        Rational num0 = numTile0.getValue();
        Rational num1 = numTile1.getValue();
        @Operation.Ops int op = opSelected.getOp();
        //don't divide by 0
        if (!(op == Operation.DIVIDE && num1.getNumerator() == 0)) {
            Operation operation = new Operation(num0, num1, op);
            Rational result = operation.evaluate();
            //Take absolute value because negatives aren't necessary
            if (result.getFloatValue() < 0) {
                result = new Rational(-result.getNumerator(), result.getDenominator());
            }
            final Rational r = result;
            //Animate one tile sliding into the other
            numTile0.bringToFront();
            animator = numTile0.animate()
                    .x(numTile1.getX())
                    .y(numTile1.getY())
                    .setDuration(ANIM_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            animating = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            numTile0.setTranslationX(0);
                            numTile0.setTranslationY(0);
                            numTile1.setValue(r);
                            numTile0.setExists(false);
                            numExists--;
                            if (r.equals(Rational.CONST_24) && numExists == 1) {
                                activity.victoryAnim(numTile1);
                            }
                            animating = false;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            animation.removeAllListeners();
                            numTile0.setTranslationX(0);
                            numTile0.setTranslationY(0);
                            animating = false;
                        }
                    });
        }
        //reset selections
        numTile0.toggle();
        numTile1.toggle();
        opSelected.toggle();
        numsSelected[0] = null;
        numsSelected[1] = null;
        numsSelectedLen = 0;
        opSelected = null;
    }

    public void reset() {
        if (animator != null) {
            animator.cancel();
        }
        numsSelectedLen = 0;
        numsSelected[0] = null;
        numsSelected[1] = null;
        numExists = 4;
        opSelected = null;
    }
}
