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
package jdplus.timeseries.simplets;

import demetra.data.DoubleSeq;
import jdplus.data.transformation.LogJacobian;
import nbbrd.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.CalendarUtility;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.data.Doubles;
import java.util.Arrays;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
class LengthOfPeriodTransformation implements TsDataTransformation {

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
    public TsDataTransformation converse() {
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
//        if (ratio < 2) {
//            throw new TsException(TsException.INCOMPATIBLE_DOMAIN);
//        }
        if (type == LengthOfPeriodType.LengthOfPeriod) {
            return length(data, ratio, ljacobian);
        } else {
            return lp(data, ratio, ljacobian);
        }
    }

    @Override
    public double transform(TsPeriod p, double value) {
        // TODO: optimize        
        TsData s = TsData.of(p, DoubleSeq.of(value));
        TsData t = transform(s, null);
        return t.getValue(0);
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
            }
            if (lj != null) {
                if (lj.missing == null) {
                    for (int i = lj.start; i < lj.end; ++i) {
                        if (Double.isFinite(data[i])) {
                            double fac = ndays[i] / m;
                            lj.value += Math.log(fac);
                        }
                    }
                } else {
                    int nmissing = lj.missing.length;
                    int imissing = 0, ic = lj.start;
                    while (imissing < nmissing && lj.missing[imissing] < ic) {
                        ++imissing;
                    }
                    while (imissing != nmissing && ic < lj.end) {
                        if (ic == lj.missing[imissing]) {
                            ++ic;
                            ++imissing;
                        } else {
                            double fac = ndays[ic++] / m;
                            lj.value += Math.log(fac);
                        }
                    }
                    while (ic < lj.end) {
                        double fac = ndays[ic++] / m;
                        lj.value += Math.log(fac);
                    }
                }
            }
        } else {
            for (int i = 0; i < ndays.length; ++i) {
                double fac = m / ndays[i];
                data[i] *= fac;
            }
            if (lj != null) {
                if (lj.missing == null) {
                    for (int i = lj.start; i < lj.end; ++i) {
                        if (Double.isFinite(data[i])) {
                            double fac = m / ndays[i];
                            lj.value += Math.log(fac);
                        }
                    }
                } else {
                    int nmissing = lj.missing.length;
                    int imissing = 0, ic = lj.start;
                    while (imissing < nmissing && lj.missing[imissing] < ic) {
                        ++imissing;
                    }
                    while (imissing != nmissing && ic < lj.end) {
                        if (ic == lj.missing[imissing]) {
                            ++ic;
                            ++imissing;
                        } else {
                            double fac = m / ndays[ic++];
                            lj.value += Math.log(fac);
                        }
                    }
                    while (ic < lj.end) {
                        double fac = m / ndays[ic++];
                        lj.value += Math.log(fac);
                    }
                }
            }
        }
        return TsData.ofInternal(tsdata.getStart(), data);
    }

    private TsData lp(TsData tsdata, int freq, LogJacobian lj) {
        if (!tsdata.hasDefaultEpoch()) {
            throw new UnsupportedOperationException();
        }
        TsDomain domain = tsdata.getDomain();
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
        switch (freq) {
            case 12:
                ndays = 28;
                break;
            case 1:
                ndays=365;
                break;
            default:
                ndays = CalendarUtility.getCumulatedMonthDays(1 + 12 / freq);
                break;
        }
        double[] data = tsdata.getValues().toArray();
        if (back) {
            double leap = (ndays + 1) / (ndays + .25);
            double nleap = ndays / (ndays + .25);
            while (idx < domain.getLength()) {
                double fac = (idx - lppos) % (4 * freq) != 0 ? nleap : leap;
                data[idx] *= fac;
                if (lj != null && idx >= lj.start && idx < lj.end) {
                    if (lj.missing == null) {
                        if (Double.isFinite(data[idx])) {
                            lj.value += Math.log(fac);
                        }
                    } else {
                        int lpos = Arrays.binarySearch(lj.missing, idx);
                        if (lpos < 0) {
                            lj.value += Math.log(fac);
                        }
                    }
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
                    if (lj.missing == null) {
                        if (Double.isFinite(data[idx])) {
                            lj.value += Math.log(fac);
                        }
                    } else {
                        int lpos = Arrays.binarySearch(lj.missing, idx);
                        if (lpos < 0) {
                            lj.value += Math.log(fac);
                        }
                    }
                }
                idx += freq;
            }
        }
        return TsData.ofInternal(start, data);
    }
}
