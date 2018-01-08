package org.jeff.game24app.solver;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Operation {

    @IntDef({ADD, SUBTRACT, MULTIPLY, DIVIDE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Ops {
    }

    public static final int ADD = 0;
    public static final int SUBTRACT = 1;
    public static final int MULTIPLY = 2;
    public static final int DIVIDE = 3;
    private @Ops
    int op;

    private Rational num0, num1;

    public Operation(Rational num0, Rational num1, @Ops int op) {
        this.num0 = num0;
        this.num1 = num1;
        this.op = op;
    }

    public Rational getNum0() {
        return num0;
    }

    public Rational getNum1() {
        return num1;
    }

    public @Ops
    int getOp() {
        return op;
    }

    public boolean canDivide() {
        return num1.getNumerator() != 0;
    }

    public Rational evaluate() {
        switch (op) {
            case ADD:
                return Rational.add(num0, num1);
            case SUBTRACT:
                return Rational.subtract(num0, num1);
            case MULTIPLY:
                return Rational.multiply(num0, num1);
            case DIVIDE:
                if (canDivide()) {
                    return Rational.divide(num0, num1);
                }
        }
        return null;
    }

    @Override
    public String toString() {
        return num0.toString() + "+-*/".substring(op, op + 1) + num1.toString();
    }
}
