package org.jeff.game24app.tiles;

import android.content.Context;
import android.content.res.Resources;
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

    private @Operation.Ops
    int op;

    private static Drawable[] opPics;

    /**
     * Padding from tile border as percentage of side length
     **/
    private static final float PAD = .18f;

    public OperationTile(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.OperationTile, 0, 0);
        int opInt = a.getInteger(R.styleable.OperationTile_operation, 0);
        a.recycle();
        op = opInt;

        if (opPics == null) {
            opPics = new Drawable[4];
            Resources res = getResources();
            Resources.Theme theme = res.newTheme();
            theme.applyStyle(R.style.OpNormal, true);
            opPics[0] = res.getDrawable(R.drawable.ic_op_add, theme);
            opPics[1] = res.getDrawable(R.drawable.ic_op_sub, theme);
            opPics[2] = res.getDrawable(R.drawable.ic_op_mul, theme);
            opPics[3] = res.getDrawable(R.drawable.ic_op_div, theme);
        }
    }

    public @Operation.Ops
    int getOp() {
        return op;
    }

    public void setOp(@Operation.Ops int op) {
        this.op = op;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable pic = opPics[op];
        int pad = (int) (getWidth() * PAD);
        pic.setBounds(pad, pad, getWidth() - pad, getHeight() - pad);
        pic.draw(canvas);
    }
}
