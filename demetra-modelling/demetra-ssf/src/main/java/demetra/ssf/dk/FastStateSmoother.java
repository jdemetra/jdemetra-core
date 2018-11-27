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
package demetra.ssf.dk;

import demetra.data.DataBlock;
import demetra.data.DataBlockStorage;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.univariate.DefaultDisturbanceSmoothingResults;
import demetra.ssf.univariate.DisturbanceSmoother;
import demetra.ssf.univariate.IDisturbanceSmoothingResults;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.ISsfMeasurement;

/**
 *
 * @author Jean Palate
 */
public class FastStateSmoother {
    
    public static interface Corrector {

        void adjust(int pos, DataBlock a, double error);
    }
    
    private ISsf ssf;
    private ISsfDynamics dynamics;
    private ISsfLoading loading;
    private ISsfError error;
    protected Corrector corrector;
    
    public DataBlockStorage process(ISsf ssf, ISsfData data) {
        initSsf(ssf);
        int dim = ssf.getStateDim();
        int n = data.length();
        DataBlockStorage storage = new DataBlockStorage(dim, n);
        DefaultDisturbanceSmoothingResults srslts = DefaultDisturbanceSmoothingResults.light(error != null);
        srslts.prepare(ssf, 0, n);
        DataBlock a = initialState(ssf, data, srslts);
        storage.save(0, a);
        int cur = 1;
        while (cur < n) {
            // next: a(t+1) = T(t) a(t) + S*r(t)
            dynamics.TX(cur - 1, a);
            if (dynamics.hasInnovations(cur - 1)) {
                DataBlock u = srslts.u(cur);
                dynamics.addSU(cur - 1, a, u);
            }
            // T
            if (corrector != null) {
                // we want to stabilize the results so that Za(t)=y(t)
                // we suppose that the error is very small, so that we can distribute it on a 
                // in  a simple way
                if (!data.isMissing(cur)) {
                    double e = data.get(cur) - loading.ZX(cur, a) - srslts.e(cur);
                    corrector.adjust(cur, a, e);
                }
            }
            storage.save(cur++, a);
        }
        return storage;
    }
    
    private void initSsf(ISsf ssf) {
        this.ssf=ssf;
        dynamics = ssf.dynamics();
        loading = ssf.loading();
        error=ssf.measurementError();
    }
    
    private DataBlock initialState(ISsf ssf, ISsfData data, IDisturbanceSmoothingResults srslts) {
        if (ssf.initialization().isDiffuse()) {
            DiffuseDisturbanceSmoother sm = new DiffuseDisturbanceSmoother();
            sm.setCalcVariances(false);
            sm.process(ssf, data, srslts);
            return sm.firstSmoothedState();
        } else {
            DisturbanceSmoother sm = new DisturbanceSmoother();
            sm.setCalcVariances(false);
            sm.process(ssf, data, srslts, 0);
            return sm.firstSmoothedState();
        }
    }

    /**
     * @return the adjust
     */
    public Corrector getCorrector() {
        return corrector;
    }

    /**
     * @param adjust the adjust to set
     */
    public void setCorrector(Corrector corrector) {
        this.corrector = corrector;
    }
    
}
