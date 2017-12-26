package org.jeff.game24app;

import org.jeff.game24app.solver.Game24Solver;
import org.jeff.game24app.solver.Operation;
import org.jeff.game24app.solver.Rational;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSolver() throws Exception {
        //Check hard puzzle
        Rational[] puzzle = new Rational[4];
        puzzle[0] = new Rational(3);
        puzzle[1] = new Rational(3);
        puzzle[2] = new Rational(8);
        puzzle[3] = new Rational(8);
        List<Operation[]> solutions = Game24Solver.solve(puzzle);
        assertTrue(!solutions.isEmpty());
        System.out.println(Arrays.toString(solutions.get(0)));

        //There are 1362 solvable puzzles using numbers from 1 to 13
        int numSolvable = 0;
        for (int n0 = 1; n0 <= 13; n0++) {
            for (int n1 = n0; n1 <= 13; n1++) {
                for (int n2 = n1; n2 <= 13; n2++) {
                    for (int n3 = n2; n3 <= 13; n3++) {
                        puzzle[0] = new Rational(n0);
                        puzzle[1] = new Rational(n1);
                        puzzle[2] = new Rational(n2);
                        puzzle[3] = new Rational(n3);
                        if (!Game24Solver.solve(puzzle).isEmpty()) {
                            numSolvable++;
                        }
                    }
                }
            }
        }
        assertEquals(numSolvable, 1362);
    }
}