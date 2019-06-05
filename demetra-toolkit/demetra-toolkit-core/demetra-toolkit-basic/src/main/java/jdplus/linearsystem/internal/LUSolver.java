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
import jdplus.data.accumulator.NeumaierAccumulator;
import demetra.design.BuilderPattern;
import jdplus.maths.matrices.MatrixException;
import demetra.design.AlgorithmImplementation;
import demetra.design.Development;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.decomposition.LUDecomposition;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
@Development(status = Development.Status.Release)
public class LUSolver implements LinearSystemSolver {

    @BuilderPattern(LUSolver.class)
    public static class Builder {

        private final LUDecomposition lu;
        private boolean improve, normalize;

        private Builder(LUDecomposition lu) {
            this.lu = lu;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public Builder improve(boolean improve) {
            this.improve = improve;
            return this;
        }

        public LUSolver build() {
            return new LUSolver(lu, normalize, improve);
        }
    }

    public static Builder builder(LUDecomposition lu) {
        return new Builder(lu);
    }

    private final LUDecomposition lu;
    private final boolean improve, normalize;

    private LUSolver(LUDecomposition lu, boolean normalize, boolean improve) {
        this.lu = lu;
        this.improve = improve;
        this.normalize = normalize;
    }

    @Override
    public void solve(FastMatrix A, DataBlock b) {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (A.getRowsCount() != b.length()) {
            throw new MatrixException(MatrixException.DIM);
        }
        // we normalize b
        FastMatrix An;
        if (normalize) {

            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            DoubleSeqCursor.OnMutable cells = b.cursor();
            while (rows.hasNext()) {
                DataBlock row = rows.next();
                double norm = row.norm2()/Math.sqrt(row.length());
                row.div(norm);
                cells.applyAndNext(x -> x / norm);
            }
        } else {
            An = A;
        }
        lu.decompose(An);
        DataBlock b0 = null;
        if (improve) {
            b0 = DataBlock.of(b);
        }
        lu.solve(b);
        // improve the result
        if (!improve) {
            return;
        }
        DataBlock db = DataBlock.make(b.length());
        db.robustProduct(An.rowsIterator(), b, new NeumaierAccumulator());
        db.sub(b0);
        lu.solve(db);
        b.sub(db);
    }

    @Override
    public void solve(FastMatrix A, FastMatrix B) {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (A.getRowsCount() != B.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        // we normalize b 
        FastMatrix An;
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
        lu.decompose(An);
        CanonicalMatrix B0 = improve ? B.deepClone() : null;
        lu.solve(B);
        if (!improve) {
            return;
        }
        // improve the result
        CanonicalMatrix DB = CanonicalMatrix.make(B.getRowsCount(), B.getColumnsCount());
        DB.robustProduct(An, B, new NeumaierAccumulator());
        DB.sub(B0);
        lu.solve(DB);
        B.sub(DB);
    }
}
