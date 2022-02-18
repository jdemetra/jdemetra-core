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
package jdplus.math.linearsystem;

import nbbrd.design.Development;
import java.util.Arrays;
import jdplus.data.DataBlock;
import demetra.design.AlgorithmImplementation;
import demetra.math.Constants;
import jdplus.math.matrices.MatrixException;
import jdplus.math.matrices.FastMatrix;

/**
 * LU factorization with column pivoting
 * This class is a translation of the code used in Seats (routine MLTSOL)
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
public final class LULinearSystemSolver2 implements LinearSystemSolver {
    
    private final double eps;
    
    public LULinearSystemSolver2(double eps){
        this.eps=eps;
    }

   public LULinearSystemSolver2(){
        this.eps=Constants.getEpsilon();
    }

    @Override
    public void solve(FastMatrix A, DataBlock b) throws MatrixException {
        int n = A.getRowsCount();
        if (b.length() != n) {
            throw new MatrixException(MatrixException.DIM);
        }
        FastMatrix col = FastMatrix.columnOf(b);
        solve(A, col);
        b.copy(col.column(0));
    }

    @Override
    public void solve(FastMatrix A, FastMatrix B) throws MatrixException {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.DIM);
        }

        int n = A.getRowsCount(), nl = n + B.getColumnsCount();
        if (B.getRowsCount() != n) {
            throw new MatrixException(MatrixException.DIM);
        }
        double[] X = new double[n * nl];
        A.copyTo(X, 0);
        B.copyTo(X, n * n);
        int[] m = new int[nl];
        Arrays.fill(m, -1);

        for (int i = 0; i < n; ++i) {

            // search for max value in the row
            double u = Double.MIN_VALUE;
            int c = -1;

            for (int k = 0, ik = i; k < n; ++k, ik += n) {
                if (m[k] == -1) // unused
                {
                    double a = X[ik];
                    double absa = Math.abs(a);
                    if (absa > u) {
                        c = k;
                        u = absa;
                    }
                }
            }
            if (c < 0) {
                throw new MatrixException(); // should not happen
            }
            m[c] = i;
            // current pivot 
            int ic = i + c * n;
            double xmax = X[ic];
            if (Math.abs(xmax) < eps) {
                throw new MatrixException(MatrixException.SINGULAR);
            }
            double pivot = 1 / xmax;
            for (int j = 0, jc = c * n; j < n; ++j, ++jc) {
                if (j != i) {
                    double a = X[jc];
                    if (a != 0) {
                        double fac = pivot * a;
                        for (int k = 0, ik = i, jk = j; k < nl; ++k, ik += n, jk += n) {
                            if (m[k] == -1) {
                                double aik = X[ik];
                                X[jk] -= fac * aik;
                            }
                        }
                    }
                }
            }
            for (int k = 0, ik = i; k < nl; ++k, ik += n) {
                if (m[k] == -1) {
                    X[ik] *= pivot;
                }
            }
        }
        int kmax = n * nl;
        for (int l = 0, kn = n * n; kn < kmax; ++l, kn += n) {
            for (int i = 0; i < n; ++i) {
                if (m[i] != -1) {
                    B.set(i, l, X[m[i] + kn]);
                }
            }
        }
    }
}
