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
package ec.tstoolkit.ssf.multivariate;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class MultivariateSsfData implements IMSsfData
{

    private final Matrix m_data;

    private double[] m_a0;

    /** Creates a new instance of SSFData
     * @param data
     * @param a0
     */
    public MultivariateSsfData(final Matrix data, final double[] a0) {
	m_data = data;
	if (a0 != null)
	    m_a0 = a0.clone();
    }

    /**
     * 
     * @param v
     * @return
     */
    @Override
    public int count(final int v)
    {
	return m_data.getColumnsCount();
    }

    /**
     * 
     * @param v
     * @param n
     * @return
     */
    @Override
    public double get(final int v, final int n)
    {
	return n >= m_data.getColumnsCount() ? Double.NaN : m_data.get(v, n);
    }

    /**
     * 
     * @return
     */
    @Override
    public double[] getInitialState()
    {
	return m_a0;
    }

    /**
     * 
     * @return
     */
    @Override
    public int getVarsCount()
    {
	return m_data.getRowsCount();
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean hasData()
    {
	return true;
    }

    /**
     * 
     * @return
     */
    public boolean hasMissingValues()
    {
	double[] a = m_data.internalStorage();
	for (int i = 0; i < a.length; ++i)
	    if (Double.isNaN(a[i]))
		return true;
	return false;
    }

    /**
     * 
     * @param v
     * @param pos
     * @return
     */
    @Override
    public boolean isMissing(final int v, final int pos)
    {
	return pos >= m_data.getColumnsCount()
		|| Double.isNaN(m_data.get(v, pos));
    }

    /**
     * 
     * @param v
     * @return
     */
    @Override
    public int obsCount(final int v)
    {
	int n = 0;
	for (int i = 0; i < m_data.getColumnsCount(); ++i)
	    if (!Double.isNaN(m_data.get(v, i)))
		++n;
	return n;
    }

    /**
     * 
     * @return
     */
    public M2uData UConvert()
    {
	return new M2uData(m_data, m_a0);
    }
}
