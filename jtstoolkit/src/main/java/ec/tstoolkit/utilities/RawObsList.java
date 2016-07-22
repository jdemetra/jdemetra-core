/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.utilities;

import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
public interface RawObsList {

    void clear();

    int size();

    void add(long period, double value);

    long getPeriod(int index) throws IndexOutOfBoundsException;

    double getValue(int index) throws IndexOutOfBoundsException;

    void sortByPeriod();

    @NewObject
    default double[] getValues() {
        double[] result = new double[size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getValue(i);
        }
        return result;
    }

    @NewObject
    public static RawObsList fromArrayList() {
        return new RawObsListImpl();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static final class RawObsListImpl implements RawObsList {

        private final List<Obs> list = new ArrayList<>();
        private boolean sorted = true;
        private long latestPeriod = Long.MIN_VALUE;

        @VisibleForTesting
        public boolean isSorted() {
            return sorted;
        }

        @Override
        public void clear() {
            list.clear();
            sorted = true;
            latestPeriod = Long.MIN_VALUE;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public void add(long period, double value) {
            list.add(new Obs(period, value));
            sorted = sorted && latestPeriod <= period;
            latestPeriod = period;
        }

        @Override
        public long getPeriod(int index) {
            return list.get(index).period;
        }

        @Override
        public double getValue(int index) {
            return list.get(index).value;
        }

        @Override
        public void sortByPeriod() {
            if (!sorted) {
                list.sort((l, r) -> Long.compare(l.period, r.period));
                sorted = true;
                latestPeriod = getPeriod(size() - 1);
            }
        }

        private static final class Obs {

            final long period;
            final double value;

            private Obs(long period, double value) {
                this.period = period;
                this.value = value;
            }
        }
    }
    //</editor-fold>
}
