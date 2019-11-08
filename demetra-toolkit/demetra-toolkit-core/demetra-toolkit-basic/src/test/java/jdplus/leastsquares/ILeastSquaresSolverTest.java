/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.leastsquares;

import jdplus.leastsquares.internal.DefaultQRSolver;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.Matrix;
import jdplus.maths.matrices.decomposition.Householder;
import jdplus.maths.matrices.decomposition.HouseholderWithPivoting;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ILeastSquaresSolverTest {
    
    public ILeastSquaresSolverTest() {
    }

    @Test
    @Ignore
    public void testPerformance() {
        int N=300, M=20, K=10000;
        FastMatrix A=Matrix.make(N, M);
        Random rnd=new Random(0);
        A.set(rnd::nextDouble);
        DataBlock y=DataBlock.make(N);
        y.set(rnd::nextDouble);
        long t0=System.currentTimeMillis();
        for (int i=0; i<K; ++i){
            DefaultQRSolver qr= new DefaultQRSolver();
            qr.solve(y, A);
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
        for (int i=0; i<K; ++i){
            DefaultQRSolver qr= new DefaultQRSolver(new HouseholderWithPivoting.Processor());
            qr.solve(y, A);
        }
        t1=System.currentTimeMillis();
        System.out.println(t1-t0);
    }
    
}
