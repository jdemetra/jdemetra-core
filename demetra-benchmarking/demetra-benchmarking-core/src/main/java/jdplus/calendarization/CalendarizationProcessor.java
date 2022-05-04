/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package jdplus.calendarization;

import demetra.calendarization.Calendarization;
import demetra.calendarization.CalendarizationResults;
import demetra.calendarization.CalendarizationSpec;
import jdplus.data.DataBlock;
import jdplus.data.DataBlockStorage;
import nbbrd.design.Development;
import jdplus.math.matrices.QuadraticForm;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;
import demetra.timeseries.CalendarPeriodObs;
import demetra.timeseries.CalendarTimeSeries;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import nbbrd.service.ServiceProvider;

/**
 * See "Calendarization with splines and state space models" B. Quenneville, F.
 * Picard and S.Fortier Appl. Statistics (2013) 62, part 3, pp 371-399
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@ServiceProvider(Calendarization.Processor.class)
public class CalendarizationProcessor implements Calendarization.Processor {

    public static final CalendarizationProcessor PROCESSOR = new CalendarizationProcessor();

    @Override
    public CalendarizationResults process(CalendarTimeSeries data, CalendarizationSpec spec) {
        Impl monitor = new Impl(data, spec);
        return CalendarizationResults.builder()
                .start(monitor.start)
                .dailyData(monitor.sdays)
                .dailyStdev(monitor.esdays)
                .aggregatedSeries(monitor.aggts)
                .stdevAggregatedSeries(monitor.eaggts)
                .build();
    }

    private static class Impl {

        private final CalendarTimeSeries data;
        private final CalendarizationSpec spec;
        private double[] sdays, esdays;
        private TsData aggts, eaggts;
        // local buffer
        private double[] x, w;
        private int[] starts;
        private LocalDate start, end;

        private Impl(final CalendarTimeSeries data, final CalendarizationSpec spec) {
            this.data = data;
            this.spec = spec;
            process();
        }

        private boolean process() {
            prepare();
            if (spec.getAggregationUnit() == TsUnit.UNDEFINED) {
                if (!spec.isStdev()) {
                    return fastProcess();
                } else {
                    return fastFullProcess();
                }
            } else {
                return fullProcess();
            }
        }

        private void prepare() {
            // actual start/end for computation
            start = data.getPeriod(0).getStart();
            end = data.getPeriod(data.length() - 1).getEnd();
            if (spec.getStart() != null && spec.getStart().isBefore(start)) {
                start = spec.getStart();
            }
            if (spec.getEnd() != null && spec.getEnd().isAfter(end)) {
                end = spec.getEnd();
            }
            // creates the data.
            int n = (int) start.until(end, ChronoUnit.DAYS);
            x = new double[n];
            if (spec.getDailyWeights() != null) {
                if (spec.getDailyWeights().length == 7) {
                    w = new double[n];
                    int j = start.getDayOfWeek().getValue() - 1;
                    for (int i = 0; i < w.length; ++i) {
                        w[i] = spec.getDailyWeights()[j];
                        if (++j == 7) {
                            j = 0;
                        }
                    }
                } else if (spec.getDailyWeights().length >= n) {
                    w = spec.getDailyWeights();
                }
            }
            for (int i = 0; i < n; ++i) {
                x[i] = Double.NaN;
            }
            starts = new int[data.length() + 1];
            int idx = 0;
            for (CalendarPeriodObs obs : data) {
                starts[idx++] = (int) start.until(obs.getPeriod().start(), ChronoUnit.DAYS);
                int q = (int) start.until(obs.getPeriod().end(), ChronoUnit.DAYS) - 1;
                x[q] = obs.getValue();
            }
            starts[idx] = (int) start.until(data.getPeriod(idx - 1).end(), ChronoUnit.DAYS);
        }

        // processing without forecast errors
        private boolean fastProcess() {
            DataBlockStorage rslt = DkToolkit.fastSmooth(SsfCalendarization.of(starts, w, 1e-4), new SsfData(x));
            double[] c = rslt.item(1).toArray();

            if (w != null) {
                for (int i = 0; i < c.length; ++i) {
                    c[i] *= w[i];
                }
            }
            sdays = c;
            return true;
        }

        private boolean fastFullProcess() {

            DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(SsfCalendarization.of(starts, w, 1e-4), new SsfData(x), true, true);
            double[] c = srslts.getComponent(1).toArray();
            double[] e = srslts.getComponentVariance(1)
                    .map(q -> q <= 0 ? 0 : Math.sqrt(q))
                    .toArray();
            if (w != null) {
                for (int i = 0; i < c.length; ++i) {
                    c[i] *= w[i];
                    e[i] *= w[i];
                }
            }
            sdays = c;
            esdays = e;
            return true;
        }

        private boolean fullProcess() {

            // actual start/end for computation
            TsPeriod S = TsPeriod.of(spec.getAggregationUnit(), start);
            TsPeriod E = TsPeriod.of(spec.getAggregationUnit(), end.minusDays(1));
            int na = S.until(E) + 1;
            int[] astarts = new int[na];
            for (int i = 0; i < astarts.length; ++i) {
                astarts[i] = (int) Math.max(0, start.until(S.plus(i).start(), ChronoUnit.DAYS));
            }
            if (spec.isStdev()) {
                DefaultSmoothingResults srslts = DkToolkit.sqrtSmooth(SsfCalendarizationEx.of(starts, astarts, w, 1e-4), new SsfData(x), true, true);
                double[] c = srslts.getComponent(2).toArray();
                if (w != null) {
                    for (int i = 0; i < c.length; ++i) {
                        c[i] *= w[i];
                    }
                }
                sdays = c;

                if (esdays == null) {
                    double[] e = srslts.getComponentVariance(2)
                            .map(q -> q <= 0 ? 0 : Math.sqrt(q))
                            .toArray();
                    if (w != null) {
                        for (int i = 0; i < e.length; ++i) {
                            e[i] *= w[i];
                        }
                    }
                    esdays = e;
                }
                int[] aends = new int[astarts.length];
                for (int i = 1; i < astarts.length; ++i) {
                    aends[i - 1] = astarts[i] - 1;
                }
                aends[aends.length - 1] = sdays.length - 1;
                DataBlock Z = DataBlock.of(new double[]{0, 1, 1});
                Z.set(1, 1);
                Z.set(2, 1);
                double[] ax = new double[aends.length],
                        eax = new double[aends.length];
                for (int i = 0; i < aends.length; ++i) {
                    int icur = aends[i];
                    if (w != null) {
                        Z.set(2, w[icur]);
                    }
                    ax[i] = srslts.a(icur).dot(Z);
                    eax[i] = Math.sqrt(Math.max(0, QuadraticForm.apply(srslts.P(icur), Z)));
                }
                aggts = TsData.ofInternal(S, ax);
                eaggts = TsData.ofInternal(S, eax);
                return true;
            } else {
                DataBlockStorage rslt = DkToolkit.fastSmooth(SsfCalendarizationEx.of(starts, astarts, w, 1e-4), new SsfData(x));
                double[] c = rslt.item(2).toArray();
                if (w != null) {
                    for (int i = 0; i < c.length; ++i) {
                        c[i] *= w[i];
                    }
                }
                sdays = c;

                int[] aends = new int[astarts.length];
                for (int i = 1; i < astarts.length; ++i) {
                    aends[i - 1] = astarts[i] - 1;
                }
                aends[aends.length - 1] = sdays.length - 1;
                DataBlock Z = DataBlock.of(new double[]{0, 1, 1});
                Z.set(1, 1);
                Z.set(2, 1);
                double[] ax = new double[aends.length];
                for (int i = 0; i < aends.length; ++i) {
                    int icur = aends[i];
                    if (w != null) {
                        Z.set(2, w[icur]);
                    }
                    ax[i] = rslt.block(icur).dot(Z);
                }
                aggts = TsData.ofInternal(S, ax);
                return true;
            }
        }

//        private TsData makeTsData() {
//            LocalDate start = data.get(0).getPeriod().getStart(), end = data.get(data.length() - 1).getPeriod().getEnd();
//            if (spec.getStart().isBefore(start)) {
//                start = spec.getStart();
//            }
//            if (spec.getEnd().isAfter(end)) {
//                end = spec.getEnd();
//            }
//            TsPeriod S = TsPeriod.of(spec.getAggregationUnit(), start);
//            TsPeriod E = TsPeriod.of(spec.getAggregationUnit(), end);
//            int n = S.until(E);
//            double[] sum = new double[n];
//            TsPeriod cur = S;
//            for (int i = 0, j0 = 0, j1 = 0; i < sum.length; ++i) {
//                j1 = Math.min((int) start.until(cur.end(), ChronoUnit.DAYS), sdays.length);
//                double s = 0;
//                for (int j = j0; j < j1; ++j) {
//                    s += sdays[j];
//                }
//                sum[i] = s;
//                cur = cur.next();
//                j0 = j1;
//            }
//
//            return TsData.of(S, DoubleSequence.of(sum));
//        }
    }
}
