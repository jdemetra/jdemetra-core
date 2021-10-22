/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

import jdplus.math.matrices.DataPointer;
import jdplus.math.matrices.MatrixNorms;
import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.SymmetricMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class SYRKTest {

    public SYRKTest() {
    }

    @Test
    public void testXXt() {
        FastMatrix S = FastMatrix.square(10);
        S.set((i, j) -> i + j + 1);
        FastMatrix T = S.deepClone();
        DataBlock X = DataBlock.make(10);
        X.set(i -> i + 1);
        SYRK.laddaXXt(10, DataPointer.of(X), S);
        SymmetricMatrix.fromLower(S);
        SYRK.uaddaXXt(10, DataPointer.of(X), T);
        SymmetricMatrix.fromUpper(T);
        assertTrue(MatrixNorms.absNorm(S.minus(T)) < 1e-9);
    }

    @Test
    public void testXXt2() {
        FastMatrix S = FastMatrix.square(10);
        S.set((i, j) -> i + j + 1);
        FastMatrix T = S.deepClone();
        DataBlock X = DataBlock.make(10);
        X.set(i -> i + 1);
        SYRK.laddaXXt(10, DataPointer.of(X.reverse()), S);
        SymmetricMatrix.fromLower(S);
        SYRK.uaddaXXt(10, DataPointer.of(X.reverse()), T);
        SymmetricMatrix.fromUpper(T);
        assertTrue(MatrixNorms.absNorm(S.minus(T)) < 1e-9);
    }
}
