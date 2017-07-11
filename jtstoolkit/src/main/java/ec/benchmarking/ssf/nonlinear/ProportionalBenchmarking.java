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
import ec.tstoolkit.ssf.extended.WeightedLogSsf;

/**
 *
 * @author Jean Palate
 * @param <S>
 */
@Development(status = Development.Status.Preliminary)
public class ProportionalBenchmarking<S extends ISsf>
	extends
	AbstractLinearizedDisaggregationSmoother<WeightedLogSsf<S>, LinearizedLogSsf<S>> {

    private double[] m_data;

    /**
     * 
     * @param y
     * @param data
     * @param conv
     * @param s
     */
    public ProportionalBenchmarking(DataBlock y, double[] data, int conv, S s)
    {
	super(y, conv, new WeightedLogSsf<>(s, data));
	m_data = data;
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
	    double s = 0;
	    for (int i = 0; i < conversion; ++i)
		s += m_data[n - i];
	    double w = m_y[n] / s;
	    for (int i = 0; i < conversion; ++i)
		e[n - i] = w;
	    if (n + conversion >= e.length)
		break;
	    n += conversion;
	}
	while (n < e.length)
	    e[n++] = 1;

	double[] tmp = new double[e.length];
	m_lssf = getNonLinearSsf().linearApproximation(new DataBlock(tmp),
		new DataBlock(m_data));

	Cumulator cumul = new Cumulator(conversion);
	cumul.transform(tmp);
	m_yc = m_y.clone();
	for (int i = 0; i < tmp.length; ++i)
	    if (Double.isFinite(m_yc[i]))
		m_yc[i] += tmp[i];
	return true;
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calcNextApproximation() {
	double[] e = new double[m_y.length];
	for (int i = 0; i < e.length; ++i)
	    e[i] = getNonLinearSsf().Z(i, m_states.block(i).drop(1, 0));
	double[] tmp = new double[m_y.length];
	m_lssf = getNonLinearSsf().linearApproximation(new DataBlock(tmp),
		new DataBlock(e));

	Cumulator cumul = new Cumulator(conversion);
	cumul.transform(tmp);
	m_yc = m_y.clone();
	for (int i = 0; i < tmp.length; ++i)
	    if (Double.isFinite(m_yc[i]))
		m_yc[i] += tmp[i];
	return true;
    }

    /**
     * 
     * @return
     */
    public DataBlock getBenchmarkedData()
    {
	if (m_lssf == null && !calc())
	    return null;
	return m_lssf.getE();
    }

}
