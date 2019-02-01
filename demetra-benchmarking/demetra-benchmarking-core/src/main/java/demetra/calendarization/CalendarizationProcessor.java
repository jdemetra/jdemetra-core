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
package demetra.calendarization;

import demetra.design.Development;
import demetra.timeseries.CalendarTimeSeries;
import demetra.timeseries.TsData;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * See "Calendarization with splines and state space models" B. Quenneville, F.
 * Picard and S.Fortier Appl. Statistics (2013) 62, part 3, pp 371-399
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@ServiceProvider(service = Calendarization.Processor.class)
public class CalendarizationProcessor implements Calendarization.Processor {

    @Override
    public CalendarizationResults process(CalendarTimeSeries data, CalendarizationSpec spec) {
        Impl monitor=new Impl(data, spec);
        return null;
    }

    private static class Impl {

        private final CalendarTimeSeries data;
        private final CalendarizationSpec spec;
        private double[] s_, es_;
        private TsData[] output_;
        
        private Impl(final CalendarTimeSeries data, final CalendarizationSpec spec){
            this.data=data;
            this.spec=spec;
        }


        private boolean process() {
            if (!spec.isStdev()) {
                return fastProcess();
            } else {
                return fullProcess();
            }
        }

        // processing without forecast errors
        private boolean fastProcess() {
            if (s_ != null) {
                return true;
            }
            // actual start/end for computation
            LocalDate start = data.getPeriod(0).getStart(), end = data.getPeriod(data.length() - 1).getEnd();
            if (spec.getStart() != null && spec.getStart().isBefore(start)) {
                start = spec.getStart();
            }
            if (spec.getEnd() != null && spec.getEnd().isAfter(end)) {
                end = spec.getEnd();
            }
            // creates the data.
            int n=(int)start.until(end, ChronoUnit.DAYS);
            double[] x = new double[n];
            double[] w;
            if (spec.getDailyWeights() != null) {
                w = new double[n];
                int j = start.getDayOfWeek().getValue()-1;
                for (int i = 0; i < w.length; ++i) {
                    w[i] = spec.getDailyWeights()[j];
                    if (++j == 7) {
                        j = 0;
                    }
                }
            } else {
                w = null;
            }
            for (int i = 0; i < n; ++i) {
                data[i] = Double.NaN;
            }
            int[] starts = new int[data_.size()];
            int idx = 0;
            for (PeriodObs obs : data_) {
                starts[idx++] = obs.start.difference(start);
                int n = obs.end.difference(start);
                data[n] = obs.value;
            }

            DisturbanceSmoother smoother = new DisturbanceSmoother();
            smoother.setSsf(new SsfCalendarization(starts, w));
            smoother.process(new SsfData(data, null));
            SmoothingResults sstates = smoother.calcSmoothedStates();
            double[] c = sstates.component(1);

            if (w != null) {
                for (int i = 0; i < c.length; ++i) {
                    c[i] *= w[i];
                }
            }
            s_ = c;
            return true;
        }

        private boolean fullProcess(TsFrequency freq) {
            if (freq == TsFrequency.Undefined) {
                if (s_ != null) {
                    return true;
                } else {
                    return fastFullProcess();
                }
            } else if (output_.containsKey(freq)) {
                return true;
            }

            // actual start/end for computation
            Day start = data_.get(0).start, end = data_.get(data_.size() - 1).end;
            if (start_.isBefore(start)) {
                start = start_;
            }
            if (end_.isAfter(end)) {
                end = end_;
            }
            // creates the data.
            double[] data = new double[end.difference(start) + 1];
            double[] w;
            if (dweights_ != null) {
                w = new double[data.length];
                int j = start.getDayOfWeek().intValue();
                for (int i = 0; i < w.length; ++i) {
                    w[i] = dweights_[j];
                    if (++j == 7) {
                        j = 0;
                    }
                }
            } else {
                w = null;
            }
            for (int i = 0; i < data.length; ++i) {
                data[i] = Double.NaN;
            }

            int[] starts = new int[data_.size()];

            TsPeriod S = new TsPeriod(freq, start);
            TsPeriod E = new TsPeriod(freq, end);
            int[] astarts = new int[E.minus(S) + 1];
            for (int i = 0; i < astarts.length; ++i) {
                astarts[i] = Math.max(0, S.plus(i).firstday().difference(start));
            }

            int idx = 0;
            for (PeriodObs obs : data_) {
                starts[idx++] = obs.start.difference(start);
                int n = obs.end.difference(start);
                data[n] = obs.value;
            }

            Smoother smoother = new Smoother();
            smoother.setCalcVar(true);
            smoother.setSsf(new SsfCalendarizationEx(starts, astarts, w));
            SmoothingResults sstates = new SmoothingResults(true, true);
            smoother.process(new SsfData(data, null), sstates);
            if (s_ == null) {
                double[] c = sstates.component(2);
                if (w != null) {
                    for (int i = 0; i < c.length; ++i) {
                        c[i] *= w[i];
                    }
                }
                s_ = c;
            }
            if (es_ == null) {
                double[] e = sstates.componentStdev(2);
                if (w != null) {
                    for (int i = 0; i < e.length; ++i) {
                        e[i] *= w[i];
                    }
                }
                es_ = e;
            }
            TsData X = new TsData(S, astarts.length);
            TsData EX = new TsData(S, astarts.length);
            int[] aends = new int[astarts.length];
            for (int i = 1; i < astarts.length; ++i) {
                aends[i - 1] = astarts[i] - 1;
            }
            aends[aends.length - 1] = s_.length - 1;
            DataBlock Z = new DataBlock(3);
            Z.set(1, 1);
            Z.set(2, 1);
            for (int i = 0; i < aends.length; ++i) {
                int icur = aends[i];
                if (w != null) {
                    Z.set(2, w[icur]);
                }
                X.set(i, sstates.zcomponent(icur, Z));
                EX.set(i, Math.sqrt(Math.max(0, sstates.zvariance(icur, Z))));
            }
            output_.put(freq, new TsData[]{X, EX});
            return true;
        }

