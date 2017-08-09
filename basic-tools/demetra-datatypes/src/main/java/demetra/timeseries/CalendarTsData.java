/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries;

import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import java.time.Period;
import java.util.Random;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class CalendarTsData implements ITimeSeries.OfDouble<IDatePeriod, DateObservation> {

    /**
     * Creates a random time series
     *
     * @param domain
     * @param seed
     * @return A time series with a random length (<600 observations), a random
     * starting period (between 1970 and 1990) and random observations is
     * generated
     */
    public static CalendarTsData random(IDateDomain domain, int seed) {
        Random rnd = new Random(seed);
        int beg = rnd.nextInt(240);
        int count = domain.length();
        double[] data = new double[count];
        double cur = rnd.nextDouble() + 100;
        for (int i = 0; i < count; ++i) {
            cur = cur + rnd.nextDouble() - .5;
            data[i] = cur;
        }
        return new CalendarTsData(domain, DoubleSequence.ofInternal(data));
    }

    /**
     * Creates a new time series from a copy of this sequence of doubles
     *
     * @param domain
     * @param values
     * @return
     */
    public static CalendarTsData of(IDateDomain domain, DoubleSequence values) {
        return new CalendarTsData(domain, DoubleSequence.ofInternal(values.toArray()));
    }

    public static CalendarTsData ofInternal(IDateDomain domain, DoubleSequence values) {
        return new CalendarTsData(domain, values);
    }

    public static CalendarTsData ofInternal(IDateDomain domain, double[] values) {
        return new CalendarTsData(domain, DoubleSequence.ofInternal(values));
    }

    private final IDateDomain domain;
    private final DoubleSequence values;

    private CalendarTsData(IDateDomain domain, DoubleSequence values) {
        this.domain = domain;
        this.values = values;
    }

    @Override
    public IDateDomain domain() {
        return domain;
    }

    @Override
    public DoubleSequence values() {
        return values;
    }

    @Override
    public DateObservation get(int index) throws IndexOutOfBoundsException {
        return new DateObservation(getPeriod(index), getValue(index));
    }

    /**
     * Gets the frequency of the series.
     *
     * @return The frequency.
     */
    public Period getPeriod() {
        return domain.getPeriod();
    }

    /**
     * *
     * Gets the data corresponding to a given period. The period should have the
     * same frequency of the time series, otherwise an exception will be
     * thrown.
     *
     * @param period The considered period.
     * @return The corresponding data or Nan if the period doesn't belong to
     * this time series
     */
    public double getDoubleValue(IDatePeriod period) {
        int pos = domain.search(period.firstDay());
        if (pos < 0 || pos >= length()) {
            return Double.NaN;
        }
        DateObservation obs = get(pos);
        if (obs.getPeriod().equals(period)) {
            return obs.getValue();
        } else {
            return Double.NaN;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        DoubleReader reader = values.reader();
        for (int i = 0; i < values.length(); ++i) {
            builder.append(domain.get(i)).append('\t').append(reader.next());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}
