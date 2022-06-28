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
 * Iterator that walk through a time series year by year. Years can be
 * incomplete
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class YearIterator implements java.util.Enumeration<TsDataView> {

    /**
     *
     * @param series
     * @return
     */
    public static YearIterator fullYears(TsData series) {
        TsDomain domain = series.getDomain();
        int ifreq = domain.getAnnualFrequency();
        int nbeg = series.getStart().annualPosition();
        int nend = series.getEnd().annualPosition();
        domain = domain.drop(nbeg == 0 ? 0 : ifreq - nbeg, nend);
        return new YearIterator(series, domain);
    }

    private TsDataView m_cur;
    private final TsDataView m_data;

    /**
     *
     * @param series
     */
    public YearIterator(TsData series) {
        if (!series.isEmpty()) {
            m_data = TsDataView.all(series);
            initialize();
        } else {
            m_data = null;
        }
    }

    /**
     *
     * @param series
     * @param domain
     */
    public YearIterator(TsData series, TsDomain domain) {
        m_data = TsDataView.select(series, domain);
        initialize();
    }

    /**
     *
     * @param series
     * @param selector
     */
    public YearIterator(TsData series, TimeSelector selector) {
        m_data = TsDataView.select(series, selector);
        initialize();
    }

    @Override
    public boolean hasMoreElements() {
        if (m_data == null) {
            return false;
        }
        return m_cur != null;
    }

    private void initialize() {
        int ifreq = m_data.getStart().annualFrequency();
        int beg = m_data.getStart().annualPosition();
        int end = ifreq;
        int n = m_data.getData().length();
        if (end > n) {
            end = n;
        }
        m_cur = new TsDataView(m_data.getStart(), m_data.getData().range(0, end-beg), 1);
    }

    @Override
    public TsDataView nextElement() {
        TsDataView view = m_cur;
        if (view == null) {
            return null;
        }
        TsPeriod start = m_data.getStart(), cstart = view.getStart();
        int n = m_data.getData().length();
        int ifreq = m_data.getStart().annualFrequency();
        int cn= view.getData().length();
        int cbeg = start.until(cstart) + cn;
        if (cbeg >= n) {
            m_cur = null;
        } else {
            cstart = start.plus(cbeg);
            int cend = cbeg + ifreq;
            if (cend > n) {
                cend = n;
            }
            m_cur = new TsDataView(cstart, m_data.getData().range(cbeg, cend), 1);
        }
        return view;
    }

    /**
     *
     */
    public void reset() {
        initialize();
    }
}
