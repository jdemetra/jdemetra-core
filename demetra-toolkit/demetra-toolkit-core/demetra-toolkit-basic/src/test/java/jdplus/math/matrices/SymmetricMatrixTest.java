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
        Matrix S = Matrix.square(10);
        SymmetricMatrix.randomize(S, null);
        assertTrue(S.isSymmetric());
    }
    
    @Test
    public void testCholesky(){
        Matrix X = Matrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);
        Matrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        Matrix del = SymmetricMatrix.LLt(T).minus(S);
        assertTrue(MatrixNorms.absNorm(del) < 1e-9);
    }

    @Test
    public void testInverse(){
        Matrix X = Matrix.make(10, 5);
        JdkRNG rng = JdkRNG.newRandom(0);
        X.set((i, j) -> rng.nextDouble());
        Matrix S = SymmetricMatrix.XtX(X);
        Matrix T = S.deepClone();
        SymmetricMatrix.lcholesky(T);
        Matrix I = SymmetricMatrix.LtL(LowerTriangularMatrix.inverse(T));
        Matrix P=I.times(S);
        assertTrue(P.isDiagonal(1e-9) && P.diagonal().allMatch(x->Math.abs(x-1)<1e-9));
    }
}
