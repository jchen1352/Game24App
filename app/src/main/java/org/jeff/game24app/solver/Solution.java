package org.jeff.game24app.solver;

import java.util.ArrayList;
import java.util.List;

public class Solution {

    private List<Operation> ops;
    /** Contains the numbers remaining after doing ops **/
    private Rational[] numbers;

    public Solution(List<Operation> ops, Rational[] numbers) {
        this.ops = ops;
        this.numbers = numbers;
    }

    public Rational[] getNumbers() {
        return numbers;
    }

    public List<Operation> getOpsCopy() {
        List<Operation> opsCopy = new ArrayList<Operation>();
        for (Operation op : ops) {
            opsCopy.add(op);
        }
        return opsCopy;
    }
}
