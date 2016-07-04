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
public class LevelShift extends AbstractOutlierVariable {
    
    public static final String CODE="LS";

    boolean zeroEnded = true;

    /**
     * 
     * @param p
     */
    public LevelShift(Day p) {
        super(p);
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        int n = data.getLength();
        double Zero = zeroEnded ? -1 : 0, One = zeroEnded ? 0 : 1;
        TsPeriod pstart=new TsPeriod(start.getFrequency(), position);
        int xpos = pstart.minus(start);
        if (xpos <= 0) {
            data.set(One);
        } else if (xpos >= n) {
            data.set(Zero);
        } else {
            data.range(0, xpos).set(Zero);
            data.range(xpos, n).set(One);
        }
    }

    @Override
    public OutlierType getOutlierType() {
        return OutlierType.LS;
    }
    
    @Override
    public String getCode(){
        return CODE;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        return domain.search(position) > 0;
    }

    /**
     * 
     * @return
     */
    public boolean isZeroEnded() {
        return zeroEnded;
    }

    /**
     * 
     * @param value
     */
    public void setZeroEnded(boolean value) {
        zeroEnded = value;
    }

    @Override
    public FilterRepresentation getFilterRepresentation(int freq){

        return new FilterRepresentation (new RationalBackFilter(
            BackFilter.ONE, BackFilter.D1), zeroEnded ? -1 : 0);
    }
}
