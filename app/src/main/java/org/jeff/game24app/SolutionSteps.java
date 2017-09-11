package org.jeff.game24app;

import java.util.ArrayList;
import java.util.List;

public class SolutionSteps {

    private List<Operation> ops;
    private boolean valid;

    public SolutionSteps() {
        ops = new ArrayList<Operation>();
        valid = false;
    }

    public void addStep(Operation step) {
        ops.add(step);
    }

    public void setValid(boolean v) {
        valid = v;
    }

    public boolean isValid() {
        return valid;
    }

    public List<Operation> getOps() {
        return ops;
    }

    public SolutionSteps copy() {
        SolutionSteps solutionStepsCopy = new SolutionSteps();
        for (Operation op : ops) {
            solutionStepsCopy.ops.add(op);
        }
        solutionStepsCopy.setValid(valid);
        return solutionStepsCopy;
    }

    @Override
    public String toString() {
        return ops.toString();
    }

}
