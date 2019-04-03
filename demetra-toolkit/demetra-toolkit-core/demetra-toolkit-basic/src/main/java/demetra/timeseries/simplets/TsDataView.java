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
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsException;
import demetra.timeseries.TsUnit;
import demetra.data.DoubleSeq;

/**
 * A TsDataView is a view on equally spaced observations in a time series. Users
 * should use TsDataView for going through a time series in an efficient way.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class TsDataView {

    private final DoubleSeq data;
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
        if (start.getEpoch() != TsPeriod.DEFAULT_EPOCH)
            throw new TsException(TsException.INVALID_OPERATION);
        int period=domain.getAnnualFrequency();
        if (period<0)
            throw new TsException(TsException.INVALID_OPERATION);
        TsPeriod nstart=start.withUnit(TsUnit.YEAR);
        int nbeg = TsDomain.splitOf(nstart, start.getUnit(), true).indexOf(start);
        if (nbeg != 0) {
            nbeg = period - nbeg;
        }
        int nend = TsDomain.splitOf(end.withUnit(TsUnit.YEAR), end.getUnit(), true).indexOf(end);
        int len = series.length() - nend - nbeg;
        final int beg = nbeg;
        return new TsDataView(start.plus(nbeg), DoubleSeq.onMapping(len, i -> series.getValue(beg + i)), 1);
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
        return new TsDataView(dstart, DoubleSeq.onMapping(domain.length(), i -> series.getValue(del + i)), 1);
    }

    /**
     * Creates a data block corresponding to a given selection.
     *
     * @param series The series on which the data block is created.
     * @param selector The domain selector.
     * @return The corresponding data block. Null is returned if the no data are
     * selected.
     */
    public static TsDataView select(TsData series, TimeSelector selector) {
        TsDomain domain = series.getDomain().select(selector);
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        TsPeriod start = series.getStart(), dstart = domain.getStartPeriod();
        int del = start.until(dstart);
        return new TsDataView(dstart, DoubleSeq.onMapping(domain.length(), i -> series.getValue(del + i)), 1);
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

    TsDataView(TsPeriod start, DoubleSeq data, int inc) {
        this.start = start;
        this.data = data;
        this.inc = inc;
    }

    /**
     * @return the data
     */
    public DoubleSeq getData() {
        return data;
    }

    /**
     * @return the start
     */
    public TsPeriod getStart() {
        return start;
    }

}
