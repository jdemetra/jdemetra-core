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
package jdplus.ssf.dk;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.univariate.DefaultDisturbanceSmoothingResults;
import jdplus.ssf.univariate.DisturbanceSmoother;
import jdplus.ssf.univariate.IDisturbanceSmoothingResults;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.univariate.ISsfData;
import jdplus.ssf.univariate.ISsfError;

/**
 * Fast smoother based on the disturbance smoother
 * @author Jean Palate
 */
public class FastStateSmoother {

    private final ISsf ssf;
    private final ISsfDynamics dynamics;
    private final ISsfError error;

    public FastStateSmoother(final ISsf ssf) {
        this.ssf = ssf;
        dynamics = ssf.dynamics();
        error = ssf.measurementError();
    }

    public DataBlockStorage process(ISsfData data) {
        int dim = ssf.getStateDim();
        int n = data.length();
        DataBlockStorage storage = new DataBlockStorage(dim, n);
        DefaultDisturbanceSmoothingResults srslts = DefaultDisturbanceSmoothingResults.light(error != null);
        srslts.prepare(ssf, 0, n);
        DataBlock a = initialState(data, srslts);
        storage.save(0, a);
        int cur = 1;
        while (cur < n) {
            // next: a(t+1) = T(t) a(t) + S*r(t)
            dynamics.TX(cur - 1, a);
            if (dynamics.hasInnovations(cur - 1)) {
                DataBlock u = srslts.u(cur);
                dynamics.addSU(cur - 1, a, u);
            }
            storage.save(cur++, a);
        }
        return storage;
    }

    private DataBlock initialState(ISsfData data, IDisturbanceSmoothingResults srslts) {
        if (ssf.initialization().isDiffuse()) {
            DiffuseDisturbanceSmoother sm = DiffuseDisturbanceSmoother
                    .builder(ssf)
                    .calcVariance(false)
                    .rescaleVariance(false)
                    .build();
            sm.process(data, srslts);
            return sm.firstSmoothedState();
        } else {
            DisturbanceSmoother sm = DisturbanceSmoother
                    .builder(ssf)
                    .calcVariance(false)
                    .rescaleVariance(false)
                    .build();
            sm.process(data, srslts, 0);
            return sm.firstSmoothedState();
        }
    }

}
