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
package jdplus.benchmarking.univariate;

import demetra.benchmarking.univariate.DentonSpec;
import demetra.data.AggregationType;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.linearsystem.LinearSystemSolver;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.SymmetricMatrix;
import jdplus.maths.polynomials.Polynomial;
import jdplus.maths.polynomials.UnitRoots;
import demetra.data.DoubleSeq;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class MatrixDenton {

    private final boolean multiplicative, modified;
    private final int differencing, conversion, offset;
    private final AggregationType type;

    public MatrixDenton(DentonSpec spec, int conversion, int offset) {
        this.conversion = conversion;
        this.offset = offset;
        this.multiplicative = spec.isMultiplicative();
        this.modified = spec.isModified();
        this.differencing = spec.getDifferencing();
        this.type = spec.getAggregationType();
    }

    private void J(FastMatrix M) {
        int j = offset;
        DataBlockIterator rows = M.rowsIterator();
        while (rows.hasNext()) {
            switch (type) {
                case Sum:
                case Average:
                    rows.next().range(j, j + conversion).set(1);
                    break;
                case First:
                    rows.next().set(j, 1);
                    break;
                case Last:
                    rows.next().set(j + conversion - 1, 1);
                    break;
            }
            j += conversion;
        }
    }

    private CanonicalMatrix D(DataBlock x) {
        Polynomial pd = UnitRoots.D(1, differencing);
        int d = pd.degree();
        int n = x.length();
        if (multiplicative) {
            x = DataBlock.of(x);
            x.apply(z -> 1 / z);
        }

        if (modified) {
            CanonicalMatrix D = CanonicalMatrix.make(n - d, n);
            for (int i = 0; i <= d; ++i) {
                if (multiplicative) {
                    D.subDiagonal(i).setAY(pd.get(d - i), x.drop(i, d - i));
                } else {
                    D.subDiagonal(i).set(pd.get(d - i));
                }
            }
            return D;
        } else {
            CanonicalMatrix D = CanonicalMatrix.square(n);
            for (int i = 0; i <= d; ++i) {
                if (multiplicative) {
                    D.subDiagonal(-i).setAY(pd.get(i), x.drop(0, i));
                } else {
                    D.subDiagonal(-i).set(pd.get(i));
                }
            }
            return D;
        }
    }

    public double[] process(DoubleSeq highSeries, DoubleSeq lowSeries) {

        DataBlock x = DataBlock.of(highSeries), y = DataBlock.of(lowSeries);
        if (type == AggregationType.Average) {
            y.mul(conversion);
        }
        int n = x.length();
        int ny = y.length();

        double xm = x.sum() / x.length();
        x.mul(1 / xm);

        CanonicalMatrix D = D(x);

        CanonicalMatrix A = CanonicalMatrix.square(n + ny);

        SymmetricMatrix.XtX(D, A.extract(0, n, 0, n));
        J(A.extract(n, ny, 0, n));
        CanonicalMatrix B = A.deepClone();
        J(A.extract(0, n, n, ny).transpose());
        B.diagonal().drop(n, 0).set(1);

        DataBlock q = DataBlock.make(n + ny);
        DataBlock q0 = q.range(0, n);
        q0.copy(x);
        DataBlock q1 = q.range(n, n + ny);
        q1.product(A.extract(n, ny, 0, n).rowsIterator(), q0);
        q1.chs();
        q1.addAY(1.0 / xm, y);

        DataBlock z = DataBlock.make(n + ny);
        z.product(B.rowsIterator(), q);
        LinearSystemSolver.fastSolver().solve(A, z);
        DataBlock rslt = z.range(0, n);
        rslt.mul(xm);
        return rslt.toArray();
    }

    public double[] process(DoubleSeq lowSeries) {
        int ny = lowSeries.length();
        int n = ny * conversion;

        DataBlock x = DataBlock.make(n), y = DataBlock.of(lowSeries);
        if (type == AggregationType.Average) {
            y.mul(conversion);
        }
        if (multiplicative) {
            x.set(1);
        }
        CanonicalMatrix D = D(x);
        CanonicalMatrix A = CanonicalMatrix.square(n + ny);

        SymmetricMatrix.XtX(D, A.extract(0, n, 0, n));
        J(A.extract(n, ny, 0, n));
        CanonicalMatrix B = A.deepClone();
        J(A.extract(0, n, n, ny).transpose());
        B.diagonal().drop(n, 0).set(1);

        DataBlock q = DataBlock.make(n + ny);
        DataBlock q1 = q.range(n, n + ny);
        q1.copy(y);
        DataBlock z = DataBlock.make(n + ny);
        z.product(B.rowsIterator(), q);
        LinearSystemSolver.fastSolver().solve(A, z);
        return z.range(0, n).toArray();
    }

    public boolean isMultiplicative() {
        return multiplicative;
    }

    public boolean isModified() {
        return modified;
    }

    public int getConversionFactor() {
        return conversion;
    }

    public int getDifferencingOrder() {
        return differencing;
    }

    public AggregationType getAggregationType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

}
