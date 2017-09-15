package org.jeff.game24app;

import org.jeff.game24app.solver.Game24Solver;
import org.jeff.game24app.solver.Rational;
import org.junit.Test;

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
        Game24Solver solver = new Game24Solver(new Rational[] {
                new Rational(1), new Rational(4), new Rational(6), new Rational(1)});
        assertTrue(solver.isSolvable());
        solver = new Game24Solver(new Rational[] {
                new Rational(3), new Rational(3), new Rational(8), new Rational(8)});
        assertTrue(solver.isSolvable());
    }
}