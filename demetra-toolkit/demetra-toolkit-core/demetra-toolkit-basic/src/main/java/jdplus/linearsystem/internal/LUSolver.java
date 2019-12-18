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
import demetra.math.Constants;
import jdplus.data.normalizer.SafeNormalizer;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.math.matrices.decomposition.LUDecomposition;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.decomposition.Gauss;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
@Development(status = Development.Status.Release)
public class LUSolver implements LinearSystemSolver {

    @BuilderPattern(LUSolver.class)
    public static class Builder {

        private LUDecomposition.Decomposer decomposer=(M, e)->Gauss.decompose(M, e);
        private double eps=Constants.getEpsilon();
        private boolean normalize=false;

        private Builder() {
        }

        public Builder decomposer(LUDecomposition.Decomposer decomposer) {
            this.decomposer=decomposer;
            return this;
        }
        
        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }
        
        public Builder precision(double eps){
            this.eps=eps;
            return this;
        }

        public LUSolver build() {
            return new LUSolver(decomposer, eps, normalize);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

        private final LUDecomposition.Decomposer decomposer;
        private double eps=Constants.getEpsilon();
        private boolean normalize;

    private LUSolver(LUDecomposition.Decomposer decomposer, double eps, boolean normalize) {
        this.decomposer=decomposer;
        this.eps=eps;
        this.normalize = normalize;
    }

    @Override
    public void solve(Matrix A, DataBlock b) {
        // we normalize b
        Matrix An;
        double[] factor=null;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            SafeNormalizer sn=new SafeNormalizer();
            factor=new double[A.getRowsCount()];
            int i=0;
            while (rows.hasNext()) {
                factor[i++]=sn.normalize(rows.next());
            }
        } else {
            An = A;
        }
        LUDecomposition lu = decomposer.decompose(An, eps);
        lu.solve(b);
        if (factor != null){
            b.div(DataBlock.of(factor));
        }
    }

    @Override
    public void solve(Matrix A, Matrix B) {
        Matrix An;
        double[] factor=null;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            SafeNormalizer sn=new SafeNormalizer();
            factor=new double[A.getRowsCount()];
            int i=0;
            while (rows.hasNext()) {
                factor[i++]=sn.normalize(rows.next());
            }
        } else {
            An = A;
        }
        LUDecomposition lu = decomposer.decompose(An, eps);
        lu.solve(B);
        if (factor != null){
            DataBlockIterator rows = B.rowsIterator();
            int r=0;
            while (rows.hasNext())
                rows.next().div(factor[r++]);
        }
    }
}
