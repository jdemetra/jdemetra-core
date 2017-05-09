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
package demetra.maths.matrices.impl;

import demetra.maths.matrices.impl.FastLowerTriangularMatrixAlgorithms;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import java.util.Random;
import demetra.maths.matrices.impl.FastLowerTriangularMatrixAlgorithms;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class FastLowerTriangularMatrixAlgorithmsTest {
    
    final int N = 30;
    final Matrix M, T, S, ST;
    final DataBlock X,Y;

    public FastLowerTriangularMatrixAlgorithmsTest() {
        double[] x = new double[N * N];
        Random rnd = new Random(0);
        for (int i = 0; i < x.length; ++i) {
            x[i] = rnd.nextDouble();
        }
        M = Matrix.builder(x).nrows(N).ncolumns(N).build();
        T=M.transpose();
        S=M.extract(N/4, 3*N/4, N/4, 3*N/4);
        ST=S.transpose();
        
        X=DataBlock.make(N);
        Y=DataBlock.make(S.getRowsCount());
        X.set(rnd::nextDouble);
        Y.set(rnd::nextDouble);
    }

    @Test
    public void testRMul() {
        DataBlock x1=DataBlock.copyOf(X);
        DataBlock x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_row(M, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_column(M, x2);
        assertTrue(x1.distance(x2)<1e-9);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_row(T, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_column(T, x2);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_row(S, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_column(S, x2);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_row(ST, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rmul_column(ST, x2);
        assertTrue(x1.distance(x2)<1e-9);
    }
    
    @Test
    public void testLMul() {
        DataBlock x1=DataBlock.copyOf(X);
        DataBlock x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_row(M, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_column(M, x2);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(X);
        x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_row(T, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_column(T, x2);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_row(S, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_column(S, x2);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_row(ST, x1);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lmul_column(ST, x2);
        assertTrue(x1.distance(x2)<1e-9);
    }
    
    @Test
    public void testRSolve() {
        DataBlock x1=DataBlock.copyOf(X);
        DataBlock x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_row(M, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_column(M, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(X);
        x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_row(T, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_column(T, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_row(S, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_column(S, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_row(ST, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.rsolve_column(ST, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
    }

    @Test
    public void testLSolve() {
        DataBlock x1=DataBlock.copyOf(X);
        DataBlock x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_row(M, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_column(M, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(X);
        x2=DataBlock.copyOf(X);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_row(T, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_column(T, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_row(S, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_column(S, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
        x1=DataBlock.copyOf(Y);
        x2=DataBlock.copyOf(Y);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_row(ST, x1,0);
        FastLowerTriangularMatrixAlgorithms.INSTANCE.lsolve_column(ST, x2, 0);
        assertTrue(x1.distance(x2)<1e-9);
    }
}
