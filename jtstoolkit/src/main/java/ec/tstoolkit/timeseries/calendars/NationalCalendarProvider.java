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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.DayOfWeek;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class NationalCalendarProvider extends DefaultGregorianCalendarProvider {

    public NationalCalendarProvider() {
        m_ncal = new NationalCalendar();
    }

    public NationalCalendarProvider(Collection<SpecialDayEvent> sd) {
        m_ncal = new NationalCalendar();
        for (SpecialDayEvent d : sd) {
            m_ncal.add(d);
        }
    }

    public NationalCalendarProvider(NationalCalendar ncal) {
        m_ncal = ncal.clone();
    }

    public Collection<SpecialDayEvent> events() {
        return m_ncal.elements();
    }

    @Override
    public List<DataBlock> holidays(TradingDaysType dtype, TsDomain domain) {
        switch (dtype) {
            case TradingDays:
                double[][] h = TDHolidays(domain);
                List<DataBlock> rslt = new ArrayList<>();
                for (int i = 0; i < h.length; ++i) {
                    rslt.add(new DataBlock(h[i]));
                }
                return rslt;
            case WorkingDays:
                double[] h2 = WHolidays(domain);
                List<DataBlock> rslt2 = new ArrayList<>();
                rslt2.add(new DataBlock(h2));
                return rslt2;
        }
        return null;
    }

    private double[] WHolidays(TsDomain domain) {
        double[][] tdh = holidays(domain);
        double[] wh = new double[domain.getLength()];
        for (int i = 0; i < wh.length; ++i) {
            int sum = 0;
            for (int j = 0; j < 5; ++j) {
                sum += tdh[j][i];
            }
            //int sumf = tdh[5][i] + tdh[6][i];
            double d = sum;// -2.5 * sumf;
            if (Math.abs(d) < 1e-9) {
                d = 0;
            }
            wh[i] = d;
        }
        if (m_mean) {
            int ifreq = domain.getFrequency().intValue();
            for (SpecialDayEvent day : m_ncal.elements()) {
                Day start = day.getStart(), end = day.getEnd();
                TsDomain xdomain = day.day.getSignificantDomain(domain.getFrequency(), start, end);
                TsDomain cdomain = domain.intersection(xdomain);
//                TsPeriod ps = xdomain.getStart(),
//                        pe = xdomain.getLast();
//                int is = ps.minus(domain.getStart());
//                if (is < 0) {
//                    is = 0;
//                    ps = domain.getStart();
//                }
//                int ie = domain.getEnd().minus(pe);
//                if (ie < 0) {
//                    ie = 0;
//                }
//                ie = domain.getLength() - ie;

                //               if (is < domain.getLength() && ie > 0) {
                if (!cdomain.isEmpty()) {
                    double[][] cur = day.day.getLongTermMeanEffect(ifreq);
                    if (cur != null) {
                        int del = cdomain.getStart().minus(domain.getStart());
                        for (int p = 0; p < ifreq; ++p) {
                            if (cur[p] != null) {
                                int i = p - cdomain.getStart().getPosition();
                                if (i < 0) {
                                    i += ifreq;
                                }
                                double m = 0;
                                for (int j = 0; j < 5; ++j) {
                                    m += cur[p][j];
                                }
                                if (Math.abs(m) > 1e-9) {
                                    while (i < cdomain.getLength()) {
                                        wh[i + del] -= m;
                                        i += ifreq;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return wh;
    }

//    public double[][] getLongTermMeanCorrection(int freq) {
//        double[][] rslt = null;
//        for (SpecialDayEvent day : m_ncal.elements()) {
//            double[][] cur = day.day.getLongTermMeanEffect(freq);
//            if (cur != null) {
//                if (rslt == null) {
//                    rslt = cur;
//                } else {
//                    for (int i = 0; i < cur.length; ++i) {
//                        if (cur[i] != null) {
//                            if (rslt[i] == null) {
//                                rslt[i] = cur[i];
//                            } else {
//                                for (int j = 0; j < 6; ++j) {
//                                    rslt[i][j] += cur[i][j];
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return rslt;
//    }
    private double[][] TDHolidays(TsDomain domain) {
        double[][] h = holidays(domain);

        double[][] tdh = new double[6][];
        for (int i = 0; i < 6; ++i) {
            double[] tmp = new double[domain.getLength()];
            for (int j = 0; j < tmp.length; ++j) {
                tmp[j] = h[i][j];
            }
            tdh[i] = tmp;
        }

        if (m_mean) {
            int ifreq = domain.getFrequency().intValue();
            // iterates through all days
            for (SpecialDayEvent day : m_ncal.elements()) {
                Day start = day.getStart(), end = day.getEnd();
                TsDomain xdomain = day.day.getSignificantDomain(domain.getFrequency(), start, end);
                TsDomain cdomain = domain.intersection(xdomain);

//                TsPeriod ps = new TsPeriod(domain.getFrequency(), start),
//                        pe = new TsPeriod(domain.getFrequency(), end);
//                int is = ps.minus(domain.getStart());
//                if (is < 0) {
//                    is = 0;
//                    ps = domain.getStart();
//                }
//                int ie = domain.getEnd().minus(pe);
//                if (ie < 0) {
//                    ie = 0;
//                }
//                ie = domain.getLength() - ie;
//
//                if (is < domain.getLength() && ie > 0) {
                if (!cdomain.isEmpty()) {
                    double[][] cur = day.day.getLongTermMeanEffect(ifreq);
                    if (cur != null) {
                        int del = cdomain.getStart().minus(domain.getStart());
                        for (int p = 0; p < ifreq; ++p) {
                            if (cur[p] != null) {
                                int i = p - cdomain.getStart().getPosition();
                                if (i < 0) {
                                    i += ifreq;
                                }
                                while (i < cdomain.getLength()) {
                                    for (int j = 0; j < 6; ++j) {
                                        tdh[j][i + del] -= cur[p][j];
                                    }
                                    i += ifreq;
                                }
                            }
                        }
                    }
                }
            }
        }
        return tdh;
    }

    private double[][] holidays(TsDomain domain) {
        double[][] h = new double[6][];
        for (int i = 0; i < 6; ++i) {
            h[i] = new double[domain.getLength()];
        }

        Day dstart = domain.getStart().firstday();
        Day dend = domain.getLast().lastday();
        Map<Day, Double> used = new HashMap<>();
        for (SpecialDayEvent ev : m_ncal.elements()) {
            Day start = ev.getStart(), end = ev.getEnd();
            if (start.isBefore(dstart)) {
                start = dstart;
            }
            if (end.isAfter(dend)) {
                end = dend;
            }
            if (start.isBefore(end)) {
                for (IDayInfo info : ev.day.getIterable(domain.getFrequency(), start, end)) {
                    Day curday = info.getDay();
                    Double Weight = used.get(curday);
                    double weight = ev.day.getWeight();
                    if (Weight == null || weight > Weight.doubleValue()) {
                        used.put(curday, weight);
                        TsPeriod cur = info.getPeriod();
                        DayOfWeek w = info.getDayOfWeek();
                        if (w != DayOfWeek.Sunday) {
                            int pos = domain.search(cur);
                            if (pos >= 0) {
                                int col = -1 + w.intValue();
                                h[col][pos] += Weight == null ? weight : weight - Weight.doubleValue();
                            }
                        }
                    }
                }
            }
        }
        return h;
    }

    @Override
    protected void tradingDays(TsDomain domain, List<DataBlock> buffer) {
        int n = domain.getLength();
        super.tradingDays(domain, buffer);
        double[][] h = TDHolidays(domain);
        for (int j = 0; j < n; ++j) {
            double s = 0;
            for (int i = 0; i < h.length; ++i) {
                s -= h[i][j];
            }
            for (int i = 0; i < h.length; ++i) {
                double del = s - h[i][j];
                double idel = Math.round(del);
                if (Math.abs(del - idel) < 1e-9) {
                    del = idel;
                }

                if (del != 0) {
                    buffer.get(i).add(j, del);
                }
            }
        }
    }

    @Override
    protected void workingDays(TsDomain domain, DataBlock buffer) {
        int n = domain.getLength();
        super.workingDays(domain, buffer);
        double[] h = WHolidays(domain);
        for (int i = 0; i < h.length; ++i) {
            if (h[i] != 0) {
                buffer.add(i, -3.5 * h[i]);
            }
        }
    }

    public boolean isLocked() {
        return m_locked;
    }

    public void setLocked() {
        m_locked = true;
    }

    public void add(ISpecialDay day) {
        if (!m_locked) {
            m_ncal.add(day);
        }
    }

    public void add(SpecialDayEvent evday) {
        if (!m_locked) {
            m_ncal.add(evday);
        }
    }

    public boolean isLongTermMeanCorrection() {
        return m_mean;
    }

    public void setLongTermMeanCorrection(boolean value) {
        m_mean = value;
    }
    private boolean m_mean = true;
    private NationalCalendar m_ncal;
    private boolean m_locked;
    public static final double EPS = 1e-9;

    public boolean contentEquals(NationalCalendarProvider other) {
        return m_ncal.contentEquals(other.m_ncal);
    }
}
