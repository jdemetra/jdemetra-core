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
package demetra.ssf.array;

import demetra.data.DataBlock;
import demetra.maths.matrices.ElementaryTransformations;
import demetra.maths.matrices.LowerTriangularMatrix;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.ISsfMeasurements;
import demetra.ssf.multivariate.IMultivariateSsfData;
import demetra.ssf.multivariate.IMultivariateFilteringResults;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.multivariate.MultivariateUpdateInformation;

/**
 * Array form copyOf the Kalman filter
 *
 * @author Jean Palate
 */
public class MultivariateArrayFilter {

    private LState state;
    private MultivariateUpdateInformation perrors;
    private ISsfMeasurements measurements;
    private ISsfDynamics dynamics;
    private IMultivariateSsfData data;
    private int pos, end, nm, dim, nres;
    private Matrix A;

    /**
     *
     */
    public MultivariateArrayFilter() {
    }

    /**
     */
    protected void error() {
        DataBlock U=perrors.getTransformedPredictionErrors();
        Matrix L=perrors.getCholeskyFactor();
        U.set(0);
        for (int i = 0; i < nm; ++i) {
            double y = data.get(pos, i);
            U.set(i, y - measurements.ZX(pos, i, state.a));
        }
        LowerTriangularMatrix.rsolve(L, U, State.ZERO);
    }

    private boolean initFilter() {
        pos = 0;
        end = data.getCount();
        nm = measurements.getMaxCount();
        nres = dynamics.getInnovationsDim();
        dim = dynamics.getStateDim();
        A = Matrix.make(dim + nm, dim + nm + nres);
        return true;
    }

    private int initState() {
        state = new LState(L());
        perrors = new MultivariateUpdateInformation(dim, nm);
        if (!dynamics.a0(state.a)) {
            return -1;
        }
        Matrix P0 = Matrix.make(dim, dim);
        if (!dynamics.Pf0(P0)) {
            return -1;
        }
        SymmetricMatrix.lcholesky(P0, State.ZERO);
        state.L.copy(P0);

        return 0;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateFilteringResults rslts) {
        measurements = ssf.getMeasurements();
        if (!measurements.isHomogeneous()) {
            return false;
        }
        dynamics = ssf.getDynamics();
        this.data = data;
        if (!initFilter()) {
            return false;
        }
        pos = initState();
        if (pos < 0) {
            return false;
        }
        rslts.open(ssf, this.data);
        do {
            preArray();
            ElementaryTransformations.fastGivensTriangularize(A);
            postArray();
            error();
            rslts.save(pos, perrors);
            nextState();
        } while (++pos < end);
        rslts.close();
        return true;
    }

    private void preArray() {
        measurements.ZM(pos, L(), ZL());
        dynamics.TM(pos, L());
        R().set(0);
        measurements.R(pos, R());
        U().set(0);
        dynamics.S(pos, U());
        K().set(0);
    }

    private void postArray() {
        perrors.getCholeskyFactor().copy(R());
        perrors.getK().copy(K());
    }

    private void nextState() {
        dynamics.TX(pos, state.a);
        for (int i = 0; i < nm; ++i) {
            state.a.addAY(perrors.getTransformedPredictionErrors().get(i), perrors.getK().column(i));
        }

    }

    private Matrix R() {
        return A.extract(0, nm, 0, nm);
    }

    private Matrix K() {
        return A.extract(nm, nm + dim, 0, nm);
    }

    private Matrix ZL() {
        return A.extract(0, nm, nm, nm + dim);
    }

    private Matrix L() {
        return A.extract(nm, nm + dim, nm, nm + dim);
    }

    private Matrix U() {
        return A.extract(nm, nm + dim, nm + dim, -1);
    }
}
