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
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.SymmetricMatrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.univariate.IFilteringResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.ssf.UpdateInformation;
import demetra.ssf.ISsfInitialization;

/**
 * Array form copyOf the Kalman filter
 *
 * @author Jean Palate
 */
public class ArrayFilter {

    private LState state_;
    private UpdateInformation pe_;
    private ISsf ssf;
    private ISsfMeasurement measurement;
    private ISsfDynamics dynamics;
    private ISsfData data_;
    private int pos_, end_, dim_, nres_;
    private Matrix A;

    /**
     *
     */
    public ArrayFilter() {
    }

    /**
     */
    protected void error() {

        double y = data_.get(pos_);
        pe_.set(y - measurement.ZX(pos_, state_.a));
    }

    private boolean initFilter() {
        pos_ = 0;
        end_ = data_.length();
        nres_ = dynamics.getInnovationsDim();
        dim_ = ssf.getStateDim();
        A = Matrix.make(dim_ + 1, dim_ + 1 + nres_);
        return true;
    }

    private int initState() {
        state_ = new LState(L());
        pe_ = new UpdateInformation(dim_);
        ISsfInitialization initialization = ssf.getInitialization();
        if (!initialization.a0(state_.a))
            return -1;
        Matrix P0 = Matrix.make(dim_, dim_);
        if (! initialization.Pf0(P0))
            return -1;
        SymmetricMatrix.lcholesky(P0, State.ZERO);
        state_.L.copy(P0);

        return 0;
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
        measurement = ssf.getMeasurement();
        dynamics = ssf.getDynamics();
        data_ = data;
        if (!initFilter()) {
            return false;
        }
        pos_ = initState();
        if (pos_ < 0) {
            return false;
        }
        do {
            preArray();
            ElementaryTransformations.fastGivensTriangularize(A);
            postArray();
            error();
            rslts.save(pos_, pe_);
            nextState();
        } while (++pos_ < end_);
        return true;
    }

    private void preArray() {
        measurement.ZM(pos_, L(), ZL());
        dynamics.TM(pos_, L());
        U().set(0);
        dynamics.S(pos_, U());
        K().set(0);
        if (measurement.hasError(pos_))
            A.set(0,0, Math.sqrt(measurement.errorVariance(pos_)));
        else
            A.set(0,0,0);
    }

    private void postArray() {
        double e=A.get(0,0);
        pe_.setStandardDeviation(e);
        pe_.M().copy(K());
        pe_.M().mul(e);
    }

    private void nextState() {
        dynamics.TX(pos_, state_.a);
        state_.a.addAY(pe_.get() / pe_.getVariance(), pe_.M());
     }

    private DataBlock K() {
        return A.column(0).drop(1, 0);
    }

    private DataBlock ZL() {
        return A.row(0).range(1, 1 + dim_);
    }

    private Matrix L() {
        return A.extract(1, dim_, 1, dim_);
    }

    private Matrix U() {
        return A.extract(1, dim_, 1 + dim_, A.getColumnsCount()-dim_-1);
    }
}
