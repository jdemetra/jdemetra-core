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
package internal.tsprovider.util;

import demetra.data.DoubleSequence;
import demetra.design.VisibleForTesting;
import demetra.timeseries.TsUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntUnaryOperator;

/**
 *
 * @author Philippe Charles
 */
interface ByObjObsList<T> extends ObsList {

    void clear();

    void add(T period, double value);

    interface ToPeriodIdFunc<T> {

        int apply(TsUnit unit, int offset, T value);
    }

    static final class PreSorted<T> implements ByObjObsList<T> {

        private final ToPeriodIdFunc<T> tsPeriodIdFunc;
        private Object[] periods;
        private double[] values;
        private int size;

        PreSorted(ToPeriodIdFunc<T> tsPeriodIdFunc, int initialCapacity) {
            this.tsPeriodIdFunc = tsPeriodIdFunc;
            this.periods = new Object[initialCapacity];
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
        public void add(T period, double value) {
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
        public void sortByPeriod() {
            // do nothing
        }

        @Override
        public IntUnaryOperator getPeriodIdFunc(TsUnit unit, int offset) {
            return o -> tsPeriodIdFunc.apply(unit, offset, (T) periods[o]);
        }

        @Override
        public double getValue(int index) throws IndexOutOfBoundsException {
            return values[index];
        }

        @Override
        public DoubleSequence getValues() {
            return DoubleSequence.ofInternal(Arrays.copyOf(values, size));
        }
    }

    static final class Sortable<T> implements ByObjObsList<T> {

        private final ToPeriodIdFunc<T> tsPeriodIdFunc;
        private final Comparator<T> comparator;
        private final List<Obs<T>> list;
        private boolean sorted;
        private T latestPeriod;

        Sortable(ToPeriodIdFunc<T> tsPeriodIdFunc, Comparator<T> comparator) {
            this.tsPeriodIdFunc = tsPeriodIdFunc;
            this.comparator = comparator;
            this.list = new ArrayList<>();
            this.sorted = true;
            this.latestPeriod = null;
        }

        @VisibleForTesting
        boolean isSorted() {
            return sorted;
        }

        @Override
        public void clear() {
            list.clear();
            sorted = true;
            latestPeriod = null;
        }

        @Override
        public void add(T period, double value) {
            list.add(new Obs(period, value));
            sorted = sorted && (latestPeriod == null || comparator.compare(latestPeriod, period) <= 0);
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
        public IntUnaryOperator getPeriodIdFunc(TsUnit unit, int offset) {
            return o -> tsPeriodIdFunc.apply(unit, offset, list.get(o).period);
        }

        @Override
        public void sortByPeriod() {
            if (!sorted) {
                list.sort(Comparator.comparing(o -> o.period, comparator));
                sorted = true;
                latestPeriod = list.get(list.size() - 1).period;
            }
        }

        @lombok.AllArgsConstructor
        private static final class Obs<T> {

            final T period;
            final double value;
        }
    }
}
