/*
* Copyright 2013 National Bank of Belgium
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

package ec.benchmarking.ssf.nonlinear;

import ec.benchmarking.ssf.SsfDisaggregation;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.DisturbanceSmoother;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.Smoother;
import ec.tstoolkit.ssf.SmoothingResults;
import ec.tstoolkit.ssf.SsfRefData;
import ec.tstoolkit.ssf.extended.INonLinearSsf;

/**
 * 
 * @param <S>
 * @param <T>
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public abstract class AbstractLinearizedDisaggregationSmoother<S extends INonLinearSsf, T extends ISsf>
        extends AbstractLinearizedDisaggregationAlgorithm<S, T> {

    private boolean dsmoother_ = true;

    /**
     *
     * @param y
     * @param conv
     * @param nlssf
     */
    protected AbstractLinearizedDisaggregationSmoother(DataBlock y, int conv,
            S nlssf) {
        super(y, conv, nlssf);
    }

    /**
     *
     * @return
     */
    @Override
    protected DataBlockStorage iterate() {
        SsfDisaggregation<T> disagg = new SsfDisaggregation<>(conversion, m_lssf);
        SsfRefData data = new SsfRefData(this.getModifiedObservations(), null);
        if (dsmoother_) {
            // calc new storage by disturbance smoother
            DisturbanceSmoother dsmoother = new DisturbanceSmoother();

            dsmoother.setSsf(disagg);
            if (!dsmoother.process(data)) {
                return null;
            }
            SmoothingResults srslts = dsmoother.calcSmoothedStates();
            return srslts.getSmoothedStates();
        } else {
            Smoother smoother = new Smoother();
            smoother.setSsf(disagg);
            SmoothingResults srslts = new SmoothingResults(true, false);
            smoother.process(data, srslts);
            return srslts.getSmoothedStates();
        }
    }
}
