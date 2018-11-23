/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import demetra.data.AggregationType;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import internal.timeseries.InternalAggregator;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import lombok.AccessLevel;

/**
 * A TsData is a raw time series, containing only the actual data. TsData can
 * only handle regular time series, with observations corresponding to the usual
 * time decomposition of an year (frequency lower or equal to the monthly
 * frequency). Observations are represented by double values. Missing values are
 * allowed; they are represented by Double.NaN.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsData implements TimeSeriesData<TsPeriod, TsObs> {

    /**
     * Creates a random time series
     *
     * @param freq The frequency of the series.
     * @param seed
     * @return A time series with a random length (<600 observations), a random
     * starting period (between 1970 and 1990) and random observations is
     * generated
     */
    public static TsData random(TsUnit freq, int seed) {
        Random rnd = new Random(seed);
        int beg = rnd.nextInt(240);
        int count = rnd.nextInt(600);
        double[] data = new double[count];
        double cur = rnd.nextDouble() + 100;
        for (int i = 0; i < count; ++i) {
            cur = cur + rnd.nextDouble() - .5;
            data[i] = cur;
        }
        return ofInternal(TsPeriod.of(freq, beg), DoubleSequence.ofInternal(data));
    }

    /**
     * Creates a new time series from a copy of this sequence of doubles
     *
     * @param start
     * @param values
     * @return
     */
    @Nonnull
    public static TsData of(@Nonnull TsPeriod start, @Nonnull DoubleSequence values) {
        TsDomain domain = TsDomain.of(start, values.length());
        return domain.isEmpty()
                ? new TsData(domain, DoubleSequence.empty(), NO_DATA_CAUSE)
                : new TsData(domain, DoubleSequence.ofInternal(values.toArray()), null);
    }

    @Nonnull
    public static TsData ofInternal(@Nonnull TsPeriod start, @Nonnull DoubleSequence values) {
        TsDomain domain = TsDomain.of(start, values.length());
        return domain.isEmpty()
                ? new TsData(domain, DoubleSequence.empty(), NO_DATA_CAUSE)
                : new TsData(domain, values, null);
    }

    @Nonnull
    public static TsData ofInternal(@Nonnull TsPeriod start, @Nonnull double[] values) {
        TsDomain domain = TsDomain.of(start, values.length);
        return domain.isEmpty()
                ? new TsData(domain, DoubleSequence.empty(), NO_DATA_CAUSE)
                : new TsData(domain, DoubleSequence.ofInternal(values), null);
    }

    @Nonnull
    public static TsData empty(@Nonnull TsPeriod start, @Nonnull String cause) {
        return new TsData(TsDomain.of(start, 0), DoubleSequence.empty(), Objects.requireNonNull(cause));
    }

    @Nonnull
    public static TsData empty(@Nonnull String cause) {
        return new TsData(TsDomain.of(TsPeriod.of(TsUnit.YEAR, 0), 0), DoubleSequence.empty(), Objects.requireNonNull(cause));
    }

    private static final String NO_DATA_CAUSE = "No data available";

    private final TsDomain domain;
    private final DoubleSequence values;

    /**
     * Message explaining why the time series data is empty.
     */
    private final String cause;

    @Override
    public TsObs get(int index) throws IndexOutOfBoundsException {
        return TsObs.of(getPeriod(index), getValue(index));
    }

    /**
     * Gets the time unit of the series.
     *
     * @return The time unit.
     */
    public TsUnit getTsUnit() {
        return domain.getTsUnit();
    }

    public TsPeriod getStart() {
        return domain.getStartPeriod();
    }

    /**
     * Gets the number of periods in one year.
     *
     * @return The number of periods in 1 year or -1 if the unit is not compatible 
     * with years
     */
    public int getAnnualFrequency() {
        return domain.getTsUnit().getAnnualFrequency();
    }

    public boolean hasDefaultEpoch() {
        return domain.hasDefaultEpoch();
    }

    /**
     * *
     * Gets the data corresponding to a given period. The period should have the
     * same frequency of the time series, otherwise an exception will be thrown.
     *
     * @param period The considered period.
     * @return The corresponding data or Nan if the period doesn't belong to
     * this time series
     */
    public double getDoubleValue(TsPeriod period) {
        int pos = domain.indexOf(period);
        return (pos < 0 || pos >= values.length()) ? Double.NaN : values.get(pos);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Empty due to: '" + cause + "'";
        }
        StringBuilder builder = new StringBuilder();
        DoubleReader reader = values.reader();
        for (int i = 0; i < values.length(); ++i) {
            builder.append(domain.get(i).display()).append('\t').append(reader.next());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * Makes a frequency change of this series.
     *
     * @param newUnit The new frequency. Must be la divisor of the present
     * frequency.
     * @param conversion Aggregation mode.
     * @param complete If true, the observation for a given period in the new
     * series is set to Missing if some data in the original series are Missing.
     * @return A new time series is returned.
     */
    public TsData aggregate(@Nonnull TsUnit newUnit, @Nonnull AggregationType conversion, boolean complete) {
        int ratio = this.getTsUnit().ratioOf(newUnit);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                // FIXME: throw exception?
                return null;
            case 1:
                return TsData.of(this.getStart().withUnit(newUnit), this.getValues());
        }
        if (this.isEmpty()) {
            return TsData.of(this.getStart().withUnit(newUnit), this.getValues());
        }
        return changeUsingRatio(this, newUnit, InternalAggregator.of(conversion), ratio, complete);
    }

    private static TsData changeUsingRatio(TsData s, TsUnit newUnit, InternalAggregator aggregator, int ratio, boolean complete) {
        int oldLength = s.length();
        TsPeriod start = s.getStart(), nstart = start.withUnit(newUnit);
        int spos = TsDomain.splitOf(nstart, start.getUnit(), false).indexOf(start);
        int head = spos > 0 ? ratio - spos : 0;
        int tail = (oldLength - head) % ratio;
        int body = oldLength - head - tail;
        if (complete && head > 0) {
            nstart = nstart.next();
        }
        DoubleSequence newValues = aggregate(s.getValues(), aggregator, complete, ratio, head, body, tail);
        return TsData.of(nstart, newValues);
    }

    private static DoubleSequence aggregate(DoubleSequence values, InternalAggregator aggregator, boolean complete, int ratio, int head, int body, int tail) {
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
}
