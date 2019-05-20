/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.maths.matrices.internal;

import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.data.DoubleSeq;
import demetra.maths.matrices.internal.LDLDecomposition;

/**
 *
 * @author palatej
 */
public class LDLDecompositionTest {

    static CanonicalMatrix M(int n) {
        CanonicalMatrix M = CanonicalMatrix.square(n);
        Random rnd = new Random(0);
        M.set((i, j) -> i == j ? 1.0 : (i < j ? 0.0 : rnd.nextDouble()));
        return M;
    }

    @Test
    public void LDLDecompositionTest() {

        for (int n = 3; n < 50; ++n) {
            double[] d = new double[n];
            CanonicalMatrix L = M(n).deepClone();
            for (int i = 0; i < n; ++i) {
                d[i] = i - n / 2;
                if (d[i] == 0) {
                    L.column(i).range(i + 1, n).set(0);
                }
            }
            CanonicalMatrix D = CanonicalMatrix.diagonal(DoubleSeq.of(d));
            CanonicalMatrix S = SymmetricMatrix.XSXt(D, L);
            LDLDecomposition ldl = new LDLDecomposition();
            ldl.decompose(S, 1e-9);
            CanonicalMatrix del = ldl.L().minus(L);
            assertTrue(del.frobeniusNorm() < 1e-9);
        }
    }

    @Test
    public void LDLDecompositionTest2() {

        for (int n = 3; n < 50; ++n) {
            double[] d = new double[n];
            CanonicalMatrix L = M(n).deepClone();
            for (int i = 0; i < n; ++i) {
                d[i] = i % 3 == 0 ? 0 : i - n / 2;
                if (d[i] == 0) {
                    L.column(i).range(i + 1, n).set(0);
                }
            }
            CanonicalMatrix D = CanonicalMatrix.diagonal(DoubleSeq.of(d));
            CanonicalMatrix S = SymmetricMatrix.XSXt(D, L);
            LDLDecomposition ldl = new LDLDecomposition();
            ldl.decompose(S, 1e-9);
            CanonicalMatrix del = ldl.L().minus(L);
            assertTrue(del.frobeniusNorm() < 1e-9);
        }
    }

}