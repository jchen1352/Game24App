package org.jeff.game24app.tiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.jeff.game24app.R;
import org.jeff.game24app.solver.Operation;

public class OperationTile extends BaseTile {

    private Operation.ArithmeticOp op;

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
    protected String getString() {
        String opString = "";
        switch (op) {
            case ADD:
                opString = "+";
                break;
            case SUBTRACT:
                opString = "-";
                break;
            case MULTIPLY:
                opString = "*";
                break;
            case DIVIDE:
                opString = "/";
        }
        return opString;
    }
}
