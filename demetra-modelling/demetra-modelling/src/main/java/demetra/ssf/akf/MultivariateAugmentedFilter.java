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
package demetra.ssf.akf;

import demetra.data.DataBlock;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSequence;
import demetra.data.Doubles;
import demetra.maths.matrices.Matrix;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.StateInfo;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.multivariate.IMultivariateSsfData;
import demetra.ssf.multivariate.ISsfMeasurements;

/**
 *
 * @author Jean Palate
 */
public class MultivariateAugmentedFilter {

    private AugmentedState state;
    private MultivariateAugmentedUpdateInformation updinfo;
    private IMultivariateSsf ssf;
    private ISsfMeasurements measurements;
    private ISsfDynamics dynamics;
    private IMultivariateSsfData data;
    private final boolean collapsing;
    private int collapsingPos = -1;

    /**
     *
     */
    public MultivariateAugmentedFilter() {
        collapsing = false;
    }

    public MultivariateAugmentedFilter(final boolean collapsing) {
        this.collapsing = collapsing;
    }


    /**
     * Computes a(t+1|t), P(t+1|t) from a(t|t), P(t|t) a(t+1|t) = T(t)a(t|t)
     * P(t+1|t) = T(t)P(t|t)T'(t) The same transformation is applied on state.B
     * (diffuse constraints)
     *
     * @param pos
     */
    protected void pred(int pos) {
        DataBlock a = state.a();
        dynamics.TX(pos, a);
        dynamics.TM(pos, state.B());
        dynamics.TVT(pos, state.P());
        dynamics.addV(pos, state.P());
    }

    /**
     * Computes: e(t)=y(t) - Z(t)a(t|t-1)) F(t)=Z(t)P(t|t-1)Z'(t)+H(t) F(t) =
     * L(t)L'(t) E(t) = e(t)L'(t)^-1 K(t)= P(t|t-1)Z'(t)L'(t)^-1
     *
     * Not computed for missing values
     */
    private void error(int pos) {
        int dim = ssf.getStateDim();
        DoubleSequence x=data.get(pos);
        int nmissing = x.count(y -> Double.isInfinite(y));
        int nobs = x.length() - nmissing;
        if (nobs == 0)
            updinfo=null;
        int[] obs;
        if (nmissing != 0) {
            obs = new int[nobs];
            Doubles.search(x, y -> Double.isFinite(y), obs);
        } else {
            obs = null;
        }
        updinfo = new MultivariateAugmentedUpdateInformation(dim, nobs, state.getDiffuseDim());
        updinfo.compute(ssf, pos, state, x, obs);
    }

    /**
     * Updates the state vector and its covariance a(t|t) = a(t|t-1) + e(t)
     */
    protected void update() {
        if (updinfo == null) {
            return;
        }
        int n = updinfo.getK().getColumnsCount();
        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // A = a + (M)* F^-1 * v
        Matrix P = state.P();
        DataBlock U = updinfo.getTransformedPredictionErrors();
        Matrix K = updinfo.getK();
        for (int i = 0; i < n; ++i) {
            state.a().addAY(U.get(i), K.column(i));
            P.addXaXt(-1, K.column(i));//, state_.K.column(i));
        }
        DataBlockIterator acols = state.B().columnsIterator();
        int apos=0;
        while (acols.hasNext()) {
            DataBlock row = updinfo.E().row(apos++);
            for (int i = 0; i < n; ++i) {
                acols.next().addAY(row.get(i), K.column(i));
            }
        }
    }

    /**
     *
     * @return
     */
    public AugmentedState getState() {
        return state;
    }

    public int getCollapsingPosition() {
        return collapsingPos;
    }

    private boolean initState() {
        state = AugmentedState.of(ssf);
        return (state != null);
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final IMultivariateSsf ssf, final IMultivariateSsfData data, final IMultivariateAugmentedFilteringResults rslts) {
        measurements = ssf.measurements();
        dynamics = ssf.dynamics();
        this.data = data;
        if (!initState()) {
            return false;
        }
        rslts.open(ssf, data);
        int t = 0, end = data.getObsCount();

        while (t < end) {
            if (collapse(rslts)) {
                collapsingPos = t;
                break;
            }
            rslts.save(t, state, StateInfo.Forecast);
            error(t);
            if (updinfo != null) {
                rslts.save(t, updinfo);
                update();
            }
            rslts.save(t, state, StateInfo.Concurrent);
            pred(t++);
        }
        rslts.close();
        return true;
    }

    protected boolean collapse(IMultivariateAugmentedFilteringResults decomp) {
        if (!collapsing) {
            return false;
        }
        // update the state vector
        if (!decomp.collapse(state)) {
            return false;
        }
        return true;
    }

}
