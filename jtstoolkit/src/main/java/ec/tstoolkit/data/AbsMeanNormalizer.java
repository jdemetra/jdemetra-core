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

package ec.tstoolkit.data;

/// <summary></summary>

import ec.tstoolkit.design.Development;

/// <remarks>
/// 
/// data.
/// </remarks>
/**
 * Normalization based on the mean of the absolute values. 
 * The scaling factor is the inverse of the mean of the absolute values of the data.
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class AbsMeanNormalizer implements IDataNormalizer {

    private double m_c;

    private double[] m_data;

    @Override
    public double getFactor() {
	return m_c;
    }

    @Override
    public double[] getNormalizedData() {
	return m_data;
    }

    private boolean process() {
	double s = 0;
	int n = 0;
	for (int i = 0; i < m_data.length; ++i) {
	    double d = m_data[i];
	    if (DescriptiveStatistics.isFinite(d)) {
		s += Math.abs(d);
		++n;
	    }
	}
	if (s == 0)
	    return false;
	m_c = n / s;

	for (int i = 0; i < m_data.length; ++i)
	    if (DescriptiveStatistics.isFinite(m_data[i]))
		m_data[i] *= m_c;
	return true;

    }

    @Override
    public boolean process(IReadDataBlock data) {
	m_c = 1;
	m_data = new double[data.getLength()];
	data.copyTo(m_data, 0);
	return process();
    }
}
