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

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import nbbrd.design.BuilderPattern;
import jdplus.math.matrices.MatrixException;
import demetra.design.AlgorithmImplementation;
import nbbrd.design.Development;
import jdplus.data.normalizer.SafeNormalizer;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.UpperTriangularMatrix;
import jdplus.math.matrices.decomposition.HouseholderWithPivoting;
import jdplus.math.matrices.decomposition.QRDecomposition;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
@Development(status = Development.Status.Release)
public class QRLinearSystemSolver implements LinearSystemSolver {
    
    public static final QRLinearSystemSolver DEFAULT=builder().build();

    @BuilderPattern(QRLinearSystemSolver.class)
    public static class Builder {

        private QRDecomposition.Decomposer decomposer = A -> new HouseholderWithPivoting().decompose(A, 0);
        private double eps=1e-13;
        private boolean normalize=false;

        private Builder() {
        }

        public Builder decomposer(QRDecomposition.Decomposer decomposer) {
            this.decomposer = decomposer;
            return this;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public Builder precision(double eps) {
            this.eps = eps;
            return this;
        }

        public QRLinearSystemSolver build() {
            return new QRLinearSystemSolver(decomposer, eps, normalize);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final QRDecomposition.Decomposer decomposer;
    private final double eps;
    private final boolean normalize;

    private QRLinearSystemSolver(QRDecomposition.Decomposer decomposer, double eps, boolean normalize) {
        this.decomposer = decomposer;
        this.eps = eps;
        this.normalize = normalize;
    }

    @Override
    public void solve(FastMatrix A, DataBlock b) {
        // we normalize b
        FastMatrix An;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            SafeNormalizer sn = new SafeNormalizer();
            int i = 0;
            while (rows.hasNext()) {
                double fac = sn.normalize(rows.next());
                b.mul(i++, fac);
            }
        } else {
            An = A;
        }
        QRDecomposition qr = decomposer.decompose(An);
        int rank = UpperTriangularMatrix.rank(qr.rawR(), eps);
        if (rank != An.getRowsCount()) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        double[] y = b.toArray();
        qr.applyQt(y);
        // Solve R*X = Y;
        UpperTriangularMatrix.solveUx(qr.rawR(), DataBlock.of(y));
        int[] pivot = qr.pivot();
        if (pivot == null) {
            b.copyFrom(y, 0);
        } else {
            for (int i = 0; i < rank; ++i) {
                b.set(pivot[i], y[i]);
            }
        }
    }

    @Override
    public void solve(FastMatrix A, FastMatrix B) {
        // we normalize b
        FastMatrix An;
        double[] factor = null;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            SafeNormalizer sn = new SafeNormalizer();
            factor = new double[A.getRowsCount()];
            int i = 0;
            while (rows.hasNext()) {
                factor[i++] = sn.normalize(rows.next());
            }
        } else {
            An = A;
        }
        QRDecomposition qr = decomposer.decompose(A);
        int rank = UpperTriangularMatrix.rank(qr.rawR(), eps);
        if (rank != A.getRowsCount()) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        DataBlockIterator cols = B.columnsIterator();
        while (cols.hasNext()) {
            DataBlock b = cols.next();
            double[] y = b.toArray();
            qr.applyQt(y);
            // Solve R*X = Y;
            UpperTriangularMatrix.solveUx(qr.rawR(), DataBlock.of(y));
            int[] pivot = qr.pivot();
            if (pivot == null) {
                b.copyFrom(y, 0);
            } else {
                for (int i = 0; i < rank; ++i) {
                    b.set(pivot[i], y[i]);
                }
            }
        }
        if (factor != null) {
            DataBlockIterator rows = B.rowsIterator();
            int r = 0;
            while (rows.hasNext()) {
                rows.next().div(factor[r++]);
            }
        }
    }

}
