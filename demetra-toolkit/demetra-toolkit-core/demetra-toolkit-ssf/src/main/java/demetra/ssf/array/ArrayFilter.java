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
import demetra.maths.matrices.decomposition.ElementaryTransformations;
import demetra.maths.matrices.FastMatrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.univariate.IFilteringResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.UpdateInformation;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.ISsfLoading;

/**
 * Array form copyOf the Kalman filter
 *
 * @author Jean Palate
 */
public class ArrayFilter {

    private LState state_;
    private UpdateInformation predictionError;
    private ISsf ssf;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private ISsfData data_;
    private int curPos, end, dim_, nres_;
    private FastMatrix A;

    /**
     *
     */
    public ArrayFilter() {
    }

    /**
     */
    protected void error() {

        double y = data_.get(curPos);
        predictionError.set(y - loading.ZX(curPos, state_.a));
    }

    private boolean initFilter() {
        curPos = 0;
        end = data_.length();
        nres_ = dynamics.getInnovationsDim();
        dim_ = ssf.getStateDim();
        A = FastMatrix.make(dim_ + 1, dim_ + 1 + nres_);
        return true;
    }

    private void initState() {
        state_ = new LState(L());
        predictionError = new UpdateInformation(dim_);
        ISsfInitialization initialization = ssf.initialization();
        initialization.a0(state_.a);
        FastMatrix P0 = FastMatrix.make(dim_, dim_);
        initialization.Pf0(P0);
        SymmetricMatrix.lcholesky(P0, State.ZERO);
        state_.L.copy(P0);
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IFilteringResults rslts) {
        this.ssf=ssf;
        loading = ssf.loading();
        error=ssf.measurementError();
        dynamics = ssf.dynamics();
        data_ = data;
        if (!initFilter()) {
            return false;
        }
        initState();
        curPos = 0;
        do {
            preArray();
            ElementaryTransformations.fastGivensTriangularize(A);
            postArray();
            error();
            rslts.save(curPos, predictionError);
            nextState();
        } while (++curPos < end);
        return true;
    }

    private void preArray() {
        loading.ZM(curPos, L(), ZL());
        dynamics.TM(curPos, L());
        U().set(0);
        dynamics.S(curPos, U());
        K().set(0);
        if (error != null)
            A.set(0,0, Math.sqrt(error.at(curPos)));
        else
            A.set(0,0,0);
    }

    private void postArray() {
        double e=A.get(0,0);
        predictionError.setStandardDeviation(e);
        predictionError.M().copy(K());
        predictionError.M().mul(e);
    }

    private void nextState() {
        dynamics.TX(curPos, state_.a);
        state_.a.addAY(predictionError.get() / predictionError.getVariance(), predictionError.M());
     }

    private DataBlock K() {
        return A.column(0).drop(1, 0);
    }

    private DataBlock ZL() {
        return A.row(0).range(1, 1 + dim_);
    }

    private FastMatrix L() {
        return A.extract(1, dim_, 1, dim_);
    }

    private FastMatrix U() {
        return A.extract(1, dim_, 1 + dim_, A.getColumnsCount()-dim_-1);
    }
}
