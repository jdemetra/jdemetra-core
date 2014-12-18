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

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LinearTrend extends AbstractSingleTsVariable {

    private Day m_start;

    /**
     * 
     * @param start
     */
    public LinearTrend(Day start)
    {
	m_start = start;
    }

    /**
     *
     * @param start
     * @param data
     */
    @Override
    public void data(TsPeriod start, DataBlock data) {
	TsPeriod s = new TsPeriod(start.getFrequency(), m_start);
	int val = start.minus(s);
	for (int i = 0; i < data.getLength(); ++i) {
            data.set(i, val + i);
        }
    }

    @Override
    public String getDescription() {
	return "Trend";
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
	return true;
    }

}
