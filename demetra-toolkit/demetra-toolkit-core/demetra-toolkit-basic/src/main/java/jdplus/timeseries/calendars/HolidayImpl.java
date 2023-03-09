/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.timeseries.calendars;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.calendars.CalendarUtility;
import demetra.timeseries.calendars.Easter;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.FixedWeekDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import demetra.timeseries.calendars.SingleDate;
import java.time.LocalDate;

/**
 *
 * @author palatej
 */
interface HolidayImpl {

    Iterable<HolidayInfo> getIterable(int freq, LocalDate start, LocalDate end);

    double[][] getLongTermMeanEffect(int freq);

    TsDomain getSignificantDomain(int freq, LocalDate start, LocalDate end);

    static HolidayImpl implementationOf(Holiday holiday) {
        if (holiday instanceof FixedDay fd) {
            return new FixedDayImpl(fd);
        }
        if (holiday instanceof EasterRelatedDay sd) {
            return new EasterDayImpl(sd);
        }
        if (holiday instanceof SingleDate sd) {
            return new SingleDateImpl(sd);
        }
        if (holiday instanceof FixedWeekDay fd) {
            return new FixedWeekDayImpl(fd);
        }
        if (holiday instanceof PrespecifiedHoliday ph) {
            return implementationOf(ph.rawHoliday().forPeriod(ph.start(), ph.end()));
        }

        return null;
    }

    static class FixedDayImpl implements HolidayImpl {

        final FixedDay definition;

        FixedDayImpl(FixedDay definition) {
            this.definition = definition;
        }

        @Override
        public Iterable<HolidayInfo> getIterable(int freq, LocalDate start, LocalDate end) {
            return new FixedDayInfo.FixedDayIterable(definition, start, end);
        }

        @Override
        public double[][] getLongTermMeanEffect(int freq) {
            int c = 12 / freq;
            int p = (definition.getMonth() - 1) / c;
            double[] m = new double[7];

            for (int i = 0; i < 7; ++i) {
                m[i] = definition.getWeight() / 7;
            }

            double[][] rslt = new double[freq][];
            rslt[p] = m;
            return rslt;
        }

