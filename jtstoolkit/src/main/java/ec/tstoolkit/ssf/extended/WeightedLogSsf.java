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

package ec.tstoolkit.ssf.extended;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DataBlockStorage;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.ssf.ISsf;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Exploratory)
public class WeightedLogSsf<S extends ISsf> implements INonLinearSsf
{

    private S m_ssf;

    private double[] m_w;

    /**
     * 
     * @param ssf
     * @param w
     */
    public WeightedLogSsf(S ssf, double[] w)
    {
	m_ssf = ssf;
	m_w = w;
    }

    /**
     * 
     * @return
     */
    public S getSsf()
    {
	return m_ssf;
    }

    /**
     * 
     * @return
     */
    public DataBlock getWeights()
    {
	return new DataBlock(m_w);
    }

    /**
     *
     * @param observations
     * @param lstates
     * @return
     */
    public LinearizedLogSsf<S> linearApproximation(DataBlock observations,
	    DataBlock lstates) {
	double[] e = new double[observations.getLength()];
	for (int i = 0; i < e.length; ++i) {
	    double w = lstates.get(i);
	    e[i] = w;
	    observations.add(i, -w * (1 - Math.log(w / m_w[i])));
	}

	return new LinearizedLogSsf<>(m_ssf, e);
    }

    /**
     *
     * @param observations
     * @param states
     * @return
     */
    public ISsf linearApproximation(DataBlock observations,
	    DataBlockStorage states) {
	double[] e = new double[observations.getLength()];
	for (int i = 0; i < e.length; ++i) {
	    double w = m_ssf.ZX(i, states.block(i));
	    e[i] = m_w[i] * Math.exp(w);
	    observations.add(i, -e[i] * (1 - w));
	}

	return new LinearizedLogSsf<>(m_ssf, e);
    }

    /**
     * 
     * @param pos
     * @param x
     */
    public void TX(int pos, DataBlock x)
    {
	m_ssf.TX(pos, x);
    }

    /**
     * 
     * @param pos
     * @param x
     * @return
     */
    public double Z(int pos, DataBlock x)
    {
	return m_w[pos] * Math.exp(m_ssf.ZX(pos, x));
    }

}
