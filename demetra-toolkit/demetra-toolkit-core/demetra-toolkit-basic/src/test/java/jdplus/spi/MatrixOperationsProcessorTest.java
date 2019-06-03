/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.spi;

import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixOperations;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MatrixOperationsProcessorTest {

    public MatrixOperationsProcessorTest() {
    }

    @Test
    public void testInv() {
        int n = 50;
        Matrix M = random(n, n);
        Matrix I = MatrixOperations.inv(M);
        Matrix P = MatrixOperations.times(M, I);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                assertEquals(P.get(i, j), i == j ? 1 : 0, 1e-12);
            }
        }
    }

    static Matrix random(int nr, int nc) {
        double[] data = new double[nr * nc];
        DoubleSeq.Mutable seq = DoubleSeq.Mutable.of(data);
        Random rnd = new Random(0);
        seq.set(rnd::nextDouble);

        return Matrix.ofInternal(data, nr, nc);
    }

}
