/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries.simplets;

import demetra.data.AggregationType;
import demetra.data.DoubleSequence;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsDataConverter {

    /**
     * Makes a frequency change of this series.
     *
     * @param s
     * @param newUnit The new frequency. Must be la divisor of the present
     * frequency.
     * @param conversion Aggregation mode.
     * @param complete If true, the observation for a given period in the new
     * series is set to Missing if some data in the original series are Missing.
     * @return A new time series is returned.
     */
    public TsData changeTsUnit(TsData s, TsUnit newUnit, AggregationType conversion, boolean complete) {
        int ratio = s.getUnit().ratio(newUnit);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                return null;
            case 1:
                return TsData.of(s.getStart().withUnit(newUnit), s.values());
        }
        if (s.isEmpty()) {
            return TsData.of(s.getStart().withUnit(newUnit), s.values());
        }
        return changeUsingRatio(s, newUnit, Aggregator.of(conversion), ratio, complete);
    }

    private TsData changeUsingRatio(TsData s, TsUnit newUnit, Aggregator aggregator, int ratio, boolean complete) {
        int oldLength = s.length();

        int tail = s.domain().getEndPeriod().getPosition(newUnit);
        int head = (oldLength - tail) % ratio;
        int body = oldLength - head - tail;

        TsPeriod newStart = s.getStart().withUnit(newUnit).plus(complete && head > 0 ? 1 : 0);
        DoubleSequence newValues = aggregate(s.values(), aggregator, complete, ratio, head, body, tail);
        return TsData.of(newStart, newValues);
    }

    private DoubleSequence aggregate(DoubleSequence values, Aggregator aggregator, boolean complete, int ratio, int head, int body, int tail) {
        boolean appendHead = !complete && head > 0;
        boolean appendTail = !complete && tail > 0;

        int length = body / ratio + (appendHead ? 1 : 0) + (appendTail ? 1 : 0);

        double[] result = new double[length];
        int i = 0;

        // head
        if (appendHead) {
            result[i++] = aggregator.aggregate(values, 0, head);
        }
        // body
        int tailIndex = body + head;
        for (int j = head; j < tailIndex; j += ratio) {
            result[i++] = aggregator.aggregate(values, j, j + ratio);
        }
        // tail
        if (appendTail) {
            result[i++] = aggregator.aggregate(values, tailIndex, tailIndex + tail);
        }

        return DoubleSequence.ofInternal(result);
    }

    private interface Aggregator {

        double aggregate(DoubleSequence values, int start, int end);

        static Aggregator of(AggregationType type) {
            switch (type) {
                case Average:
                    return Aggregator::average;
                case First:
                    return Aggregator::first;
                case Last:
                    return Aggregator::last;
                case Max:
                    return Aggregator::max;
                case Min:
                    return Aggregator::min;
                case None:
                    return Aggregator::none;
                case Sum:
                    return Aggregator::sum;
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
}
