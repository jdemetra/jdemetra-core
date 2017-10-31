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


package demetra.x11;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.maths.linearfilters.SymmetricFilter;
import demetra.timeseries.simplets.PeriodIterator;
import demetra.timeseries.simplets.TsData;
import demetra.timeseries.simplets.TsDomain;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class StableSeasonalFilteringStrategy implements IFiltering {

    /**
     *
     * @return
     */
    @Override
    public SymmetricFilter getCentralFilter() {
	return null;
    }

    /**
     *
     * @param s
     * @param domain
     * @return
     */
    @Override
    public TsData process(final TsData s, final TsDomain domain) {
	TsDomain rdomain = domain == null ? s.getDomain() : domain;
	TsData out = new TsData(rdomain);
	PeriodIterator pin = new PeriodIterator(s, rdomain);
	PeriodIterator pout = new PeriodIterator(out);
	while (pin.hasMoreElements()) {
	    DataBlock bin = pin.nextElement().data;
	    DataBlock bout = pout.nextElement().data;
	    double c = bin.sum() / bin.getLength();
	    bout.set(c);
	}
	return out;
    }

    @Override
    public String getDescription() {
        return "Stable filter";
    }

}
