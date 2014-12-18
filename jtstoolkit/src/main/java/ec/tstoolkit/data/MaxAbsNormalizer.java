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

/// <summary>Normalization based on the maximal absolute value.</summary>

import ec.tstoolkit.design.Development;

/**
 * Tool for scaling a set of data by the inverse of the maximum of the
 * absolute values.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MaxAbsNormalizer implements IDataNormalizer {

    private double m_c, m_max = 10;

    private double[] m_data;

    /**
     * Gets the scaling factor
     * @return The scaling factor, which is the inverse of the maximum of the
     * absolute value of the data.
     */
    public double getFactor()
    {
	return m_c;
    }

    /**
     * 
     * @return
     */
    public double getMax()
    {
	return m_max;
    }

    /**
     * 
     * @return
     */
    public double[] getNormalizedData()
    {
	return m_data;
    }

    private boolean process() {
	// if (data == null)
	// throw new ArgumentNullException("data");
	double max = 0;
	for (int i = 0; i < m_data.length; ++i) {
	    double d = m_data[i];
	    if (DescriptiveStatistics.isFinite(d)) {
		d = Math.abs(d);
		if (d > max)
		    max = d;
	    }
	}
	if (max == 0)
	    return false;
	m_c = m_max / max;

	for (int i = 0; i < m_data.length; ++i)
	    if (DescriptiveStatistics.isFinite(m_data[i]))
		m_data[i] *= m_c;
	return true;

    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean process(double[] data)
    {
	m_c = 1;
	m_data = data.clone();
	return process();
    }

    /**
     * 
     * @param data
     * @return
     */
    public boolean process(IReadDataBlock data)
    {
	// if (data == null)
	// throw new ArgumentNullException("data");
	m_c = 1;
	m_data = new double[data.getLength()];
	data.copyTo(m_data, 0);
	return process();
    }

    /**
     * 
     * @param value
     */
    public void setMax(double value)
    {
	m_max = value;
    }
}
