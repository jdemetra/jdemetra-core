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

import demetra.data.DoubleSequence;
import demetra.data.transformation.DataTransformation.LogJacobian;
import demetra.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.CalendarUtility;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LengthOfPeriodTransformation implements ITsTransformation {

    private final boolean back;
    private final LengthOfPeriodType type;

    /**
     *
     * @param ltype
     */
    public LengthOfPeriodTransformation(LengthOfPeriodType ltype) {
        this(ltype, false);
    }

    private LengthOfPeriodTransformation(LengthOfPeriodType ltype, boolean back) {
        this.type = ltype;
        this.back = back;
    }

    /**
     *
     * @return
     */
    @Override
    public ITsTransformation converse() {
        return new LengthOfPeriodTransformation(type, !back);
    }

    /**
     *
     * @param data
     * @param ljacobian
     * @return
     */
    @Override
    public TsData transform(TsData data, LogJacobian ljacobian) {
        int ratio = data.getTsUnit().ratioOf(TsUnit.YEAR);
        if (ratio < 2) {
            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
        }
        if (type == LengthOfPeriodType.LengthOfPeriod) {
            return length(data, ratio, ljacobian);
        } else {
            return lp(data, ratio, ljacobian);
        }
    }

    private TsData length(TsData tsdata, int ratio, LogJacobian lj) {
        int[] ndays = CalendarUtility.daysCount(tsdata.getDomain());
        // average length of a period
        double m = 365.25 / ratio;

        double[] data = tsdata.getValues().toArray();
        if (back) {
            for (int i = 0; i < ndays.length; ++i) {
                double fac = ndays[i] / m;
                data[i] *= fac;
                if (lj != null && i >= lj.start && i < lj.end) {
                    lj.value += Math.log(fac);
                }
            }
        } else {
            for (int i = 0; i < ndays.length; ++i) {
                double fac = m / ndays[i];
                data[i] *= fac;
                if (lj != null && i >= lj.start && i < lj.end) {
                    lj.value += Math.log(fac);
                }
            }
        }
        return TsData.of(tsdata.getStart(), DoubleSequence.ofInternal(data));
    }

    private TsData lp(TsData tsdata, int freq, LogJacobian lj) {
        if (!tsdata.getStart().getEpoch().equals(TsPeriod.DEFAULT_EPOCH)) {
            throw new UnsupportedOperationException();
        }
        TsDomain domain = tsdata.getDomain();
        int n = domain.getLength();
        int period = 0;
        if (freq == 12) {
            period = 1;
        }
        // position of the starting period in the year
        TsPeriod start = domain.getStartPeriod();
        int pos = (start.start().getMonthValue() - 1) % freq;
        int idx = period - pos;
        if (idx < 0) {
            idx += freq;
        }
        // position of the first period containing 29/2
        int lppos = idx;
        int year = domain.get(idx).year();
        while (!CalendarUtility.isLeap(year)) {
            lppos += freq;
            ++year;
        }

        int ndays = 0;
        if (freq == 12) {
            ndays = 28;
        } else {
            ndays = CalendarUtility.getCumulatedMonthDays(1 + 12 / freq);
        }
        double[] data = tsdata.getValues().toArray();
        if (back) {
            double leap = (ndays + 1) / (ndays + .25);
            double nleap = ndays / (ndays + .25);
            while (idx < domain.getLength()) {
                double fac = (idx - lppos) % (4 * freq) != 0 ? nleap : leap;
                data[idx] *= fac;
                if (lj != null && idx >= lj.start && idx < lj.end) {
                    lj.value += Math.log(fac);
                }
                idx += freq;
            }
        } else {
            double leap = (ndays + .25) / (ndays + 1);
            double nleap = (ndays + .25) / ndays;
            while (idx < domain.getLength()) {
                double fac = (idx - lppos) % (4 * freq) != 0 ? nleap : leap;
                data[idx] *= fac;
                if (lj != null && idx >= lj.start && idx < lj.end) {
                    lj.value += Math.log(fac);
                }
                idx += freq;
            }
        }
        return TsData.of(start, DoubleSequence.ofInternal(data));
    }
}
