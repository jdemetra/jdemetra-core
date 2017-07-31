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
package demetra.timeseries.simplets;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.timeseries.ITimeSeries;
import java.util.Random;

/**
 * A TsData is a raw time series, containing only the actual data.
 * TsData can only handle regular time series, with observations
 corresponding to the usual time decomposition of an year (frequency lower or
 equal to the monthly frequency). Observations are represented by double
 values. Missing values are allowed; they are represented by Double.NaN.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
@lombok.EqualsAndHashCode
public final class TsData implements ITimeSeries.OfDouble<TsPeriod, TsObservation> {

    /**
     * Creates a random time series
     *
     * @param freq The frequency of the series.
     * @param seed
     * @return A time series with a random length (<600 observations), a random
     * starting period (between 1970 and 1990) and random observations is
     * generated
     */
    public static TsData random(TsFrequency freq, int seed) {
        Random rnd = new Random(seed);
        int beg = rnd.nextInt(240);
        int count = rnd.nextInt(600);
        double[] data = new double[count];
        double cur = rnd.nextDouble() + 100;
        for (int i = 0; i < count; ++i) {
            cur = cur + rnd.nextDouble() - .5;
            data[i] = cur;
        }
        return new TsData(new TsPeriod(freq, beg), DoubleSequence.ofInternal(data));
    }

    /**
     * Creates a new time series from a copy of this sequence of doubles
     * @param start
     * @param values
     * @return 
     */
    public static TsData of(TsPeriod start, DoubleSequence values) {
        return new TsData(start, DoubleSequence.ofInternal(values.toArray()));
    }

    public static TsData ofInternal(TsPeriod start, DoubleSequence values) {
        return new TsData(start, values);
    }

    public static TsData ofInternal(TsPeriod start, double[] values) {
        return new TsData(start, DoubleSequence.ofInternal(values));
    }

    private final TsPeriod start;
    private final DoubleSequence values;

    private TsData(TsPeriod start, DoubleSequence values) {
        this.start = start;
        this.values = values;
    }

    @Override
    public TsDomain domain() {
        return TsDomain.of(start, values.length());
    }

    @Override
    public DoubleSequence values() {
        return values;
    }

    @Override
    public TsObservation get(int index) throws IndexOutOfBoundsException {
        return new TsObservation(getPeriod(index), getValue(index));
    }

    /**
     * Gets the frequency of the series.
     *
     * @return The frequency.
     */
    public TsFrequency getFrequency() {
        return start.getFrequency();
    }

    public TsPeriod getStart() {
        return start;
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
        int pos = period.minus(start);
        return (pos < 0 || pos >= values.length()) ? Double.NaN : values.get(pos);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int n=values.length();
        DoubleReader reader = values.reader();
        for (int i = 0; i < values.length(); ++i) {
            builder.append(start.plus(i)).append('\t').append(reader.next());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

}
