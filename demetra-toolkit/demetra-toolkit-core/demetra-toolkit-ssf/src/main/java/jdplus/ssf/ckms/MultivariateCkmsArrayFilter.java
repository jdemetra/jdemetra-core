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
package jdplus.ssf.ckms;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.LowerTriangularMatrix;
import jdplus.maths.matrices.decomposition.GivensRotation;
import jdplus.maths.matrices.decomposition.HyperbolicRotation;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.State;
import jdplus.ssf.array.LState;
import jdplus.ssf.multivariate.IMultivariateFilteringResults;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.IMultivariateSsfData;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.multivariate.MultivariateUpdateInformation;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class MultivariateCkmsArrayFilter {

    public static interface IFastFilterInitializer {

        int initializeFilter(LState state, MultivariateUpdateInformation upd, IMultivariateSsf ssf, IMultivariateSsfData data);
    }
    /**
     * M = |R Z| |K L|
     */
    static class UMatrix {

        final CanonicalMatrix M;
        final FastMatrix R, Z;
        final FastMatrix K, L;

        UMatrix(int stateDim, int varDim, int lDim) {
            M = CanonicalMatrix.make(varDim + stateDim, varDim + lDim);
            R = M.extract(0, varDim, 0, varDim);
            Z = M.extract(0, varDim, varDim, lDim);
            K = M.extract(varDim, stateDim, 0, varDim);
            L = M.extract(varDim, stateDim, varDim, lDim);
        }

        void triangularize() {
            int pos = R.getColumnsCount(), m = M.getColumnsCount();
            DataBlockIterator rows = M.rowsIterator();
            DataBlockIterator nrows = M.rowsIterator();
            for (int i = 0; i < pos; ++i) {
                DataBlock next = rows.next();
                int j = i + 1;
                while (j < pos) {
                    GivensRotation gr = GivensRotation.of(next, i, j++);
                    if (gr != null) {
                        nrows.reset(i + 1);
                        while (nrows.hasNext()) {
                            gr.transform(nrows.next());
                        }
                    }
                }
                while (j < m) {
                    HyperbolicRotation hr = HyperbolicRotation.of(next, i, j++);
                    if (hr != null) {
                        nrows.reset(i + 1);
                        while (nrows.hasNext()) {
                            hr.transform(nrows.next());
                        }
                    }
                }

            }
        }
    }
    
    private LState state;
    private MultivariateUpdateInformation perrors;
    private ISsfMeasurements measurements;
    private ISsfDynamics dynamics;
    private IMultivariateSsfData data;
    private int nm, dim;
    private UMatrix A;
    private final IFastFilterInitializer initializer;

    /**
     */
    public MultivariateCkmsArrayFilter() {
        initializer = new MultivariateCkmsInitializer();
    }

    public MultivariateCkmsArrayFilter(IFastFilterInitializer initializer) {
        this.initializer = initializer;
    }
    
    
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateFilteringResults rslts) {
        this.data = data;
        int t=initialize(ssf);
        if (t<0)
            return false;
        rslts.open(ssf, this.data);
        int end = data.getObsCount();
        while (t<end) {
            preArray(t);
            A.triangularize();
            postArray(t);
            error(t);
            rslts.save(t, perrors);
            next(t++);
        };
        rslts.close();
        return true;
    }

    private void preArray(int t) {
        measurements.ZM(t, A.L, A.Z);
        dynamics.TM(t, A.L);
        A.R.set(0);
        measurements.errors().R(t, A.R);
        A.K.set(0);
    }

    private void postArray(int t) {
        perrors.getCholeskyFactor().copy(A.R);
        perrors.getK().copy(A.K);
    }

    private void next(int t) {
        dynamics.TX(t, state.a);
        for (int i = 0; i < nm; ++i) {
            state.a.addAY(perrors.getTransformedPredictionErrors().get(i), perrors.getK().column(i));
        }

    }
    
    private void error(int t) {
        DataBlock U = perrors.getTransformedPredictionErrors();
        CanonicalMatrix L = perrors.getCholeskyFactor();
        U.set(0);
        for (int i = 0; i < nm; ++i) {
            double y = data.get(t, i);
            U.set(i, y - measurements.loading(i).ZX(t, state.a));
        }
        LowerTriangularMatrix.rsolve(L, U, State.ZERO);
    }

    private int initialize(IMultivariateSsf ssf) {
        measurements = ssf.measurements();
        dynamics = ssf.dynamics();
        nm = measurements.getCount();
        dim = ssf.getStateDim();
        A = new UMatrix(dim, nm, nm);
        state = new LState(A.L);
        perrors = new MultivariateUpdateInformation(dim, nm);
        return initializer.initializeFilter(state, perrors, ssf, data);
    }

}
