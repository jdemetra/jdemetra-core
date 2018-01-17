/*
* Copyright 2013 National Bank of Belgium
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

import demetra.timeseries.TsData;
import demetra.data.DoubleSequence;
import demetra.design.Development;
import demetra.design.Immutable;
import internal.timeseries.InternalFixme;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSeriesSelector;

/**
 * A TsDataView is a view on equally spaced observations in a time series. Users
 * should use TsDataView for going through a time series in an efficient way.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class TsDataView {

    private final DoubleSequence data;
    private final int inc;
    private final TsPeriod start;

    /**
     * Creates a data block that contains the complete years of the time series.
     *
     * @param series The series on which the data block is created.
     * @return
     */
    public static TsDataView fullYears(TsData series) {
        TsDomain domain = series.getDomain();
        TsPeriod start = domain.getStartPeriod(), end = domain.getEndPeriod();
        int freq = InternalFixme.getAsInt(domain.getStartPeriod().getUnit());
        int nbeg = InternalFixme.getPosition(start);
        if (nbeg != 0) {
            nbeg = freq - nbeg;
        }
        int nend = InternalFixme.getPosition(end);
        int len = series.length() - nend - nbeg;
        final int beg = nbeg;
        return new TsDataView(start.plus(nbeg), DoubleSequence.of(len, i -> series.getValue(beg + i)), 1);
    }

    /**
     * Creates a data block corresponding to a given time domain. The series
     * must contain that domain.
     *
     * @param series The series on which the data block is created.
     * @param domain The domain of the data block.
     * @return The corresponding data block. Null is returned if the domain of
     * the series doesn't contain the given domain.
     */
    public static TsDataView select(TsData series, TsDomain domain) {
        if (!series.getDomain().contains(domain)) {
            return null;
        }
        TsPeriod start = series.getStart(), dstart = domain.getStartPeriod();
        int del = start.until(dstart);
        return new TsDataView(dstart, DoubleSequence.of(domain.length(), i -> series.getValue(del + i)), 1);
    }

    /**
     * Creates a data block corresponding to a given selection.
     *
     * @param series The series on which the data block is created.
     * @param selector The domain selector.
     * @return The corresponding data block. Null is returned if the no data are
     * selected.
     */
    public static TsDataView select(TsData series, TimeSeriesSelector selector) {
        TsDomain domain = series.getDomain().select(selector);
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        TsPeriod start = series.getStart(), dstart = domain.getStartPeriod();
        int del = start.until(dstart);
        return new TsDataView(dstart, DoubleSequence.of(domain.length(), i -> series.getValue(del + i)), 1);
    }

    /**
     * Creates a data block that contains the complete series
     *
     * @param series The series on which a data block is created
     */
    /*public TsDataBlock(TsData series) {
    data = new DataBlock(series.getValues().internalStorage(), 0, series
    .getLength(), 1);
    start = series.getStart();
    }*/
    /**
     * Creates a data block that contains the complete series
     *
     * @param series The series on which a data block is created
     * @return The new time data block
     */
    public static TsDataView all(TsData series) {
        return new TsDataView(series.getStart(), series.getValues(), 1);
    }

    /**
     * Creates the period corresponding to the i-th element
     *
     * @param i Position of the period.
     * @return
     */
    public TsPeriod period(int i) {
        return start.plus(inc * i);
    }

    TsDataView(TsPeriod start, DoubleSequence data, int inc) {
        this.start = start;
        this.data = data;
        this.inc = inc;
    }

    /**
     * @return the data
     */
    public DoubleSequence getData() {
        return data;
    }

    /**
     * @return the start
     */
    public TsPeriod getStart() {
        return start;
    }

}
