/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import ec.tstoolkit.random.JdkRNG;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SymmetricMatrixTest {

 
    public SymmetricMatrixTest() {
    }

    @Test
    public void testRandomize() {
        FastMatrix S = FastMatrix.square(10);
        SymmetricMatrix.randomize(S, null);
        assertTrue(S.isSymmetric());
    }
    
    @Test
    public void testCholesky(){
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        FastMatrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testInverse(){
        FastMatrix X = FastMatrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        FastMatrix S = SymmetricMatrix.XtX(X);
        FastMatrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        FastMatrix I = SymmetricMatrix.LtL(LowerTriangularMatrix.inverse(T));
        FastMatrix P=GeneralMatrix.AB(I, S);
        assertTrue(P.isDiagonal(1e-9) && P.diagonal().allMatch(x->Math.abs(x-1)<1e-9));
    }
    
    @Test
    public void testXtX(){
        FastMatrix X = FastMatrix.make(2, 4);
        X.set((i, j) -> i+j*10);
        System.out.println(X);
        System.out.println(SymmetricMatrix.XtX(X));
    }
}
