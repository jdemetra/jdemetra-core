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
package ec.tstoolkit.ssf;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SsfRefData implements ISsfData, Cloneable {

    private final IReadDataBlock m_data, m_a0;

    /**
     * Creates a new instance of SsfData
     *
     * @param data
     * @param a0
     */
    public SsfRefData(final IReadDataBlock data, final IReadDataBlock a0) {
        m_data = data;
        m_a0 = a0;
    }

    @Override
    public Object clone() {
        return new SsfRefData(m_data, m_a0);
    }

    /**
     *
     * @param n
     * @return
     */
    @Override
    public double get(final int n) {
        return n >= m_data.getLength() ? Double.NaN : m_data.get(n);
    }

    /**
     *
     * @return
     */
    @Override
    public int getCount() {
        return m_data.getLength();
    }

    /**
     *
     * @return
     */
    @Override
    public double[] getInitialState() {

        if (m_a0 == null) {
            return null;
        }
        double[] a0 = new double[m_a0.getLength()];
        m_a0.copyTo(a0, 0);
        return a0;

    }

    /**
     *
     * @return
     */
    @Override
    public int getObsCount() {
        int n = 0;
        for (int i = 0; i < m_data.getLength(); ++i) {
            if (!Double.isNaN(m_data.get(i))) {
                ++n;
            }
        }
        return n;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasData() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasMissingValues() {
        for (int i = 0; i < m_data.getLength(); ++i) {
            if (Double.isNaN(m_data.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param pos
     * @return
     */
    @Override
    public boolean isMissing(final int pos) {
        return pos >= m_data.getLength() || Double.isNaN(m_data.get(pos));
    }
}
