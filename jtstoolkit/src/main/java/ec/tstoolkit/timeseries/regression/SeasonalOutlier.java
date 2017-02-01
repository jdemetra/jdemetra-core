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
import ec.tstoolkit.maths.polynomials.UnitRoots;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SeasonalOutlier extends AbstractOutlierVariable {

    public static final String CODE="SO";

    boolean zeroEnded;

    /**
     * 
     * @param pos
     */
    public SeasonalOutlier(Day pos) {
        super(pos);
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        TsPeriod pstart=new TsPeriod(start.getFrequency(), position);
        int pos = pstart.minus(start);
        data.set(0);
        int freq = start.getFrequency().intValue();

        double z = -1.0 / (freq - 1);
        int xpos;
        int len = data.getLength();
        if (zeroEnded) {
            int j = 1;
            if (pos < 0) {
                return;
            } else if (pos > len) {
                int n = pos - len;
                j = 1 + n % freq;
                xpos = len;
            } else {
                xpos = pos;
            }
            do {
                for (; j < freq && xpos > 0; ++j) {
                    data.set(--xpos, z);
                }
                if (xpos > 0) {
                    data.set(--xpos, 1);
                } else {
                    break;
                }
                j = 1;
            } while (true);
        } else {
            if (pos < 0) {
                xpos = pos % freq;
            } else {
                xpos = pos;
            }
            if (xpos < 0) {
                int max=Math.min(len, freq + xpos);
                for (int j = 0; j < max; ++j) {
                    data.set(j, z);
                }
                xpos += freq;

            }

            for (int i = xpos; i < len;) {
                data.set(i++, 1);
                for (int j = 1; j < freq && i < len; ++i, ++j) {
                    data.set(i, z);
                }
            }
        }
    }

    @Override
    public OutlierType getOutlierType() {
        return OutlierType.SO;
    }
    
    public String getCode(){
        return CODE;
    }
    
    @Override
    public boolean isSignificant(TsDomain domain) {
        if (domain.getFrequency() == TsFrequency.Yearly)
            return false;
        return domain.search(position) >= 0;
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
    public FilterRepresentation getFilterRepresentation(int freq) {
        if (freq == 1) {
            return null;
        } else {
            return new FilterRepresentation(new RationalBackFilter(
                    BackFilter.ONE, new BackFilter(UnitRoots.D(freq))), 0);
        }
    }
}
