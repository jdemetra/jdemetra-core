/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package internal.timeseries.util;

import nbbrd.design.VisibleForTesting;
import demetra.timeseries.TsUnit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import demetra.data.Doubles;

/**
 *
 * @author Philippe Charles
 */
interface ByLongObsList extends ObsList {

    void clear();

    void add(long period, double value);

    interface ToPeriodIdFunc {

        int apply(TsUnit unit, LocalDateTime reference, long value);
    }

    static final class Sortable implements ByLongObsList {

        private final ToPeriodIdFunc tsPeriodIdFunc;
        private final List<LongObs> list;
        private boolean sorted;
        private long latestPeriod;

        Sortable(ToPeriodIdFunc tsPeriodIdFunc) {
            this.tsPeriodIdFunc = tsPeriodIdFunc;
            this.list = new ArrayList<>();
            this.sorted = true;
            this.latestPeriod = Long.MIN_VALUE;
        }

        @VisibleForTesting
        boolean isSorted() {
            return sorted;
        }

        @Override
        public void clear() {
            list.clear();
            sorted = true;
            latestPeriod = Long.MIN_VALUE;
        }

        @Override
        public void add(long period, double value) {
            list.add(new LongObs(period, value));
            sorted = sorted && latestPeriod <= period;
            latestPeriod = period;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public double getValue(int index) {
            return list.get(index).value;
        }

        @Override
        public IntUnaryOperator getPeriodIdFunc(TsUnit unit, LocalDateTime reference) {
            return o -> tsPeriodIdFunc.apply(unit, reference, list.get(o).period);
        }

        @Override
        public void sortByPeriod() {
            if (!sorted) {
                list.sort((l, r) -> Long.compare(l.period, r.period));
                sorted = true;
                latestPeriod = list.get(list.size() - 1).period;
            }
        }

        @lombok.AllArgsConstructor
        private static final class LongObs {

            final long period;
            final double value;
        }
    }

    static final class PreSorted implements ByLongObsList {

        private final ToPeriodIdFunc tsPeriodIdFunc;
        private long[] periods;
        private double[] values;
        private int size;

        PreSorted(ToPeriodIdFunc tsPeriodIdFunc, int initialCapacity) {
            this.tsPeriodIdFunc = tsPeriodIdFunc;
            this.periods = new long[initialCapacity];
            this.values = new double[initialCapacity];
            this.size = 0;
        }

        private void grow() {
            int oldCapacity = periods.length;
            int newCapacity = Math.min(oldCapacity * 2, Integer.MAX_VALUE);
            periods = Arrays.copyOf(periods, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }

        @Override
        public void clear() {
            size = 0;
        }

        @Override
        public void add(long period, double value) {
            if (size + 1 == periods.length) {
                grow();
            }
            periods[size] = period;
            values[size] = value;
            size++;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public double getValue(int index) {
            return values[index];
        }

        @Override
        public IntUnaryOperator getPeriodIdFunc(TsUnit unit, LocalDateTime reference) {
            return o -> tsPeriodIdFunc.apply(unit, reference, periods[o]);
        }

        @Override
        public void sortByPeriod() {
            // do nothing
        }

        @Override
        public Doubles getValues() {
            return Doubles.ofInternal(Arrays.copyOf(values, size));
        }
    }
}
