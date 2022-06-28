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
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author palatej
 */
public class QRLinearSystemSolverTest {
    
    public QRLinearSystemSolverTest() {
    }

    @Test
    public void testRandom() {
        
        Random rnd=new Random(0);
        int n=10;
        FastMatrix M=FastMatrix.square(n);
        M.set((i, j)->rnd.nextDouble());
        DataBlock x=DataBlock.make(n);
        x.set((DoubleSupplier)rnd::nextDouble);
        DataBlock y=x.deepClone();
        QRLinearSystemSolver solver = QRLinearSystemSolver.builder().decomposer(A->new HouseholderWithPivoting().decompose(A,0)).build();
        solver.solve(M, x);
//        System.out.println(x);
        DataBlock z=DataBlock.make(n);
        z.product(M.rowsIterator(), x);
//        System.out.println(z);
//        System.out.println();
//        System.out.println(y);
//        System.out.println();
//        System.out.println(M);
        assertTrue(z.distance(y)<1e-7);
    }
    
}
