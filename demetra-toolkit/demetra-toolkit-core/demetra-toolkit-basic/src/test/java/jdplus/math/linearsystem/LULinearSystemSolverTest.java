/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.linearsystem;

import java.util.Random;
import java.util.function.DoubleSupplier;

import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.decomposition.CroutDoolittle;
import jdplus.math.matrices.decomposition.Gauss;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class LULinearSystemSolverTest {

    public LULinearSystemSolverTest() {
    }

    @Test
    public void testRandom() {

        Random rnd = new Random(0);
        int n = 4;
        FastMatrix M = FastMatrix.square(n);
        M.set((i, j) -> rnd.nextDouble());
        DataBlock x = DataBlock.make(n);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = x.deepClone();
        LULinearSystemSolver solver = LULinearSystemSolver.builder().decomposer((A, e) -> CroutDoolittle.decompose(A, e)).build();
        solver.solve(M, x);
        DataBlock z = DataBlock.make(n);
        z.product(M.rowsIterator(), x);
        assertTrue(z.distance(y) < 1e-9);
    }

    @Test
    public void testSingluar() {

        Random rnd = new Random(0);
        int n = 40;
        FastMatrix M = FastMatrix.square(n);
        M.set((i, j) -> rnd.nextDouble());
        DataBlock x = DataBlock.make(n);
        M.column(36).set(M.column(3));
        M.column(36).addAY(.002, M.column(12));
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y = x.deepClone();
        LULinearSystemSolver2 solver2 = new LULinearSystemSolver2(1e-13);
        boolean ok=true;
        try{
        solver2.solve(M, x);
        }catch (MatrixException err){
            ok=false;
        }
         assertTrue(!ok);
        LULinearSystemSolver solver = LULinearSystemSolver.builder().decomposer((A, e) -> CroutDoolittle.decompose(A, e)).precision(1e-13).build();
        ok=true;
        try{
        solver.solve(M, x);
        }catch (MatrixException err){
            ok=false;
        }
         assertTrue(!ok);
    }
    
    public static void main(String[] args) {
        Random rnd = new Random(0);
        int n = 20;
        FastMatrix M = FastMatrix.square(n);
        M.set((i, j) -> rnd.nextDouble());
        DataBlock x = DataBlock.make(n);
        x.set((DoubleSupplier)rnd::nextDouble);

        int K = 1000000;
        long t0 = System.currentTimeMillis();
        for (int k   = 0; k < K; ++k) {
            LULinearSystemSolver solver = LULinearSystemSolver.builder().decomposer((A, e) -> CroutDoolittle.decompose(A, e)).build();
            solver.solve(M, x.deepClone());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            LULinearSystemSolver solver = LULinearSystemSolver.builder().decomposer((A, e) -> Gauss.decompose(A, e)).build();
            solver.solve(M, x.deepClone());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            new LULinearSystemSolver2().solve(M, x.deepClone());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
