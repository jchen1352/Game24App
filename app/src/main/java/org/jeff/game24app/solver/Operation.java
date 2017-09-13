package org.jeff.game24app.solver;

public class Operation {

    private Rational num0, num1;
    public enum ArithmeticOp {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    };
    private ArithmeticOp op;

    public Operation(Rational num0, Rational num1, ArithmeticOp op) {
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

    public ArithmeticOp getOp() {
        return op;
    }

    public boolean canDivide() {
        return num1.getNumerator() != 0;
    }

    public Rational evaluate() {
        if (op == ArithmeticOp.ADD) {
            return Rational.add(num0, num1);
        }
        else if (op == ArithmeticOp.SUBTRACT) {
            return Rational.subtract(num0, num1);
        }
        else if (op == ArithmeticOp.MULTIPLY) {
            return Rational.multiply(num0, num1);
        }
        else {
            if (canDivide()) {
                return Rational.divide(num0, num1);
            }
            return null;
        }
    }
}
