package org.jeff.game24app.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution {

    private SolutionSteps steps;
    private Rational[] numbers;
    private Map<Rational, List<String>> strMap;
    private String string;

    public Solution(SolutionSteps steps, Rational[] numbers) {
        this.steps = steps;
        this.numbers = numbers;
        strMap = new HashMap<>();
        for (Operation op : steps.getOps()) {
            Rational num0 = op.getNum0();
            Rational num1 = op.getNum1();
            Operation.ArithmeticOp aOp = op.getOp();
            String s = "(";
            if (strMap.containsKey(num0) && !strMap.get(num0).isEmpty()) {
                s += strMap.get(num0).remove(0);
            }
            else {
                s += num0;
            }
            switch (aOp) {
                case ADD:      s += "+"; break;
                case SUBTRACT: s += "-"; break;
                case MULTIPLY: s += "*"; break;
                case DIVIDE:   s += "/"; break;
            }
            if (strMap.containsKey(num1) && !strMap.get(num1).isEmpty()) {
                s += strMap.get(num1).remove(0);
            }
            else {
                s += num1;
            }
            s += ")";
            Rational eval = op.evaluate();
            if (!strMap.containsKey(eval)) {
                strMap.put(eval, new ArrayList<String>());
            }
            strMap.get(eval).add(s);
        }
        if (strMap.containsKey(Rational.CONST_24) && !strMap.get(Rational.CONST_24).isEmpty()) {
            string = strMap.get(Rational.CONST_24).get(0);
        }
        else {
            string = "sorry";
        }
    }

    public Rational[] getNumbers() {
        return numbers;
    }

    public SolutionSteps getSteps() {
        return steps;
    }

    public Solution copy() {
        return new Solution(steps.copy(), Arrays.copyOf(numbers, numbers.length));
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Solution && string.equals(obj.toString());
    }
}
