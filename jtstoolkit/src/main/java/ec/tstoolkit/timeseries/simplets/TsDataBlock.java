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

package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.timeseries.TsPeriodSelector;

/**
 * A TsDataBlock represents a block of equally spaced observations in a time series.
 * Users should use TsDataBlock for going through a time series in an efficient way.
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class TsDataBlock {

    /**
     * Creates a data block that contains the complete years of the time series.
     * 
     * @param series The series on which the data block is created.
     * @return
     */
    public static TsDataBlock fullYears(TsData series) {
        TsPeriod start = series.getStart(), end = series.getEnd();
        int freq = series.getFrequency().intValue();
        int nbeg = start.getPosition();
        if (nbeg != 0) {
            nbeg = freq - nbeg;
        }
        int nend = end.getPosition();
        return new TsDataBlock(series, nbeg, series.getLength() - nend, 1);
    }

    /**
     * Creates a data block corresponding to a given time domain. The series
     * must contain that domain.
     * @param series The series on which the data block is created.
     * @param domain The domain of the data block.
     * @return The corresponding data block. Null is returned if the domain of
     * the series doesn't contain the given domain.
     */
    public static TsDataBlock select(TsData series, TsDomain domain) {
        if (!series.getDomain().contains(domain)) {
            return null;
        }
        TsPeriod start = series.getStart(), dstart = domain.getStart();
        int istart = dstart.minus(start);
        return new TsDataBlock(series, istart, istart + domain.getLength(), 1);
    }

    /**
     * Creates a data block corresponding to a given selection.
     * @param series The series on which the data block is created.
     * @param selector The domain selector.
     * @return The corresponding data block. Null is returned if the no
     * data are selected.
     */
    public static TsDataBlock select(TsData series, TsPeriodSelector selector) {
        TsDomain domain = series.getDomain().select(selector);
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        TsPeriod start = series.getStart(), dstart = domain.getStart();
        int istart = dstart.minus(start);
        return new TsDataBlock(series, istart, istart + domain.getLength(), 1);
    }

    /**
     * Makes an extract in a given data block.
     * @param data The data block
     * @param start The position (0-based) of the first observation of the selection.
     * @param count The number of data of the selection.
     * @param inc the increment of the selection
     * @return The new data block. This method doesn't check possible a overrun.
     *
     */
    public static TsDataBlock select(TsDataBlock data, int start, int count,
            int inc) {
        TsPeriod pstart = data.start.plus(start * data.data.getIncrement());
        return new TsDataBlock(pstart, data.data.extract(start, count, inc));
    }

    /**
     * Creates a data block that contains the complete series
     * 
     * @param series
     *            The series on which a data block is created
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
    public static TsDataBlock all(TsData series) {
        return new TsDataBlock(series.getStart(), new DataBlock(series.internalStorage()));
    }
    /**
     * Data of this object
     */
    public final DataBlock data;
    /**
     * Period corresponding to the first data
     */
    public final TsPeriod start;

    /**
     * Creates an iterator on the time observations (pairs of (period, data)).
     * 
     * @return The new iterator. As usual, a first call to nextElement() (after
     *         a previous call to hasMoreElements()) is needed to access the
     *         first observation.
     */
    public java.util.Enumeration<TsObservation> observations() {
        return new TsDataIterator(this);
    }

    /**
     * Creates the period corresponding to the i-th period
     * 
     * @param i Position of the period.
     * @return A new object is returned.
     */
    @NewObject
    public TsPeriod period(int i) {
        return start.plus(data.getIncrement() * i);
    }

    /**
     * Creates a partial data block on a series
     * 
     * @param series
     *            The series that contains the data.
     * @param start
     *            The starting position of the data block (included).
     * @param end
     *            The ending position of the data block (excluded).
     * @param inc
     *            The increment between to consecutive data in the data block
     */
    TsDataBlock(TsData series, int start, int end, int inc) {
        data = new DataBlock(series.internalStorage(), start, end,
                inc);
        this.start = series.getStart().plus(start);
    }

    TsDataBlock(TsPeriod start, DataBlock data) {
        this.data = data;
        this.start = start;
    }

    TsDataBlock drop(int nbeg, int nend) {
        return new TsDataBlock(start.plus(nbeg * data.getIncrement()), data.drop(nbeg, nend));
    }

    TsDataBlock extend(int nbeg, int nend) {
        return new TsDataBlock(start.minus(nbeg * data.getIncrement()), data.extend(nbeg, nend));
    }

    void move(int m) {
        if (m == 0) {
            return;
        }
        start.move(m);
        data.slide(m);
    }
}
