/*
 * Copyright 2016 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
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
import jdplus.ssf.SsfException;
import jdplus.ssf.State;
import jdplus.ssf.dk.sqrt.DiffuseSquareRootInitializer;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.OrdinaryFilter;
import jdplus.ssf.UpdateInformation;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.univariate.ISsfError;
import jdplus.ssf.ISsfLoading;
import jdplus.maths.matrices.FastMatrix;

/**
 * Automatic initialization of diffuse time invariant models. The algorithm
 * computes the state at the end of the diffuse initialization and applies then
 * the same decomposition as in the stationary case: P(ndiffuse)-P(ndiffuse-1) =
 * - 1/f* L*L' The theoritical fundation of this approach should still be
 * developed.
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@Development(status = Development.Status.Alpha)
public class CkmsDiffuseInitializer implements CkmsFilter.IFastFilterInitializer {

    private final OrdinaryFilter.Initializer initializer;

    public CkmsDiffuseInitializer() {
        initializer = null;
    }

    /**
     *
     * @param initializer
     */
    public CkmsDiffuseInitializer(OrdinaryFilter.Initializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public int initializeFilter(CkmsState fstate, final UpdateInformation upd, ISsf ssf, ISsfData data) {
        ISsfInitialization initialization = ssf.initialization();
        int dim = initialization.getStateDim();
        State state = new State(dim);
        int t = 0;
        if (initializer != null) {
            t = initializer.initializeFilter(state, ssf, data);
            if (t < 0) {
                return -1;
            }
        } else {
            t = new DiffuseSquareRootInitializer().initializeFilter(state, ssf, data);
            if (t < 0) {
                return -1;
            }
        }

        fstate.a().copy(state.a());
        ISsfDynamics dynamics = ssf.dynamics();
        ISsfLoading loading = ssf.loading();
        ISsfError error = ssf.measurementError();
        FastMatrix P = state.P();
        DataBlock k = upd.M();
        double f = loading.ZVZ(0, P);
        if (error != null) {
            f += error.at(0);
        }
        upd.setVariance(f);
        // K0 = TPZ' / var
        loading.ZM(0, P, k);
        DataBlock l = fstate.l();
        l.copy(k);
        dynamics.TX(0, l);

        // L0: computes next iteration. TVT'-KK'*var + Q -V = - L(var)^-1 L'
        FastMatrix TVT = P.deepClone();
        dynamics.TVT(0, TVT);
        dynamics.addV(0, TVT);
        TVT.sub(P);
        TVT.addXaXt(-1 / f, l);
        TVT.mul(-f);
        int imax = 0;
        double lmax = TVT.get(0, 0);
        for (int i = 1; i < dim; ++i) {
            double lcur = TVT.get(i, i);
            if (lcur > lmax) {
                imax = i;
                lmax = lcur;
            }
        }
        if (lmax > 0) {
            l.copy(TVT.column(imax));
            l.mul(Math.sqrt(1 / lmax));
        } else if (!TVT.isZero(1e-6)) {
            throw new SsfException(SsfException.FASTFILTER);
        } else {
            l.set(0);
        }
        return t;
    }

}
