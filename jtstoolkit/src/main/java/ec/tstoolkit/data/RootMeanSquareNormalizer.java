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

/// <summary>Normalization based on the root mean square of the data.</summary>
import ec.tstoolkit.data.IReadDataBlock;

import ec.tstoolkit.design.Development;

/// <remarks>The scaling factor is the inverse of the root mean squares of the data.</remarks>
/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class RootMeanSquareNormalizer implements IDataNormalizer {

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public static double hypot(double a, double b)
    {
	if (Math.abs(a) > Math.abs(b)) {
	    double r = b / a;
	    return Math.abs(a) * Math.sqrt(1 + r * r);
	} else if (b != 0) {
	    double r = a / b;
	    return Math.abs(b) * Math.sqrt(1 + r * r);
	} else
	    return 0;
    }

    private double m_c;

    private double[] m_data;

    /**
     * 
     * @return
     */
    @Override
    public double getFactor()
    {
	return m_c;
    }

    /**
     * 
     * @return
     */
    @Override
    public double[] getNormalizedData()
    {
	return m_data;
    }

    private boolean process() {
	double s = 0;
	double n = 0;
	for (int i = 0; i < m_data.length; ++i) {
	    double d = m_data[i];
	    if (Double.isFinite(d)) {
		s = hypot(s, d);
		n += 1;
	    }
	}
	if (s == 0)
	    return false;
	m_c = Math.sqrt(n) / s;

	for (int i = 0; i < m_data.length; ++i)
	    if (Double.isFinite(m_data[i]))
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
    @Override
    public boolean process(IReadDataBlock data)
    {
	// if (data == null)
	// throw new ArgumentNullException("data");
	m_c = 1;
	m_data = new double[data.getLength()];
	data.copyTo(m_data, 0);
	return process();
    }
}