        private boolean fastFullProcess() {
            // actual start/end for computation
            Day start = data_.get(0).start, end = data_.get(data_.size() - 1).end;
            if (start_.isBefore(start)) {
                start = start_;
            }
            if (end_.isAfter(end)) {
                end = end_;
            }
            // creates the data.
            double[] data = new double[end.difference(start) + 1];
            double[] w;
            if (dweights_ != null) {
                w = new double[data.length];
                int j = start.getDayOfWeek().intValue();
                for (int i = 0; i < w.length; ++i) {
                    w[i] = dweights_[j];
                    if (++j == 7) {
                        j = 0;
                    }
                }
            } else {
                w = null;
            }
            for (int i = 0; i < data.length; ++i) {
                data[i] = Double.NaN;
            }
            int[] starts = new int[data_.size()];
            int idx = 0;
            for (PeriodObs obs : data_) {
                starts[idx++] = obs.start.difference(start);
                int n = obs.end.difference(start);
                data[n] = obs.value;
            }

            Smoother smoother = new Smoother();
            smoother.setCalcVar(true);
            smoother.setSsf(new SsfCalendarization(starts, w));
            SmoothingResults sstates = new SmoothingResults(true, true);
            smoother.process(new SsfData(data, null), sstates);
            double[] c = sstates.component(1);
            double[] e = sstates.componentStdev(1);

            if (w != null) {
                for (int i = 0; i < c.length; ++i) {
                    c[i] *= w[i];
                    e[i] *= w[i];
                }
            }
            s_ = c;
            es_ = e;
            return true;
        }

        private TsData makeTsData(TsFrequency freq) {
            Day start = data_.get(0).start, end = data_.get(data_.size() - 1).end;
            if (start_.isBefore(start)) {
                start = start_;
            }
            if (end_.isAfter(end)) {
                end = end_;
            }
            TsPeriod S = new TsPeriod(freq, start);
            TsPeriod E = new TsPeriod(freq, end);
            double[] sum = new double[E.minus(S) + 1];
            TsPeriod cur = S.clone();
            for (int i = 0, j0 = 0, j1 = 0; i < sum.length; ++i) {
                j1 = Math.min(cur.lastday().difference(start) + 1, s_.length);
                double s = 0;
                for (int j = j0; j < j1; ++j) {
                    s += s_[j];
                }
                sum[i] = s;
                cur.move(1);
                j0 = j1;
            }

            return new TsData(S, sum, false);
        }

        public Day getStart() {
            return start_;
        }

        public Day getEnd() {
            return end_;
        }

        public double[] getSmoothedData() {
            if (!process(TsFrequency.Undefined)) {
                return null;
            }
            return s_;
        }

        public TsData getAggregates(TsFrequency freq) {
            if (!process(freq)) {
                return null;
            }
            if (!stdev_) {
                return makeTsData(freq);
            } else {
                TsData[] o = output_.get(freq);
                return o == null ? null : o[0];
            }
        }

        public double[] getSmoothedStdev() {
            if (!stdev_ || !process(TsFrequency.Undefined)) {
                return null;
            }
            return es_;
        }

        public TsData getAggregatesStdev(TsFrequency freq) {
            if (!stdev_ || !process(freq)) {
                return null;
            }
            TsData[] o = output_.get(freq);
            return o == null ? null : o[1];
        }
    }
}