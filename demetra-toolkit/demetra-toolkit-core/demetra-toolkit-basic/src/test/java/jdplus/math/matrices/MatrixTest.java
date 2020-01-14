/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices;

import java.util.Random;
import jdplus.data.LogSign;
import jdplus.math.matrices.decomposition.Gauss;
import jdplus.math.matrices.decomposition.Householder;
import jdplus.math.matrices.decomposition.Householder2;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.math.matrices.decomposition.LUDecomposition;
import jdplus.math.matrices.decomposition.QRDecomposition;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class MatrixTest {

    public MatrixTest() {
    }

    @Test
    public void testDeterminant() {
        Random rnd = new Random(0);
        for (int N = 2; N < 20; ++N) {
            Matrix M = Matrix.square(N);
            M.set((i, j) -> rnd.nextDouble());
            LUDecomposition lu = Gauss.decompose(M);
            LogSign ls = lu.logDeterminant();
            double val = Math.exp(ls.getValue());
            double det1 = ls.isPositive() ? val : -val;

            Householder2 hous = new Householder2();
            QRDecomposition qr = hous.decompose(M);
            ls = LogSign.of(qr.rawRdiagonal(), N%2 == 1);
            double det2 = ls.isPositive() ? val : -val;

            assertEquals(det1, det2, 1e-9);
        }
    }

    @Test
    public void testDeterminant2() {
        Random rnd = new Random(0);
        Matrix M=Matrix.square(2);
        M.set((i, j) -> rnd.nextDouble());
        assertEquals(Matrix.determinant(M), M.get(0,0)*M.get(1, 1)-M.get(0, 1)*M.get(1, 0), 1e-9);
        M=Matrix.square(3);
        M.set((i, j) -> rnd.nextDouble());
        double det=0;
        det+=M.get(0, 0)*M.get(1, 1)*M.get(2, 2);
        det+=M.get(0, 1)*M.get(1, 2)*M.get(2, 0);
        det+=M.get(0, 2)*M.get(1, 0)*M.get(2, 1);
        det-=M.get(0, 2)*M.get(1, 1)*M.get(2, 0);
        det-=M.get(0, 1)*M.get(1, 0)*M.get(2, 2);
        det-=M.get(0, 0)*M.get(1, 2)*M.get(2, 1);
        assertEquals(Matrix.determinant(M), det, 1e-9);
    }
}
