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
package jdplus.linearsystem.internal;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.design.BuilderPattern;
import jdplus.math.matrices.MatrixException;
import demetra.design.AlgorithmImplementation;
import demetra.design.Development;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.QRDecomposer;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
@Development(status = Development.Status.Release)
public class QRLinearSystemSolver implements LinearSystemSolver {

    @BuilderPattern(QRLinearSystemSolver.class)
    public static class Builder {

        private final QRDecomposer.Processor qr;
        private boolean normalize;

        private Builder(QRDecomposer.Processor qr) {
            this.qr = qr;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public QRLinearSystemSolver build() {
            return new QRLinearSystemSolver(qr, normalize);
        }
    }

    public static Builder builder(QRDecomposer.Processor qr) {
        return new Builder(qr);
    }
    private final QRDecomposer.Processor qr;
    private final boolean normalize;

    private QRLinearSystemSolver(QRDecomposer.Processor qr, boolean normalize) {
        this.qr = qr;
        this.normalize = normalize;
    }

    @Override
    public void solve(Matrix A, DataBlock b) {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (A.getRowsCount() != b.length()) {
            throw new MatrixException(MatrixException.DIM);
        }
        // we normalize b
        Matrix An;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            DoubleSeqCursor.OnMutable cells = b.cursor();
            while (rows.hasNext()) {
                DataBlock row = rows.next();
                double norm = row.norm2();
                row.div(norm);
                cells.applyAndNext(x -> x / norm);
            }
        } else {
            An = A;
        }
        QRDecomposer decomposition = qr.decompose(An);
        decomposition.leastSquares(b, b, null);
    }

    @Override
    public void solve(Matrix A, Matrix B) {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (A.getRowsCount() != B.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        // we normalize b
        Matrix An;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            DataBlockIterator brows = B.rowsIterator();
            while (rows.hasNext()) {
                DataBlock row = rows.next();
                double norm = row.norm2();
                row.div(norm);
                brows.next().div(norm);
            }
        } else {
            An = A;
        }
        QRDecomposer decomposition = qr.decompose(An);
        if (!decomposition.isFullRank()) {
            throw new MatrixException(MatrixException.SINGULAR);
        }
        B.applyByColumns(col -> decomposition.leastSquares(col, col, null));
    }

}
