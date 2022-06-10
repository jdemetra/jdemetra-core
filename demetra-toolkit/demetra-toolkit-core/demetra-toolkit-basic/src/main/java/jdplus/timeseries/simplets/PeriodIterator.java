/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.timeseries.simplets;

import demetra.timeseries.TimeSelector;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
/**
 * Iterator of a time series following the different periods.
 * In the case of a monthly series, the iterator will return successively
 * (in TsDataBlocks) all the Januaries, Februaries...As usual, the use of the
 * iterator should contain code like
 * while (iterator.hasMoreElements()){
 * TsDataBlock data=iterator.nextElement();
 *    // do something
 * }
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class PeriodIterator implements java.util.Enumeration<TsDataView> {

    /**
     * Returns an iterator considering only complete years
     *
     * @param series The analyzed series
     * @return An iterator with the different periods.
     */
    public static PeriodIterator fullYears(TsData series) {
        TsDomain domain = series.getDomain();
        int ifreq = domain.getAnnualFrequency();
        int nbeg = series.getStart().annualPosition();
        int nend = series.getEnd().annualPosition();
        domain = domain.drop(nbeg == 0 ? 0 : ifreq - nbeg, nend);
        return new PeriodIterator(series, domain);
    }

    private final TsDataView data_;
    private TsDataView cur_;

    /**
     * Creates a new period iterator on the complete series. The data blocks
     * returned by the iterator may have different lengths.
     *
     * @param series The time series.
     */
    public PeriodIterator(TsData series) {
        if (!series.isEmpty()) {
            data_ = TsDataView.all(series);
            initialize();
        } else {
            data_ = null;
        }

    }

    /**
     * Creates a partial iterator, limited on a specified domain.
     *
     * @param series The considered time series.
     * @param domain The domain of the iterator. Must be compatible with the
     * time series
     */
    public PeriodIterator(TsData series, TsDomain domain) {
        data_ = TsDataView.select(series, domain);
    }

    /**
     * Creates a partial iterator, limited by a period selection
     *
     * @param series The considered time series
     * @param selector The selector.
     */
    public PeriodIterator(TsData series, TimeSelector selector) {
        data_ = TsDataView.select(series, selector);
    }

    @Override
    public boolean hasMoreElements() {
        if (data_ == null) {
            return false;
        }
        return cur_ != null;
    }

    @Override
    public TsDataView nextElement() {
        TsDataView view = cur_;
        if (view == null) {
            return null;
        }
        int freq = data_.getStart().annualFrequency();
        int pstart = view.getStart().annualPosition();
        if (++pstart == freq) {
            cur_ = null;
        } else {
            TsPeriod nstart = view.getStart().next();
            int del = data_.getStart().until(nstart);
            if (del >= freq) {
                nstart = nstart.plus(-freq);
                del = del - freq;
            }
            int nyears = 1 + (data_.getData().length() - del - 1) / freq;
            cur_ = new TsDataView(nstart, data_.getData().extract(del, nyears, freq), freq);
        }
        return view;
    }

    /**
     * Restarts the iterator.
     */
    public void reset() {
        initialize();
    }

    private void initialize() {
        int freq = data_.getStart().annualFrequency();
        int istart = data_.getStart().annualPosition();
        if (istart != 0) {
            istart = freq - istart;
        }
        int nyears = 1 + (data_.getData().length() - istart - 1) / freq;
        cur_ = new TsDataView(data_.getStart().plus(istart), data_.getData().extract(istart, nyears, freq), freq);
    }

}
