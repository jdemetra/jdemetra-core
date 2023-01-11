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
import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.univariate.IFilteringResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.UpdateInformation;
import jdplus.ssf.univariate.ISsfError;
import jdplus.ssf.ISsfLoading;

/**
 * Array form copyOf the Kalman filter
 *
 * @author Jean Palate
 */
public class ArrayFilter {

    private LState state;
    private UpdateInformation updinfo;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private boolean missing;
    private int dim;
    private FastMatrix A;

    /**
     *
     */
    public ArrayFilter() {
    }

    /**
     * Just
     *
     * @param t
     * @param data
     * @return
     */
    protected void error(int t, ISsfData data) {
        missing = data.isMissing(t);
        if (missing) {
            // pe_ = null;
            updinfo.setMissing();
        } else {
            double y = data.get(t);
            updinfo.set(y - loading.ZX(t, state.a), data.isConstraint(t));
        }
    }

    private void initialize(ISsf ssf) {
        loading = ssf.loading();
        error = ssf.measurementError();
        dynamics = ssf.dynamics();
        dim = ssf.getStateDim();
        updinfo = new UpdateInformation(dim);
        state = LState.of(ssf);
        int nres = dynamics.getInnovationsDim();
        A = FastMatrix.make(dim + 1, dim + 1 + nres);
        L().copy(state.L);
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IFilteringResults rslts) {
        loading = ssf.loading();
        error = ssf.measurementError();
        dynamics = ssf.dynamics();
        initialize(ssf);
        int t = 0;
        int end = data.length();
        while (t < end) {
            // missing or e(t)
            error(t, data);
            preArray(t);
            ElementaryTransformations.givensTriangularize(A);
            postArray();
            if (rslts != null) {
                rslts.save(t, updinfo);
            }
            nextState(t++);
        }
        return true;
    }

    private void preArray(int t) {
        loading.ZM(t, L(), ZL());
        dynamics.TM(t, L());
        U().set(0);
        dynamics.S(t, U());
        K().set(0);
        if (error != null) {
            A.set(0, 0, Math.sqrt(error.at(t)));
        } else {
            A.set(0, 0, 0);
        }
    }

    private void postArray() {
        double e = A.get(0, 0);
        updinfo.setStandardDeviation(e);
        updinfo.M().setAY(e, K());
    }

    private void nextState(int t) {
        dynamics.TX(t, state.a);
        if (!missing) {
            state.a.addAY(updinfo.get() / updinfo.getVariance(), updinfo.M());
        }
    }

    private DataBlock K() {
        return A.column(0).drop(1, 0);
    }

    private DataBlock ZL() {
        return A.row(0).range(1, 1 + dim);
    }

    private FastMatrix L() {
        return A.extract(1, dim, 1, dim);
    }

    private FastMatrix U() {
        return A.extract(1, dim, 1 + dim, A.getColumnsCount() - dim - 1);
    }
}
