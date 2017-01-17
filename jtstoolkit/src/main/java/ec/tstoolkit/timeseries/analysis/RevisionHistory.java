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
package ec.tstoolkit.timeseries.analysis;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataFunction;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class RevisionHistory<T extends IProcResults> {
    

    private final ITsProcessing<T> m_processing;

    private final HashMap<TsDomain, T> m_cache = new HashMap<>();

    private final TsDomain m_domainT;

    /**
     *
     * @param processing
     * @param domain
     */
    public RevisionHistory(ITsProcessing<T> processing,
            TsDomain domain) {
        m_processing = processing;
        m_domainT = domain;
        m_cache.put(m_domainT, processing.process(m_domainT));
    }

    /**
     *
     * @return
     */
    public ITsProcessing<T> getProcessing() {
        return m_processing;
    }

    /**
     *
     * @return
     */
    public TsDomain getReferenceDomain() {
        return m_domainT;
    }

    /**
     *
     * @return
     */
    public T getReferenceInfo() {
        return tsInfo(m_domainT);
    }

    // / <summary>
    // / Computes
    // / </summary>
    // / <param name="series"></param>
    // / <param name="period"></param>
    // / <param name="lag"></param>
    // / <param name="count"></param>
    // / <param name="mode"></param>
    // / <param name="target"></param>
    // / <returns></returns>
    /**
     *
     * @param series
     * @param period
     * @param lag
     * @param count
     * @param mode
     * @param target
     * @return
     */
    public double[] laggedSeriesRevision(String series, TsPeriod period,
            int lag, int count, DiagnosticInfo mode, DiagnosticTarget target) {
        TsPeriod start = m_domainT.getStart();
        TsDomain domain = new TsDomain(start, period.minus(start) + 1);
        IProcResults[] ilag = new IProcResults[count];
        TsDomain ldomain = domain;
        for (int i = 0; i < count; ++i) {
            ldomain = ldomain.extend(0, lag);
            ilag[i] = tsInfo(ldomain);
        }
        double[] rslt = new double[count];
        if (target == DiagnosticTarget.Final) {
            T iT = tsInfo(m_domainT);
            TsData Tdata = iT != null ? iT.getData(series, TsData.class) : null;
            if (Tdata == null) {
                return null;
            }
            for (int i = 0; i < count; ++i) {
                rslt[i] = Double.NaN;
                if (ilag[i] != null) {
                    TsData tdata = ilag[i].getData(series, TsData.class);
                    if (tdata != null) {
                        int idx = tdata.getLength() - 1;
                        double dt = tdata.get(idx), dT = Tdata.get(idx);
                        if (mode == DiagnosticInfo.RelativeDifference) {
                            rslt[i] = (dT - dt) / dt;
                        } else if (mode == DiagnosticInfo.AbsoluteDifference) {
                            rslt[i] = dT - dt;
                        } else // PtoP growth difference
                        {
                            double dt0 = tdata.get(idx - 1), dT0 = Tdata
                                    .get(idx - 1);
                            rslt[i] = (dT - dT0) / dT0 - (dt - dt0) / dt0;
                        }
                    }
                }
            }
        } else {
            T it = tsInfo(domain);
            TsData cdata = it != null ? it.getData(series, TsData.class) : null;
            if (cdata == null) {
                return null;
            }
            for (int i = 0; i < count; ++i) {
                rslt[i] = Double.NaN;
                if (ilag[i] != null) {
                    TsData tdata = ilag[i].getData(series, TsData.class);
                    if (tdata != null) {
                        int idx = tdata.getLength() - 1;
                        double dt = tdata.get(idx), dc = cdata.get(idx);
                        if (mode == DiagnosticInfo.RelativeDifference) {
                            rslt[i] = (dt - dc) / dc;
                        } else if (mode == DiagnosticInfo.AbsoluteDifference) {
                            rslt[i] = dt - dc;
                        } else // PtoP growth difference
                        {
                            double dt0 = tdata.get(idx - 1), dc0 = cdata
                                    .get(idx - 1);
                            rslt[i] = (dt - dt0) / dt0 - (dc - dc0) / dc0;
                        }
                    }
                }
            }
        }
        return rslt;
    }

    /**
     *
     * @param series
     * @return
     */
    public TsData referenceSeries(String series) {
        T it = tsInfo(m_domainT);
        return it == null ? null : it.getData(series, TsData.class);
    }

    /**
     *
     * @param item
     * @param start
     * @return
     */
    public TsData revision(String item, TsPeriod start) {
        TsPeriod p0 = m_domainT.getStart();
        TsData rslt = new TsData(start, m_domainT.getEnd().minus(start));
        int len = start.minus(p0) + 1;
        for (int i = 0; i < rslt.getLength(); ++i, ++len) {
            TsDomain rdom = new TsDomain(p0, len);
            T output = tsInfo(rdom);
            if (output != null) {
                Double d = output.getData(item, double.class);
                if (d != null) {
                    rslt.set(i, d);
                } else {
                    rslt.set(i, Double.NaN);
                }

            }
        }
        return rslt;
    }

    // / <summary>
    // / Returns a list with the first estimations of the ts identified by
    // "item",
    // / during a timespan defined by [beg, end].
    // / </summary>
    // / <param name="item"></param>
    // / <param name="start"></param>
    // / <param name="beg"></param>
    // / <returns></returns>
    /**
     *
     * @param item
     * @param beg
     * @param end
     * @return
     */
    @Deprecated
    public List<TsData> Select(String item, Date beg, Date end) {
        return select(item, beg, end);
    }

    public List<TsData> select(String item, Date beg, Date end) {
        ArrayList<TsData> s = new ArrayList<>();
        TsPeriod start = m_domainT.getStart();
        TsPeriod pbeg = new TsPeriod(start.getFrequency(), beg);
        TsPeriod pend = new TsPeriod(start.getFrequency(), end);
        if (pend.isAfter(m_domainT.getLast())) {
            pend = m_domainT.getLast();
        }
        int n = pend.minus(pbeg);
        if (n >= 0) {
            int len = pbeg.minus(start) + 1;
            for (int i = 0; i <= n; ++i) {
                try {
                    TsDomain dom = new TsDomain(start, len++);
                    T output = tsInfo(dom);
                    if (output != null) {
                        TsData q = output.getData(item, TsData.class);
                        if (q != null) {
                            s.add(q);
                        }
                    }
                } catch (Exception err) {
                }
            }
        }
        return s;
    }

    /**
     *
     * @param series
     * @param period
     * @return
     */
    public TsData series(String series, TsPeriod period) {
        TsPeriod start = m_domainT.getStart();
        TsDomain domain = new TsDomain(start, period.minus(start) + 1);
        T it = tsInfo(domain);
        if (it == null) {
            return null;
        }
        return it.getData(series, TsData.class);
    }

    // / <summary>
    // / Compute [A(t|T) - A(t|t)] / A(t|t) or [A(t|T) - A(t|t)]
    // / </summary>
    // / <param name="period"></param>
    // / <param name="series"></param>
    // / <param name="mode"></param>
    // / <returns></returns>
    /**
     *
     * @param series
     * @param period
     * @param mode
     * @return
     */
    public double seriesRevision(String series, TsPeriod period,
            DiagnosticInfo mode) {
        return seriesRevision(series, period, mode.asFunction());
    }

    public double seriesRevision(String series, TsPeriod period,
            DiagnosticTsFunction fn) {
        TsPeriod start = m_domainT.getStart();
        TsDomain domain = new TsDomain(start, period.minus(start) + 1);
        T it = tsInfo(domain);
        if (it == null) {
            return Double.NaN;
        }
        T iT = tsInfo(m_domainT);
        TsData tdata = it.getData(series, TsData.class), Tdata = iT.getData(series, TsData.class);
        if (tdata == null || Tdata == null) {
            return Double.NaN;
        }
        int idx = domain.getLength() - 1;
        return fn.apply(Tdata, tdata, idx);
    }

    /**
     *
     * @param domain
     * @return
     */
    public T tsInfo(TsDomain domain) {
        T info = m_cache.get(domain);
        if (info == null) {
            info = m_processing.process(domain);
            m_cache.put(domain, info);
        }
        return info;
    }

    // / <summary>
    // / Returns the value of the time series identified by "item" at a given
    // point ("period"),
    // / starting from a given date (start).
    // / In a formal way:
    // /
    // / Y = { Info([p0, p1])(period)}, p1 >= start
    // / </summary>
    // / <param name="item">The identifier of the series</param>
    // / <param name="period">The period of interest</param>
    // / <param name="start">The first period</param>
    // / <returns></returns>
    /**
     *
     * @param item
     * @param period
     * @param start
     * @return
     */
    public TsData tsRevision(String item, TsPeriod period, TsPeriod start) {
        TsPeriod p0 = m_domainT.getStart();
        int pos = period.minus(p0);
        TsData rslt = new TsData(start, m_domainT.getEnd().minus(start));
        int len = start.minus(p0) + 1;
        for (int i = 0; i < rslt.getLength(); ++i, ++len) {
            TsDomain rdom = new TsDomain(p0, len);
            T output = tsInfo(rdom);
            if (output != null) {
                TsData t = output.getData(item, TsData.class);
                if (t != null) {
                    rslt.set(i, t.get(pos));
                } else {
                    rslt.set(i, Double.NaN);
                }
            }
        }
        return rslt;
    }
 
    public TsData tsRevision(String item, TsPeriod period, TsPeriod start, TsDataFunction fn ) {
        TsPeriod p0 = m_domainT.getStart();
        int pos = period.minus(p0);
        TsData rslt = new TsData(start, m_domainT.getEnd().minus(start));
        int len = start.minus(p0) + 1;
        for (int i = 0; i < rslt.getLength(); ++i, ++len) {
            TsDomain rdom = new TsDomain(p0, len);
            T output = tsInfo(rdom);
            if (output != null) {
                TsData t = output.getData(item, TsData.class);
                if (t != null) {
                    rslt.set(i, fn.apply(t, pos));
                } else {
                    rslt.set(i, Double.NaN);
                }
            }
        }
        return rslt;
    }
}
