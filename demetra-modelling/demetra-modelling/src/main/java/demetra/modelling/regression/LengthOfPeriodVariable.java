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
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.Utility;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LengthOfPeriodVariable implements ILengthOfPeriodVariable {

    private final LengthOfPeriodType type;
    private final String name;

    public LengthOfPeriodVariable(LengthOfPeriodType type) {
        if (type == LengthOfPeriodType.None) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.name = ILengthOfPeriodVariable.NAME;
    }

    public LengthOfPeriodVariable(LengthOfPeriodType type, String name) {
        if (type == LengthOfPeriodType.None) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.name = name;
    }

    public LengthOfPeriodType getType() {
        return type;
    }

    @Override
    public void data(TsDomain domain, List<DataBlock> data) {
        if (type == LengthOfPeriodType.LeapYear) {
            lp(domain, data.get(0));
        } else {
            length(domain, data.get(0));
        }
    }

    @Override
    public String getDescription(TsDomain context) {
        switch (type) {
            case LeapYear:
                return "Leap year";
            case LengthOfPeriod:
                return "Length of period";
            default:
                return null;
        }
    }

    @Override
    public ITsVariable<TsDomain> rename(String name) {
        return new LengthOfPeriodVariable(type, name);
    }

    private void lp(TsDomain domain, DataBlock buffer) {
        int freq = domain.getTsUnit().ratioOf(TsUnit.YEAR);
        if (freq < 2) {
            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
        }
        TsPeriod start = domain.getStartPeriod();
        if (!start.getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        int n = domain.getLength();
        int period = 0;
        if (freq == 12) {
            period = 1;
        }
        // position of the starting period in the year
        int pos = (start.start().getMonthValue() - 1) % freq;
        int idx = period - pos;
        if (idx < 0) {
            idx += freq;
        }
        // position of the first period containing 29/2
        int lppos = idx;
        int year = domain.get(idx).year();
        while (!Utility.isLeap(year)) {
            lppos += freq;
            ++year;
        }

        buffer.extract(idx, -1, freq).set(-.25);
        buffer.extract(lppos, -1, 4 * freq).set(.75);
    }

    private void length(TsDomain domain, DataBlock buffer) {
        int freq = domain.getTsUnit().ratioOf(TsUnit.YEAR);
        if (freq < 2) {
            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
        }
        TsPeriod start = domain.getStartPeriod();
        if (!start.getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        int[] ndays = Utility.daysCount(domain);
        final double m = 365.25 / freq;
        buffer.set(i -> ndays[i] - m);
    }

}
