package org.jeff.game24app.tiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.jeff.game24app.R;
import org.jeff.game24app.solver.Operation;

/**
 * A tile that handles an Operation.
 */
public class OperationTile extends BaseTile {

    private Operation.ArithmeticOp op;

    /** Padding from tile border as percentage of side length **/
    private static final float PAD = .18f;

    public OperationTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.OperationTile, 0, 0);
        int opInt = a.getInteger(R.styleable.OperationTile_operation, 0);
        a.recycle();
        op = Operation.ArithmeticOp.values()[opInt];
    }

    public Operation.ArithmeticOp getOp() {
        return op;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable pic;
        switch (op) {
            case ADD:
                pic = getResources().getDrawable(R.drawable.ic_op_add, null);
                break;
            case SUBTRACT:
                pic = getResources().getDrawable(R.drawable.ic_op_sub, null);
                break;
            case MULTIPLY:
                pic = getResources().getDrawable(R.drawable.ic_op_mul, null);
                break;
            case DIVIDE:
                pic = getResources().getDrawable(R.drawable.ic_op_div, null);
                break;
            default:
                pic = getResources().getDrawable(R.drawable.ic_op_add, null);
        }
        int pad = (int) (getWidth() * PAD);
        pic.setBounds(pad, pad, getWidth() - pad, getHeight() - pad);
        pic.draw(canvas);
    }
}
