package org.jeff.game24app.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game24Solver {

    private List<Solution> solutionBuffer;
    private List<Solution> solutions;

    public Game24Solver(Rational[] numbers) {
        solutionBuffer = new ArrayList<Solution>();
        solutions = new ArrayList<Solution>();
        solutionBuffer.add(new Solution(new ArrayList<Operation>(), numbers));
    }

    public boolean isSolvable() {
        return !getSolutions().isEmpty();
    }

    public List<Solution> getSolutions() {
        while (!solutionBuffer.isEmpty()) {
            updateBufferOnce();
        }
        return solutions;
    }

    private void updateBufferOnce() {
        Solution sol = solutionBuffer.remove(0);
        Rational[] numbers = sol.getNumbers();
        if (numbers.length == 1) {
            if (numbers[0].equals(Rational.CONST_24)) {
                solutions.add(sol);
            }
        }
        else {
            for (int i = 0; i < numbers.length-1; i++) {
                for (int j = i+1; j < numbers.length; j++) {
                    Rational[] newNumbers = new Rational[numbers.length-1];
                    int newI = 0;
                    for (int k = 0; k < numbers.length; k++) {
                        if (k != i && k != j) {
                            newNumbers[newI++] = numbers[k];
                        }
                    }

                    List<Operation> ops = new ArrayList<Operation>();
                    if (numbers[i].compareTo(numbers[j]) > 0) {
                        ops.add(new Operation(numbers[i], numbers[j], Operation.ArithmeticOp.ADD));
                        ops.add(new Operation(numbers[i], numbers[j], Operation.ArithmeticOp.MULTIPLY));
                        ops.add(new Operation(numbers[i], numbers[j], Operation.ArithmeticOp.SUBTRACT));
                    }
                    else {
                        ops.add(new Operation(numbers[j], numbers[i], Operation.ArithmeticOp.ADD));
                        ops.add(new Operation(numbers[j], numbers[i], Operation.ArithmeticOp.MULTIPLY));
                        ops.add(new Operation(numbers[j], numbers[i], Operation.ArithmeticOp.SUBTRACT));
                    }
                    Operation divideOp0 = new Operation(numbers[i], numbers[j], Operation.ArithmeticOp.DIVIDE);
                    if (divideOp0.canDivide()) {
                        ops.add(divideOp0);
                    }
                    Operation divideOp1 = new Operation(numbers[j], numbers[i], Operation.ArithmeticOp.DIVIDE);
                    if (divideOp1.canDivide()) {
                        ops.add(divideOp1);
                    }

                    for (Operation op : ops) {
                        newNumbers[newI] = op.evaluate();
                        List<Operation> steps = sol.getOpsCopy();
                        steps.add(op);
                        solutionBuffer.add(new Solution(steps, Arrays.copyOf(newNumbers, newNumbers.length)));
                    }
                }
            }
        }
    }
}
