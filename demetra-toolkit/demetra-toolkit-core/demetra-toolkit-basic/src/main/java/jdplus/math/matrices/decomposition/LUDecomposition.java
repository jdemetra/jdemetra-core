/*
* Copyright 2013 National Bank of Belgium
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
package jdplus.math.matrices.decomposition;

import demetra.data.DoubleSeq;
import demetra.design.Development;
import jdplus.data.DataBlock;
import jdplus.data.LogSign;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.UpperTriangularMatrix;

/**
 * The LU decomposition with partial pivoting and row interchanges is
 * used to factor A as A = P * L * U,
 * where P is a permutation matrix, L is unit lower triangular, and U is
 * upper triangular.
 * The factored form of A is then used to solve the system of equations A * X =
 * B.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)

public class LUDecomposition {
    
    @FunctionalInterface
    public static interface Decomposer{
        LUDecomposition decompose(Matrix A, double eps);
    }

    private final Matrix lu;
    private final int[] pivot;

    public LUDecomposition(Matrix lu, int[] pivot) {
        this.lu = lu;
        this.pivot = pivot;
    }

    public LUDecomposition(Matrix lu) {
        this.lu = lu;
        this.pivot = null;
    }

    public LogSign logDeterminant() {
        return LogSign.of(lu.diagonal(), pivotSign() == -1);
    }

    public DoubleSeq uDiagonal() {
        return lu.diagonal();
    }

    public Matrix lu() {
        return lu;
    }

    /**
     * The pivot indices that define the permutation matrix P;
     * row i of the matrix was interchanged with row pivot(i)
     *
     * @return
     */
    public int[] pivot() {
        return pivot;
    }

    /**
     * Sign of the permutation
     *
     * @return
     */
    public int pivotSign() {
        if (pivot == null) {
            return 1;
        }
        int n = 1;
        for (int i = 0; i < pivot.length; ++i) {
            if (pivot[i] != i) {
                n = -n;
            }
        }
        return n;
    }

    /**
     *
     * @return
     * reversePivot[j] == i means : the i-th row of the LU decomposition
     * corresponds to the j-th row of the initial matrix.
     */
    public int[] reversePivot() {
        if (pivot == null) {
            return null;
        }
        int[] rpiv = new int[pivot.length];
        for (int i = 0; i < pivot.length; ++i) {
            rpiv[pivot[i]] = i;
        }
        return rpiv;

    }

    /**
     * Solve the system Ax=b
     * where A is described by this LU decomposition (A = P*L*U)
     *
     * @param b
     */
    public void solve(DataBlock b) {
        if (pivot != null) {
            swap(b);
        }
        UpperTriangularMatrix.solveUx(lu, b);
        LowerTriangularMatrix.solveL1x(lu, b);
    }

    public void solve(Matrix B) {
        if (pivot != null) {
            swap(B);
        }
        UpperTriangularMatrix.solveUX(lu, B);
        LowerTriangularMatrix.solveL1X(lu, B);
    }

    private void swap(DataBlock b) {
        double[] pb = b.getStorage();
        int start = b.getStartPosition();
        int inc = b.getIncrement();
        if (inc == 1) {
            for (int i = 0; i < pivot.length; ++i) {
                int ip = pivot[i];
                if (ip != i) {
                    int j = start + i, jp = start + ip;
                    double tmp = pb[j];
                    pb[j] = pb[jp];
                    pb[jp] = tmp;
                }
            }
        } else {
            for (int i = 0; i < pivot.length; ++i) {
                int ip = pivot[i];
                if (ip != i) {
                    int j = start + i * inc, jp = start + ip * inc;
                    double tmp = pb[j];
                    pb[j] = pb[jp];
                    pb[jp] = tmp;
                }
            }
        }
    }

    private void swap(Matrix B) {
        double[] pb = B.getStorage();
        int start = B.getStartPosition(), lda = B.getColumnIncrement();
        int n = B.getColumnsCount();
        for (int i = 0, rmax = start + n * lda; i < pivot.length; ++i, ++rmax) {
            int ip = pivot[i];
            if (ip != i) {
                for (int j = start + i, jp = start + ip; j < rmax; j += lda, jp += lda) {
                    double tmp = pb[j];
                    pb[j] = pb[jp];
                    pb[jp] = tmp;
                }
            }
        }
    }
}
