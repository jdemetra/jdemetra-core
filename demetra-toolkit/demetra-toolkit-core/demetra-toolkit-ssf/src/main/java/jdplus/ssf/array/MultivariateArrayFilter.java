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
package jdplus.ssf.array;

import jdplus.data.DataBlock;
import jdplus.math.matrices.decomposition.ElementaryTransformations;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.multivariate.IMultivariateSsfData;
import jdplus.ssf.multivariate.IMultivariateFilteringResults;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.State;
import jdplus.ssf.multivariate.MultivariateUpdateInformation;

/**
 * Array form copyOf the Kalman filter
 *
 * @author Jean Palate
 */
public class MultivariateArrayFilter {

    private LState state;
    private MultivariateUpdateInformation perrors;
    private IMultivariateSsf ssf;
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
    private void error() {
        DataBlock U = perrors.getTransformedPredictionErrors();
        Matrix L = perrors.getCholeskyFactor();
        U.set(0);
        for (int i = 0; i < nm; ++i) {
            double y = data.get(pos, i);
            U.set(i, y - measurements.loading(i).ZX(pos, state.a));
        }
        LowerTriangularMatrix.solveLx(L, U, State.ZERO);
    }

    private boolean initFilter() {
        pos = 0;
        end = data.getObsCount();
        nm = measurements.getCount();
        nres = dynamics.getInnovationsDim();
        dim = ssf.getStateDim();
        A = Matrix.make(dim + nm, dim + nm + nres);
        return true;
    }

    private void initState() {
        state = new LState(L());
        perrors = new MultivariateUpdateInformation(dim, nm);
        
        ssf.initialization().a0(state.a);
        Matrix P0 = Matrix.make(dim, dim);
        ssf.initialization().Pf0(P0);
        SymmetricMatrix.lcholesky(P0, State.ZERO);
        state.L.copy(P0);
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateFilteringResults rslts) {
        this.ssf=ssf;
        measurements = ssf.measurements();
        dynamics = ssf.dynamics();
        this.data = data;
        if (!initFilter()) {
            return false;
        }
        initState();
        pos=0;
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
        measurements.errors().R(pos, R());
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
        return A.extract(nm, dim, 0, nm);
    }

    private Matrix ZL() {
        return A.extract(0, nm, nm, dim);
    }

    private Matrix L() {
        return A.extract(nm, dim, nm, dim);
    }

    private Matrix U() {
        return A.extract(nm, dim, nm + dim, A.getColumnsCount()-nm-dim);
    }
}
