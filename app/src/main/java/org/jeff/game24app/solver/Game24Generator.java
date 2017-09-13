package org.jeff.game24app.solver;

public class Game24Generator {

    private Game24Solver solver;
    private boolean validPuzzle;
    private Rational[] puzzle;

    public Game24Generator() {

    }

    public Rational[] generateIntPuzzle() {
        validPuzzle = false;
        Rational[] puzzle = new Rational[4];
        while (!validPuzzle) {
            for (int i = 0; i < 4; i++) {
                //random integer from 1 to 10
                int random = ((int) (Math.random() * 10)) + 1;
                puzzle[i] = new Rational(random);
            }
            solver = new Game24Solver(puzzle);
            validPuzzle = solver.isSolvable();
        }
        this.puzzle = puzzle;
        return puzzle;
    }

    public Rational[] restartPuzzle() {
        return puzzle;
    }
}
