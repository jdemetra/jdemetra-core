/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.sp;

import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixDecompositions;
import demetra.maths.matrices.spi.MatrixOperations;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import static jdplus.sp.MatrixOperationsProcessorTest.random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MatrixDecompositionsProcessorTest {

    public MatrixDecompositionsProcessorTest() {
    }

    @Test
    public void testCholesky() {
        int n = 50, m = 20;
        Matrix A = random(m, n);
        Matrix S = MatrixOperations.XXt(A);
        Matrix L = MatrixDecompositions.cholesky(S);
        Matrix P = MatrixOperations.XXt(L);
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < m; ++j) {
                assertEquals(P.get(i, j), S.get(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testCholesky2() {
        // singular matrix
        int n = 20, m = 50;
        Matrix A = random(m, n);
        Matrix S = MatrixOperations.XXt(A);
        Matrix L = MatrixDecompositions.cholesky(S);
        Matrix P = MatrixOperations.XXt(L);
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < m; ++j) {
                assertEquals(P.get(i, j), S.get(i, j), 1e-12);
            }
        }
    }

    public static void main(String[] arg) {
        stressCholesky();
    }

    private static void stressCholesky() {
        int n = 70, m = 50;
        Matrix A = random(m, n);
        Matrix S = MatrixOperations.XXt(A);
        Matrix L = MatrixDecompositions.cholesky(S);
        Matrix P = MatrixOperations.XXt(L);
        CanonicalMatrix a = CanonicalMatrix.of(A);
        CanonicalMatrix s = SymmetricMatrix.XXt(a);
        SymmetricMatrix.lcholesky(s, 1e-9);
        CanonicalMatrix p = SymmetricMatrix.LLt(s);

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            s = SymmetricMatrix.XXt(a);
            SymmetricMatrix.lcholesky(s, 1e-9);
            p = SymmetricMatrix.LLt(s);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

        t0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            S = MatrixOperations.XXt(A);
            L = MatrixDecompositions.cholesky(S);
            P = MatrixOperations.XXt(L);
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
