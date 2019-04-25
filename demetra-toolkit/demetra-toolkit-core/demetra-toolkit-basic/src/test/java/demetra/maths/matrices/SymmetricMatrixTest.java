/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.matrices;

import demetra.data.LogSign;
import demetra.data.accumulator.NeumaierAccumulator;
import demetra.maths.Constants;
import demetra.random.MersenneTwister;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import demetra.random.RandomNumberGenerator;

/**
 *
 * @author Jean Palate
 */
public class SymmetricMatrixTest {

    public SymmetricMatrixTest() {
    }

    @Test
    public void testRandom() {
        FastMatrix Q = FastMatrix.square(10);
        SymmetricMatrix.randomize(Q, MersenneTwister.fromSystemNanoTime());
        assertTrue(Q.isSymmetric());
    }

    @Test
    public void testXXt() {
        int N = 20, M = 10;
        FastMatrix X = FastMatrix.make(N, M);
        Random rnd = new Random(0);
        X.set((i, j) -> rnd.nextDouble());
        ec.tstoolkit.maths.matrices.Matrix O = new ec.tstoolkit.maths.matrices.Matrix(N, M);
        Random ornd = new Random(0);
        O.set((i, j) -> ornd.nextDouble());
        assertTrue(MatrixComparator.distance(X, O) == 0);
        assertTrue(MatrixComparator.distance(SymmetricMatrix.XXt(X), ec.tstoolkit.maths.matrices.SymmetricMatrix.XXt(O)) == 0);
        assertTrue(MatrixComparator.distance(SymmetricMatrix.XtX(X), ec.tstoolkit.maths.matrices.SymmetricMatrix.XtX(O)) == 0);
        FastMatrix R = SymmetricMatrix.robustXtX(X, new NeumaierAccumulator());
        assertTrue(MatrixComparator.distance(SymmetricMatrix.XtX(X), R) < 1e-9);
    }

    @Test
    public void testCholeskySingular1() {
        FastMatrix A = FastMatrix.make(6, 4);
        A.column(0).set(1);
        A.column(1).set(2);
        A.column(2).set(i -> i);
        A.column(3).set(A.column(0), A.column(2), (x, y) -> x + 3 * y);
        FastMatrix X = SymmetricMatrix.XtX(A);
        SymmetricMatrix.lcholesky(X, 1e-9);
        FastMatrix I = FastMatrix.square(X.getRowsCount());
        I.diagonal().set(X.diagonal(), x -> x == 0 ? 0 : 1);
        LowerTriangularMatrix.rsolve(X, I, 1e-9);
        FastMatrix IX1 = SymmetricMatrix.LtL(I);

        FastMatrix B = FastMatrix.make(6, 2);
        B.column(0).set(1);
        B.column(1).set(i -> i);
        X = SymmetricMatrix.XtX(B);
        SymmetricMatrix.lcholesky(X, Constants.getEpsilon());
        I = FastMatrix.identity(X.getRowsCount());
        LowerTriangularMatrix.rsolve(X, I, Constants.getEpsilon());
        FastMatrix IX2 = SymmetricMatrix.LtL(I);
        assertEquals(IX1.get(0, 0), IX2.get(0, 0), 1e-9);
        assertEquals(IX1.get(2, 2), IX2.get(1, 1), 1e-9);
        assertEquals(IX1.get(0, 2), IX2.get(0, 1), 1e-9);
    }

    @Test
    public void testCholeskySingular2() {
        FastMatrix A = FastMatrix.make(6, 4);
        A.column(0).set(1);
        A.column(1).set(2);
        A.column(2).set(i -> i * i);
        A.column(3).set(A.column(0), A.column(2), (x, y) -> x + 3 * y);
        FastMatrix X = SymmetricMatrix.XtX(A);
        SymmetricMatrix.lcholesky(X, 1e-9);
        FastMatrix I = FastMatrix.square(X.getRowsCount());
        I.diagonal().set(X.diagonal(), x -> x == 0 ? 0 : 1);
        LowerTriangularMatrix.lsolve(X, I, 1e-9);
        FastMatrix IX1 = SymmetricMatrix.LtL(I);

        FastMatrix B = FastMatrix.make(6, 2);
        B.column(0).set(1);
        B.column(1).set(i -> i * i);
        X = SymmetricMatrix.XtX(B);
        SymmetricMatrix.lcholesky(X, Constants.getEpsilon());
        I = FastMatrix.identity(X.getRowsCount());
        LowerTriangularMatrix.lsolve(X, I, Constants.getEpsilon());
        FastMatrix IX2 = SymmetricMatrix.LtL(I);
        assertEquals(IX1.get(0, 0), IX2.get(0, 0), 1e-9);
        assertEquals(IX1.get(2, 2), IX2.get(1, 1), 1e-9);
        assertEquals(IX1.get(0, 2), IX2.get(0, 1), 1e-9);
    }

    @Test
    public void testCholesky() {
        FastMatrix A = FastMatrix.make(6, 2);
        A.column(0).set(1);
        A.column(1).set(i -> i);
        FastMatrix X = SymmetricMatrix.XtX(A);
        SymmetricMatrix.lcholesky(X, Constants.getEpsilon());
        FastMatrix I = FastMatrix.square(X.getRowsCount());
        I.diagonal().set(X.diagonal(), x -> x == 0 ? 0 : 1);
        LowerTriangularMatrix.lsolve(X, I, Constants.getEpsilon());
        FastMatrix IX = SymmetricMatrix.LtL(I);
//        System.out.println(IX);
    }

    @Test
    public void testReenforce() {
        FastMatrix Q = FastMatrix.square(20);
        RandomNumberGenerator rnd = MersenneTwister.fromSystemNanoTime();
        Q.set((i, j) -> rnd.nextDouble());
        SymmetricMatrix.reenforceSymmetry(Q);
        assertTrue(Q.isSymmetric(0));
    }

    @Test
    public void testDeterminant() {
        FastMatrix X = FastMatrix.make(30, 50);
        Random rnd = new Random(0);
        X.set(rnd::nextDouble);
        FastMatrix S = SymmetricMatrix.XXt(X);
        LogSign d1 = FastMatrix.logDeterminant(S);
        LogSign d2 = SymmetricMatrix.logDeterminant(S);
        assertEquals(d1.getValue(), d2.getValue(), 1e-6);
    }

    @Test
    public void stressTestCholesky() {
        FastMatrix A = FastMatrix.make(60, 10);
        Random rnd = new Random();
        A.set(rnd::nextDouble);
        long t0=System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            FastMatrix X = SymmetricMatrix.XtX(A);
            SymmetricMatrix.lcholesky(X, Constants.getEpsilon());
        }
        long t1=System.currentTimeMillis();
        System.out.println(t1-t0);
    }

}
