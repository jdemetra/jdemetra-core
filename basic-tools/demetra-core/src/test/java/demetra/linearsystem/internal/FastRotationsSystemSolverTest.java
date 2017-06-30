/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.linearsystem.internal;

import demetra.data.DataBlock;
import demetra.linearsystem.ILinearSystemSolver;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastRotationsSystemSolverTest {

    public FastRotationsSystemSolverTest() {
    }

    @Test
    public void testArray() {
        Matrix A = Matrix.square(5);
        Random rnd = new Random(0);
        A.set(rnd::nextDouble);
        DataBlock x = DataBlock.make(A.getColumnsCount());
        x.set(1);
        FastRotationsSystemSolver solver = new FastRotationsSystemSolver();
        solver.solve(A, x);
        DataBlock y = DataBlock.make(A.getColumnsCount());
        y.set(1);
        ILinearSystemSolver.fastSolver().solve(A, y);
        assertTrue(x.distance(y)<1e-9);
    }

}
