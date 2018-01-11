package org.jeff.game24app.solver;

import java.util.List;

/**
 * A class that handles random generation of 24 puzzles.
 */
public class Game24Generator {

    private boolean validPuzzle;
    private Rational[] puzzle;
    private boolean fractional;

    /**
     * Constructor for puzzle generator
     *
     * @param fractional whether to generate fractional puzzles
     */
    public Game24Generator(boolean fractional) {
        this.fractional = fractional;
    }

    /**
     * Generates a puzzle depending on fractional.
     *
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
     *
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
            validPuzzle = Game24Solver.isSolvable(puzzle);
        }
        this.puzzle = puzzle;
        return puzzle;
    }

    /**
     * Generates a puzzle that includes fractions.
     *
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
            validPuzzle = Game24Solver.isSolvable(puzzle);
        }
        this.puzzle = puzzle;
        return puzzle;
    }

    /**
     * Returns a unique int that represents a puzzle.
     * Since a puzzle consists of 4 rationals with numerator and denominator at most 10,
     * the puzzle can be encoded in a single integer. The first 4 bits are the 1st
     * numerator, the second 4 bits are the 1st denominator, and so on.
     *
     * @param puzzle The puzzle to hash
     * @return The unique hash
     */
    public static int hashToInt(Rational[] puzzle) {
        int puzzleEncoded = 0;
        for (int i = 0; i < 4; i++) {
            puzzleEncoded <<= 4;
            puzzleEncoded |= puzzle[i].getNumerator();
            puzzleEncoded <<= 4;
            puzzleEncoded |= puzzle[i].getDenominator();
        }
        return puzzleEncoded;
    }

    /**
     * Reverses a hash encoding described by {@link #hashToInt(Rational[])}.
     * If hash is zero, returns null.
     *
     * @param hash The hash to reverse
     * @return The puzzle that has the hash
     */
    public static Rational[] reverseHash(int hash) {
        Rational[] puzzle = new Rational[4];
        for (int i = 0; i < 4; i++) {
            int numerator = hash >> (28 - (i * 8)) & 0xF;
            int denominator = hash >> (24 - (i * 8)) & 0xF;
            puzzle[i] = new Rational(numerator, denominator);
        }
        return puzzle;
    }

    /**
     * Returns the current puzzle.
     *
     * @return the puzzle
     */
    public Rational[] restartPuzzle() {
        return puzzle;
    }

    /**
     * Returns a hint containing the next step in a solution for a puzzle.
     * The puzzle may be partially completed and thus unsolvable.
     *
     * @param puzzle the puzzle to give a hint for
     * @return the hint as an Operation, or null if unsolvable
     */
    public static Operation getHint(Rational[] puzzle) {
        if (puzzle.length <= 1) return null; //Already solved or unsolvable
        List<Operation[]> solutions = Game24Solver.solve(puzzle);
        if (solutions.isEmpty()) {
            return null;
        }
        //If possible, try to pick a solution that doesn't involve fractions
        //and doesn't have big numbers.
        Operation[] hintSolution = solutions.get(0);
        for (Operation[] s : solutions) {
            hintSolution = s;
            boolean nice = true;
            for (Operation op : s) {
                Rational eval = op.evaluate();
                //Solution involves fractions
                if (eval.getDenominator() != 1) {
                    nice = false;
                }
                //Solution involves big numbers
                if (eval.getFloatValue() > 50) {
                    nice = false;
                }
            }
            if (nice) {
                break;
            }
        }
        return hintSolution[0];
    }
}
