package org.jeff.game24app.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game24Solver {

    public static List<Operation[]> solve(Rational[] numbers) {
        Operation[] start = new Operation[0];
        List<Operation[]> solutions = new ArrayList<>();
        solve(numbers, start, solutions);
        return solutions;
    }

    private static void solve(Rational[] numbers, Operation[] current,
                             List<Operation[]> solutions) {
        if (numbers.length == 1 && numbers[0].equals(Rational.CONST_24)) {
            solutions.add(current.clone());
        }
        for (int i = 0; i < numbers.length; i++) {
            for (int j = i+1; j < numbers.length; j++) {
                Operation[] next = Arrays.copyOf(current, current.length + 1);
                Rational[] newNums = new Rational[numbers.length - 1];
                int newI = 0;
                for (int k = 0; k < numbers.length; k++) {
                    if (k != i && k != j) {
                        newNums[newI++] = numbers[k];
                    }
                }
                Rational num0, num1;
                if (numbers[i].compareTo(numbers[j]) > 0) {
                    num0 = numbers[i];
                    num1 = numbers[j];
                } else {
                    num0 = numbers[j];
                    num1 = numbers[i];
                }
                Operation op;
                op = new Operation(num0, num1, Operation.ADD);
                next[next.length - 1] = op;
                newNums[newI] = op.evaluate();
                solve(newNums, next, solutions);
                op = new Operation(num0, num1, Operation.SUBTRACT);
                next[next.length - 1] = op;
                newNums[newI] = op.evaluate();
                solve(newNums, next, solutions);
                op = new Operation(num0, num1, Operation.MULTIPLY);
                next[next.length - 1] = op;
                newNums[newI] = op.evaluate();
                solve(newNums, next, solutions);
                op = new Operation(num0, num1, Operation.DIVIDE);
                if (op.canDivide()) {
                    next[next.length - 1] = op;
                    newNums[newI] = op.evaluate();
                    solve(newNums, next, solutions);
                }
                op = new Operation(num1, num0, Operation.DIVIDE);
                if (op.canDivide()) {
                    next[next.length - 1] = op;
                    newNums[newI] = op.evaluate();
                    solve(newNums, next, solutions);
                }
            }
        }
    }

    public static boolean isSolvable(Rational[] numbers) {
        return !solve(numbers).isEmpty();
    }
}
