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
import ec.tstoolkit.maths.linearfilters.BackFilter;
import ec.tstoolkit.maths.linearfilters.RationalBackFilter;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class AdditiveOutlier extends AbstractOutlierVariable {

    public static final String CODE="AO";
    /**
     *
     * @param p
     */
    public AdditiveOutlier(Day pos) {
        super(pos);
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        TsPeriod pstart=new TsPeriod(start.getFrequency(), position);
        int pos = pstart.minus(start);
        data.set(0);
        if (pos >= 0 && pos < data.getLength()) {
            data.set(pos, 1);
        }
    }

    @Override
    public OutlierType getOutlierType() {
        return OutlierType.AO;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.search(position) >= 0;
    }

    @Override
    public FilterRepresentation getFilterRepresentation(int freq) {

        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, BackFilter.ONE), 0);
    }

    @Override
    public String getCode() {
        return CODE;
     }

}