        @Override
        public TsDomain getSignificantDomain(int freq, LocalDate start, LocalDate end) {
            LocalDate vstart=definition.start(), vend=definition.end();
            if (vstart.isAfter(start))
                start=vstart;
            if (vend.isBefore(end))
                end=vend;
            TsPeriod pstart = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), start),
                    pend = TsPeriod.of(TsUnit.ofAnnualFrequency(freq), end);
            LocalDate sday = LocalDate.of(pstart.year(), definition.getMonth(), definition.getDay());
            if (start.isAfter(sday)) {
                pstart = pstart.next();
            }
            LocalDate eday = LocalDate.of(pend.year(), definition.getMonth(), definition.getDay());
            if (! eday.isBefore(end)) {
                pend = pend.previous();
            }
            int n = pstart.until(pend) + 1;
            return TsDomain.of(pstart, Math.max(0, n));
        }
    }

    static class FixedWeekDayImpl implements HolidayImpl {

        FixedWeekDayImpl(FixedWeekDay definition) {
            this.definition = definition;
        }

        final FixedWeekDay definition;

        @Override
        public Iterable<HolidayInfo> getIterable(int freq, LocalDate start, LocalDate end) {
            return new FixedWeekDayInfo.FixedWeekDayIterable(definition, start, end);
        }

        @Override
        public double[][] getLongTermMeanEffect(int freq) {
            int c = 12 / freq;
            int p = (definition.getMonth() - 1) / c;
            double[] m = new double[7];

            for (int i = 0; i < 7; ++i) {
                m[i] = definition.getWeight() / 7;
            }

            double[][] rslt = new double[freq][];
            rslt[p] = m;
            return rslt;
        }

        @Override
        public TsDomain getSignificantDomain(int freq, LocalDate start, LocalDate end) {
            LocalDate vstart=definition.start(), vend=definition.end();
            if (vstart.isAfter(start))
                start=vstart;
            if (vend.isBefore(end))
                end=vend;
            TsUnit unit = TsUnit.ofAnnualFrequency(freq);
            TsPeriod pstart = TsPeriod.of(unit, start), pend = TsPeriod.of(unit, end);
            LocalDate sday = CalendarUtility.firstWeekDay(definition.getDayOfWeek(), pstart.year(), definition.getMonth());
            if (definition.getPlace() > 0) {
                sday = sday.plusDays(definition.getPlace() * 7);
            }
            if (start.isAfter(sday)) {
                pstart = pstart.next();
            }
            LocalDate eday = CalendarUtility.firstWeekDay(definition.getDayOfWeek(), pend.year(), definition.getMonth());
            if (definition.getPlace() > 0) {
                eday = eday.plusDays(definition.getPlace() * 7);
            }
            if (! eday.isBefore(end)) {
                pend = pend.previous();
            }
            int n = pstart.until(pend) + 1;
            return TsDomain.of(pstart, Math.max(0, n));
        }
    }

    static class SingleDateImpl implements HolidayImpl {

        final SingleDate definition;

        SingleDateImpl(SingleDate definition) {
            this.definition = definition;
        }

        @Override
        public Iterable<HolidayInfo> getIterable(int freq, LocalDate start, LocalDate end) {
            return new SingleDateInfo.SingleDateIterable(definition, start, end);
        }

        @Override
        public double[][] getLongTermMeanEffect(int freq) {
            return null;
        }

        @Override
        public TsDomain getSignificantDomain(int freq, LocalDate start, LocalDate end) {
            TsUnit unit = TsUnit.ofAnnualFrequency(freq);
            TsPeriod pstart = TsPeriod.of(unit, definition.getDate());
            int n = 0;
            if (!definition.getDate().isBefore(start) && definition.getDate().isBefore(end)) {
                n = 1;
            }
            return TsDomain.of(pstart, n);
        }

    }

    static class EasterDayImpl implements HolidayImpl {

        private static final int START = 80, JSTART = 90, DEL = 35, JDEL = 43;
        private static final int[] MDAYS = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        final EasterRelatedDay definition;

        EasterDayImpl(EasterRelatedDay definition) {
            this.definition = definition;
        }

        @Override
        public Iterable<HolidayInfo> getIterable(int freq, LocalDate start, LocalDate end) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        /*
     * Raw estimation of the probability to get Easter at a specific date is defined below:
     * 22/3 (1/7)*1/LUNARY
     * 23/3 (2/7)*1/LUNARY
     * ...
     * 27/3 (6/7)*1/LUNARY
     * 28/3 1/LUNARY
     * ...
     * 18/4 1/LUNARY
     * 19/4 1/LUNARY + (1/7) * DEC_LUNARY/LUNARY = (7 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 20/4 (6/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY= (6 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 21/4 (5/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 22/4 (4/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 23/4 (3/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 24/4 (2/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 25/4 (1/7)*1/LUNARY + (1/7) *DEC_LUNARY/LUNARY
         */
        @Override
        public double[][] getLongTermMeanEffect(int freq) {
            int w = definition.getOffset() % 7;
            if (w == 0) {
                w = 7; // Sunday
            }
            if (w < 0) {
                w += 7;
            }
            // monday must be 0...
            --w;

            // Easter always falls between March, 22 and April, 25 (inclusive). The probability to get a specific day is defined by probEaster.
            // We don't take into account leap year. So, the solution is slightly wrong for offset
            // <= -50.
            // The considered day falls between ...
            int d0, d1;
            if (definition.isJulian()) {
                d0 = JSTART + definition.getOffset();
                d1 = d0 + JDEL;
            } else {
                d0 = START + definition.getOffset();
                d1 = d0 + DEL;
            }
            // d1 excluded

            int ifreq = (int) freq;
            int c = 12 / ifreq;

            int c0 = 0, c1 = 0;
            for (int i = 0; i < c; ++i) {
                c1 += MDAYS[i];
            }

            double[][] rslt = new double[ifreq][];
            for (int i = 0; i < ifreq;) {
                if (d0 < c1 && d1 > c0) {
                    double[] m = new double[7];
                    double x = 0;
                    for (int j = Math.max(d0, c0); j < Math.min(d1, c1); ++j) {
                        x += probEaster(j - d0);
                    }
                    m[w] = x * definition.getWeight();
                    rslt[i] = m;
                }
                // update c0, c1;
                c0 = c1;
                if (++i < ifreq) {
                    for (int j = 0; j < c; ++j) {
                        c1 += MDAYS[i * c + j];
                    }
                }
            }
            return rslt;
        }

        @Override
        public TsDomain getSignificantDomain(int freq, LocalDate start, LocalDate end) {
            LocalDate vstart=definition.start(), vend=definition.end();
            if (vstart.isAfter(start))
                start=vstart;
            if (vend.isBefore(end))
                end=vend;
            TsUnit unit = TsUnit.ofAnnualFrequency(freq);
            TsPeriod pstart = TsPeriod.of(unit, start), pend = TsPeriod.of(unit, end);
            LocalDate easter = Easter.easter(pstart.year());
            LocalDate sday = easter.plusDays(definition.getOffset());
            if (start.isAfter(sday)) {
                pstart = pstart.next();
            }
            easter = Easter.easter(pend.year());
            LocalDate eday = easter.plusDays(definition.getOffset());
            if (! eday.isBefore(end)) {
                pend.previous();
            }
            int n = pstart.until(pend) + 1;
            return TsDomain.of(pstart, Math.max(0, n));
        }

        private double probEaster(int del) {
            return definition.isJulian() ? Easter.probJulianEaster(del)
                    : Easter.probEaster(del);
        }
    }

}
