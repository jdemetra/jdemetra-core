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

import ec.benchmarking.Cumulator;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.ISsf;
import ec.tstoolkit.ssf.extended.LinearizedLogSsf;
import ec.tstoolkit.ssf.extended.LogSsf;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Preliminary)
public class LinearizedLogSmoother<S extends ISsf>
        extends AbstractLinearizedDisaggregationSmoother<LogSsf<S>, LinearizedLogSsf<S>> {

    /**
     * 
     * @param y
     * @param conv
     * @param s
     */
    public LinearizedLogSmoother(DataBlock y, int conv, S s) {
        super(y, conv, new LogSsf<>(s));
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calcInitialApproximation() {
        double[] e = new double[m_y.length];
        int n = conversion - 1;
        while (n < e.length) {
            double w = m_y[n] / conversion;
            for (int i = 0; i < conversion; ++i) {
                e[n - i] = w;
            }
            n += conversion;
        }

        double[] tmp = new double[e.length];
        m_lssf = getNonLinearSsf().linearApproximation(new DataBlock(tmp),
                new DataBlock(e));

        Cumulator cumul = new Cumulator(conversion);
        cumul.transform(tmp);
        m_yc = m_y.clone();
        for (int i = 0; i < tmp.length; ++i) {
            if (Double.isFinite(m_yc[i])) {
                m_yc[i] += tmp[i];
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calcNextApproximation() {
        double[] e = new double[m_y.length];
        for (int i = 0; i < e.length; ++i) {
            e[i] = getNonLinearSsf().Z(i, m_states.block(i).drop(1, 0));
        }
        double[] tmp = new double[m_y.length];
        m_lssf = getNonLinearSsf().linearApproximation(new DataBlock(tmp),
                new DataBlock(e));

        Cumulator cumul = new Cumulator(conversion);
        cumul.transform(tmp);
        m_yc = m_y.clone();
        for (int i = 0; i < tmp.length; ++i) {
            if (Double.isFinite(m_yc[i])) {
                m_yc[i] += tmp[i];
            }
        }
        return true;
    }

    /**
     * 
     * @return
     */
    public DataBlock getBenchmarkedData() {
        if (m_lssf == null && !calc()) {
            return null;
        }
        return m_lssf.getE();
    }
}
