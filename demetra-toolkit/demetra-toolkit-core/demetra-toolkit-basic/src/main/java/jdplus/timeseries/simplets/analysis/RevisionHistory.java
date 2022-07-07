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
package jdplus.timeseries.simplets.analysis;

import demetra.information.Explorable;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 * @param <T>
 */
@Development(status = Development.Status.Preliminary)
public class RevisionHistory<T> {

    private final Function<TsDomain, T> m_processing;

    private final HashMap<TsDomain, T> m_cache = new HashMap<>();

    private final TsDomain m_domainT;

    /**
     *
     * @param processing
     * @param domain
     */
    public RevisionHistory(TsDomain domain, Function<TsDomain, T> processing) {
        m_processing = processing;
        m_domainT = domain;
        m_cache.put(m_domainT, processing.apply(m_domainT));
    }

    /**
     *
     * @return
     */
    public Function<TsDomain, T> getProcessing() {
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

    /**
     *
     * @param period
     * @param lag
     * @param count
     * @param mode
     * @param target
     * @param extractor
     * @return
     */
    public double[] laggedSeriesRevision(TsPeriod period,
            int lag, int count, DiagnosticInfo mode, DiagnosticTarget target, Function<T, TsData> extractor) {
        TsPeriod start = m_domainT.getStartPeriod();
        TsDomain domain = TsDomain.of(start, start.until(period) + 1);
        List<T> ilag = new ArrayList<>(count);
        TsDomain ldomain = domain;
        for (int i = 0; i < count; ++i) {
            ldomain = ldomain.extend(0, lag);
            ilag.add(tsInfo(ldomain));
        }
        double[] rslt = new double[count];
        if (target == DiagnosticTarget.Final) {
            T iT = tsInfo(m_domainT);
            TsData Tdata = iT != null ? extractor.apply(iT) : null;
            if (Tdata == null) {
                return null;
            }
            for (int i = 0; i < count; ++i) {
                rslt[i] = Double.NaN;
                T cur = ilag.get(i);
                if (cur != null) {
                    TsData tdata = extractor.apply(cur);
                    if (tdata != null) {
                        int idx = tdata.length() - 1;
                        rslt[i] = mode.asFunction().apply(Tdata, tdata, idx);
                    }
                }
            }
        } else {
            T it = tsInfo(domain);
            TsData cdata = it != null ? extractor.apply(it) : null;
            if (cdata == null) {
                return null;
            }
            for (int i = 0; i < count; ++i) {
                rslt[i] = Double.NaN;
                T cur = ilag.get(i);
                if (cur != null) {
                    TsData tdata = extractor.apply(cur);
                    if (tdata != null) {
                        int idx = tdata.length() - 1;
                        rslt[i] = mode.asFunction().apply(cdata, tdata, idx);
                    }
                }
            }
        }
        return rslt;
    }

    /**
     *
     * @param extractor
     * @return
     */
    public TsData referenceSeries(Function<T, TsData> extractor) {
        T it = tsInfo(m_domainT);
        return it == null ? null : extractor.apply(it);
    }

    /**
     *
     * @param start
     * @param extractor
     * @return
     */
    public TsData revision(TsPeriod start, ToDoubleFunction<T> extractor) {
        TsPeriod p0 = m_domainT.getStartPeriod();
        double[] x = new double[start.until(m_domainT.getEndPeriod())];
        int len = p0.until(start) + 1;
        for (int i = 0; i < x.length; ++i, ++len) {
            TsDomain rdom = TsDomain.of(p0, len);
            T output = tsInfo(rdom);
            if (output != null) {
                x[i] = extractor.applyAsDouble(output);
            }
        }
        return TsData.ofInternal(start, x);
    }

    public List<TsData> select(LocalDate beg, LocalDate end, Function<T, TsData> extractor) {
        ArrayList<TsData> s = new ArrayList<>();
        TsPeriod start = m_domainT.getStartPeriod();
        TsPeriod pbeg = start.withDate(beg.atStartOfDay());
        TsPeriod pend = start.withDate(end.atStartOfDay());
        if (pend.isAfter(m_domainT.getLastPeriod())) {
            pend = m_domainT.getLastPeriod();
        }
        int n = pbeg.until(pend);
        if (n >= 0) {
            int len = start.until(pbeg) + 1;
            for (int i = 0; i <= n; ++i) {
                try {
                    TsDomain dom = TsDomain.of(start, len++);
                    T output = tsInfo(dom);
                    if (output != null) {
                        TsData q = extractor.apply(output);
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
     * @param period
     * @param extractor
     * @return
     */
    public TsData series(TsPeriod period, Function<T, TsData> extractor) {
        TsPeriod start = m_domainT.getStartPeriod();
        TsDomain domain = TsDomain.of(start, start.until(period) + 1);
        T it = tsInfo(domain);
        if (it == null) {
            return null;
        }
        return extractor.apply(it);
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
     * @param period
     * @param extractor
     * @param mode
     * @return
     */
    public double seriesRevision(TsPeriod period,
            DiagnosticInfo mode, Function<T, TsData> extractor) {
        return seriesRevision(period, mode.asFunction(), extractor);
    }

    public double seriesRevision(TsPeriod period,
            DiagnosticTsFunction fn, Function<T, TsData> extractor) {
        TsPeriod start = m_domainT.getStartPeriod();
        TsDomain domain = TsDomain.of(start, start.until(period) + 1);
        T it = tsInfo(domain);
        if (it == null) {
            return Double.NaN;
        }
        T iT = tsInfo(m_domainT);
        TsData tdata = extractor.apply(it), Tdata = extractor.apply(iT);
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
        synchronized (m_cache) {
            T info = m_cache.get(domain);
            if (info == null) {
                info = m_processing.apply(domain);
                m_cache.put(domain, info);
            }
            return info;
        }
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
     * @param period
     * @param start
     * @param extractor
     * @return
     */
    public TsData tsRevision(TsPeriod period, TsPeriod start, Function<T, TsData> extractor) {
        TsPeriod p0 = m_domainT.getStartPeriod();
        int pos = p0.until(period);
        double[] x = new double[start.until(m_domainT.getEndPeriod())];
        int len = p0.until(start) + 1;
        for (int i = 0; i < x.length; ++i, ++len) {
            TsDomain rdom = TsDomain.of(p0, len);
            T output = tsInfo(rdom);
            if (output != null) {
                TsData t = extractor.apply(output);
                if (t != null) {
                    x[i] = t.getValue(pos);
                } else {
                    x[i] = Double.NaN;
                }
            }
        }
        return TsData.ofInternal(start, x);
    }

    /**
     * 
     * @param period
     * @param start
     * @param fn
     * @param extractor
     * @return 
     */
    public TsData tsRevision(TsPeriod period, TsPeriod start, TsDataFunction fn, Function<T, TsData> extractor) {
        TsPeriod p0 = m_domainT.getStartPeriod();
        int pos = p0.until(period);
        double[] x = new double[start.until(m_domainT.getEndPeriod())];
        int len = p0.until(start) + 1;
        for (int i = 0; i < x.length; ++i, ++len) {
            TsDomain rdom = TsDomain.of(p0, len);
            T output = tsInfo(rdom);
            if (output != null) {
                TsData t = extractor.apply(output);
                if (t != null) {
                    x[i] = fn.apply(t, pos);
                } else {
                    x[i] = Double.NaN;
                }
            }
        }
        return TsData.ofInternal(start, x);
    }
}
