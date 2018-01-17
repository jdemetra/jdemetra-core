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
package internal.timeseries;

import demetra.data.AggregationType;
import demetra.data.DoubleSequence;

/**
 *
 * @author Philippe Charles
 */
@FunctionalInterface
public interface InternalAggregator {

    double aggregate(DoubleSequence values, int start, int end);

    static InternalAggregator of(AggregationType type) {
        switch (type) {
            case Average:
                return InternalAggregator::average;
            case First:
                return InternalAggregator::first;
            case Last:
                return InternalAggregator::last;
            case Max:
                return InternalAggregator::max;
            case Min:
                return InternalAggregator::min;
            case None:
                return InternalAggregator::none;
            case Sum:
                return InternalAggregator::sum;
            default:
                throw new RuntimeException();
        }
    }

    static double none(DoubleSequence values, int start, int end) {
        return Double.NaN;
    }

    static double sum(DoubleSequence values, int start, int end) {
        double sum = 0;
        for (int i = start; i < end; i++) {
            double val = values.get(i);
            if (Double.isFinite(val)) {
                sum += val;
            }
        }
        return sum;
    }

    static double average(DoubleSequence values, int start, int end) {
        double sum = 0;
        double count = 0;
        for (int i = start; i < end; i++) {
            double val = values.get(i);
            if (Double.isFinite(val)) {
                sum += val;
                count++;
            }
        }
        return count != 0 ? sum / count : Double.NaN;
    }

    static double first(DoubleSequence values, int start, int end) {
        for (int i = start; i < end; i++) {
            double val = values.get(i);
            if (Double.isFinite(val)) {
                return val;
            }
        }
        return Double.NaN;
    }

    static double last(DoubleSequence values, int start, int end) {
        double last = Double.NaN;
        for (int i = start; i < end; i++) {
            double val = values.get(i);
            if (Double.isFinite(val)) {
                last = val;
            }
        }
        return last;
    }

    static double min(DoubleSequence values, int start, int end) {
        double min = Double.MAX_VALUE;
        for (int i = start; i < end; i++) {
            double val = values.get(i);
            if (Double.isFinite(val)) {
                if (val < min) {
                    min = val;
                }
            }
        }
        return min != Double.MAX_VALUE ? min : Double.NaN;
    }

    static double max(DoubleSequence values, int start, int end) {
        double max = Double.MIN_VALUE;
        for (int i = start; i < end; i++) {
            double val = values.get(i);
            if (Double.isFinite(val)) {
                if (val > max) {
                    max = val;
                }
            }
        }
        return max != Double.MIN_VALUE ? max : Double.NaN;
    }
}
