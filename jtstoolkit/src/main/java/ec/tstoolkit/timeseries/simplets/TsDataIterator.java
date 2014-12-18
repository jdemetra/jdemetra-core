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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsDataIterator implements java.util.Enumeration<TsObservation> {

    private TsDataBlock m_block;
    private int m_cur = 0;
    private boolean m_skip;

    /**
     * 
     * @param TSData
     */
    public TsDataIterator(TsData TSData) {
	m_block = TsDataBlock.all(TSData);
	m_skip = true;
    }

    /**
     * 
     * @param block
     */
    public TsDataIterator(TsDataBlock block) {
	m_block = block;
	m_skip = false;
    }

    @Override
    public boolean hasMoreElements() {
	if (m_skip) {
	    for (; m_cur < m_block.data.getLength(); ++m_cur)
		if (DescriptiveStatistics.isFinite(m_block.data.get(m_cur)))
		    return true;
	    return false;
	} else
	    return m_cur < m_block.data.getLength();
    }

    /**
     * 
     * @return
     */
    public boolean isSkippingMissings()
    {
	return m_skip;
    }

    @Override
    public TsObservation nextElement() {
	// hasMoreElements was called before that call...
	double v = m_block.data.get(m_cur);
	TsObservation obs = new TsObservation(m_block.start.plus(m_cur
		* m_block.data.getIncrement()), v);
	++m_cur;
	return obs;
    }

    /**
     *
     */
    public void reset() {
	m_cur = 0;
    }

    /**
     * 
     * @param val
     */
    public void setSkippingMissings(boolean val) {
	m_skip = val;
    }
}
