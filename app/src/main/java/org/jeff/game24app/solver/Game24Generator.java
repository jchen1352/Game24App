package org.jeff.game24app.solver;

/**
 * A class that handles random generation of 24 puzzles.
 */
public class Game24Generator {

    private Game24Solver solver;
    private boolean validPuzzle;
    private Rational[] puzzle;
    private boolean fractional;

    /**
     * Constructor for puzzle generator
     * @param fractional whether to generate fractional puzzles
     */
    public Game24Generator(boolean fractional) {
        this.fractional = fractional;
    }

    /**
     * Generates a puzzle depending on fractional.
     * @return an array of 4 Rationals that forms a puzzle
     */
    public Rational[] generatePuzzle() {
        if (fractional) {
            return generateFracPuzzle();
        }
        return generateIntPuzzle();
    }

    /**
     * Generates a puzzle with only integers.
     * (But the solution may require fractions)
     * @return an array of 4 Rationals that forms a puzzle
     */
    private Rational[] generateIntPuzzle() {
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

    /**
     * Generates a puzzle that includes fractions.
     * @return an array of 4 Rationals that forms a puzzle
     */
    private Rational[] generateFracPuzzle() {
        validPuzzle = false;
        Rational[] puzzle = new Rational[4];
        while (!validPuzzle) {
            for (int i = 0; i < 4; i++) {
                //random integer from 1 to 10 for numerator and denominator
                int numerator = ((int) (Math.random() * 10)) + 1;
                int denominator = ((int) (Math.random() * 10)) + 1;
                puzzle[i] = new Rational(numerator, denominator);
            }
            solver = new Game24Solver(puzzle);
            validPuzzle = solver.isSolvable();
        }
        this.puzzle = puzzle;
        return puzzle;
    }

    /**
     * Returns the current puzzle.
     * @return the puzzle
     */
    public Rational[] restartPuzzle() {
        return puzzle;
    }

}
