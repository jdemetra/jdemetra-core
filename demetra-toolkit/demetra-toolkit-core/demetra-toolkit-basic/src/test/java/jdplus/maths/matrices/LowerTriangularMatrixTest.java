/*
 * Copyright 2016 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.maths.matrices;

import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.data.DataBlock;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class LowerTriangularMatrixTest {

    int N = 30, K = 5000000;
    CanonicalMatrix M;

    public LowerTriangularMatrixTest() {
        double[] x = new double[N * N];
        Random rnd = new Random(0);
        for (int i = 0; i < x.length; ++i) {
            x[i] = rnd.nextDouble();
        }
        M = new CanonicalMatrix(x, N, N);
    }

    @Test
    @Ignore
    public void testFastRSolve() {
        Random rnd = new Random(0);
        double[] b = new double[N];
        DataBlock B = DataBlock.of(b);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            for (int i = 0; i < b.length; ++i) {
                b[i] = rnd.nextDouble();
            }
            LowerTriangularMatrix.rsolve(M, B, 0);
        }

        long t1 = System.currentTimeMillis();
        System.out.println("RSolve");
        System.out.println(t1 - t0);
        System.out.println(B);
    }

    @Test
    @Ignore
    public void testFastLSolve() {
        Random rnd = new Random(0);
        double[] b = new double[N];
        DataBlock B = DataBlock.of(b);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            for (int i = 0; i < b.length; ++i) {
                b[i] = rnd.nextDouble();
            }
            LowerTriangularMatrix.lsolve(M, B, 0);
        }

        long t1 = System.currentTimeMillis();
        System.out.println("LSolve");
        System.out.println(t1 - t0);
        System.out.println(B);
    }

    @Test
    @Ignore
    public void test_RMul() {
        Random rnd = new Random(0);
        double[] b = new double[N];
        DataBlock B = DataBlock.of(b);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            for (int i = 0; i < b.length; ++i) {
                b[i] = rnd.nextDouble();
            }
            LowerTriangularMatrix.rmul(M, B);
        }

        long t1 = System.currentTimeMillis();
        System.out.println("RMul");
        System.out.println(t1 - t0);
        System.out.println(B);
    }

    @Test
    @Ignore
    public void test_LMul() {
        Random rnd = new Random(0);
        double[] b = new double[N];
        DataBlock B = DataBlock.of(b);
        long t0 = System.currentTimeMillis();
        for (int k = 0; k < K; ++k) {
            for (int i = 0; i < b.length; ++i) {
                b[i] = rnd.nextDouble();
            }
            LowerTriangularMatrix.lmul(M, B);
        }

        long t1 = System.currentTimeMillis();
        System.out.println("LMul");
        System.out.println(t1 - t0);
        System.out.println(B);
    }

}