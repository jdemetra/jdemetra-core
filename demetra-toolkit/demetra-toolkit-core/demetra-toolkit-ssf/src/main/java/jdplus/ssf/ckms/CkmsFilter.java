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
package jdplus.ssf.ckms;

import jdplus.data.DataBlock;
import demetra.design.Development;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.univariate.IFilteringResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.UpdateInformation;

/**
 * Chandrasekhar recursions
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class CkmsFilter {

    public static interface IFastFilterInitializer {

        int initializeFilter(CkmsState state, UpdateInformation upd, ISsf ssf, ISsfData data);
    }

    private double eps = 1e-15;
    private double neps;

    private final IFastFilterInitializer initializer;
    private ISsfLoading loading;
    private ISsfDynamics dynamics;

    private ISsfData data;

    private CkmsState state;
    private UpdateInformation pe;
    private double[] L, M;
    private int steadypos;

    /**
     *
     */
    public CkmsFilter() {
        initializer = new CkmsInitializer();
    }

    public CkmsFilter(IFastFilterInitializer initializer) {
        this.initializer = initializer;
    }

    /**
     * Retrieves the final state vector(which is a(N|N-1))
     *
     * @return
     */
    public CkmsState getFinalState() {
        return state;
    }

    public void setEpsilon(double eps) {
        this.eps = eps;
    }

    public double getEpsilon() {
        return eps;
    }

    public int getSteadyStatePosition() {
        return steadypos;
    }

    private int initialize(ISsf ssf) {
        steadypos = -1;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
        int dim = ssf.getStateDim();
        state = new CkmsState(dim);
        pe = new UpdateInformation(dim);

        int t = initializer.initializeFilter(state, pe, ssf, data);
        if (t < 0) {
            return -1;
        }
        M = pe.M().getStorage();
        L = state.l.getStorage();
        neps = eps * pe.getVariance();
        return t;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data,
            final IFilteringResults rslts) {
        this.data = data;
        int t = initialize(ssf);
        if (t < 0) {
            return false;
        }
        int end = this.data.length();
        while (t < end) {
            error(t);
            if (rslts != null) {
                rslts.save(t, pe);
            }
            update(t);
            next(t++);
            //
        }
        return true;
    }

    private void next(int t) {
        if (steadypos < 0) {
            // M(i+1) = M(i) - L(i) * (Z*L(i))/V(i)
            // L(i+1) = T (L(i) - M(i) * (Z*L(i))/V(i))
            // F(i+1) = F(i) - (Z*L(i))^2/V(i)

            // ZLi, V(i+1)
            double zl = loading.ZX(0, state.l);

            if (Math.abs(zl) > neps) {
                // C, L
                double f = pe.getVariance();
                double zlv = zl / f;
                f -= zl * zlv;
                for (int i = 0; i < L.length; ++i) {
                    double l = L[i];
                    L[i] -= M[i] * zlv;
                    M[i] -= l * zlv;
                }
                pe.setVariance(f);
            } else if (state.l.norm2() <= eps) {
                steadypos = t;
            }
            dynamics.TX(t, state.l);
        }
        dynamics.TX(t, state.a);
    }

    private void update(int t) {
        state.a.addAY(pe.get() / pe.getVariance(), pe.M());
    }

    private void error(int t) {
        double y = data.get(t);
        pe.set(y - loading.ZX(t, state.a));
    }
}
