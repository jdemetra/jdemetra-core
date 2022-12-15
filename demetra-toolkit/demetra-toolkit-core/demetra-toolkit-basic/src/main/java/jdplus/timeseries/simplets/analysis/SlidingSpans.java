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

import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.util.Arrays2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 * @param <I>
 */
@Development(status = Development.Status.Preliminary)
public class SlidingSpans<I> {

    class MaxMin {

        double max;
        double min;
        int count;

        void add(double val) {
            if (count == 0) {
                max = val;
                min = val;
            } else {
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }
            }
            ++count;
        }

        double value(DiagnosticInfo info) {
            if (info == DiagnosticInfo.RelativeDifference) {
                return (max - min) / min;
            } else {
                return max - min;
            }
        }
    }

    private static class Node<I> {

        TsDomain domain;
        I estimation;
    }

    private Node<I>[] m_estimation;

    private final Function<TsDomain, I> m_processing;

    private final TsDomain m_domainT;

    private final I m_reference;

    private int m_spanLength = 8;

    private int m_spanCount = 4;

    private final int m_spanDistance = 1;

    private int m_spanMin = 2;

    /**
     *
     * @param processing
     * @param domain
     */
    public SlidingSpans(TsDomain domain, Function<TsDomain, I> processing) {
        m_processing = processing;
        m_domainT = domain;
        m_reference = processing.apply(m_domainT);
    }

    private void addDel(int p,
            HashMap<TsPeriod, SlidingSpans<I>.MaxMin> buffer, TsData data) {
        TsPeriod start = data.getStart();
        for (int i = p; i < data.length(); ++i) {
            TsPeriod cur = start.plus(i);
            MaxMin Mm = buffer.get(cur);
            if (Mm == null) {
                Mm = new MaxMin();
                buffer.put(cur, Mm);
            }
            Mm.add(data.getValue(i) - data.getValue(i - p));
        }
    }

    private void addPct(int p,
            HashMap<TsPeriod, SlidingSpans<I>.MaxMin> buffer, TsData data) {
        TsPeriod start = data.getStart();
        for (int i = p; i < data.length(); ++i) {
            TsPeriod cur = start.plus(i);
            MaxMin Mm = buffer.get(cur);
            if (Mm == null) {
                Mm = new MaxMin();
                buffer.put(cur, Mm);
            }
            Mm.add(data.getValue(i) / data.getValue(i - p));
        }
    }

    private void addValue(HashMap<TsPeriod, SlidingSpans<I>.MaxMin> buffer,
            TsData data) {
        TsPeriod start = data.getStart();
        for (int i = 0; i < data.length(); ++i) {
            TsPeriod cur = start.plus(i);
            MaxMin Mm = buffer.get(cur);
            if (Mm == null) {
                Mm = new MaxMin();
                buffer.put(cur, Mm);
            }
            Mm.add(data.getValue(i));
        }
    }

    /**
     *
     * @return
     */
    public int getMaxSpanCount() {
        return m_spanCount;
    }

    /**
     *
     * @return
     */
    public int getMinSpanCount() {
        return m_spanMin;
    }

    /**
     *
     * @return
     */
    public Function<TsDomain, I> getProcessing() {
        return m_processing;
    }

    /**
     *
     * @return
     */
    public I getReferenceInfo() {
        return m_reference;
    }

    public TsDomain getReferenceDomain() {
        return m_domainT;
    }

    /**
     *
     * @return
     */
    public int getSpanCount() {
        if (m_estimation == null && !process()) {
            return 0;
        }
        return m_estimation.length;
    }

    /**
     *
     * @return
     */
    public int getSpanLength() {
        return m_spanLength;
    }

    /**
     *
     * @param idx
     * @return
     */
    public I info(int idx) {
        if (m_estimation == null && !process()) {
            return null;
        }
        return m_estimation.length <= idx ? null : m_estimation[idx].estimation;
    }

    public TsDomain getDomain(int idx) {
        if (m_estimation == null && !process()) {
            return null;
        }
        return m_estimation.length <= idx ? null : m_estimation[idx].domain;

    }

    /**
     *
     * @return
     */
    public boolean isValid() {
        if (m_estimation == null && !process()) {
            return false;
        }
        return m_estimation.length >= m_spanMin;
    }

    /**
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean process() {
        if (m_estimation != null) {
            return true;
        }
        ArrayList<Node<I>> rslts = new ArrayList<>();
        int freq = m_domainT.getAnnualFrequency();
        int length = m_spanLength * freq;
        TsPeriod start = m_domainT.getLastPeriod().plus(1 - length);
        if (start.annualPosition() != 0) {
            length += start.annualPosition();
            start = start.plus(-start.annualPosition());
        }
        int idx = 0;
        while (idx < m_spanCount && !start.isBefore(m_domainT.getStartPeriod()))
	    try {
            ++idx;
            TsDomain cur = TsDomain.of(start, length);
            I info = m_processing.apply(cur);
            if (info == null) {
                break;
            } else {
                Node<I> node = new Node<>();
                node.estimation = info;
                node.domain = cur;
                rslts.add(node);
            }
            start = start.plus(-m_spanDistance * freq);
        } catch (Exception err) {
            break;
        }

        if (rslts.size() < m_spanMin) {
            return false;
        }
        m_estimation = rslts.toArray(Node[]::new);
        Arrays2.reverse(m_estimation);
        return true;
    }

    /**
     *
     * @param fn
     * @return
     */
    public TsData referenceSeries(Function<I, TsData> fn) {
        if (m_reference == null) {
            return null;
        }
        return fn.apply(m_reference);
    }

    /**
     *
     * @param value
     */
    public void setMaxSpanCount(int value) {
        if (value != m_spanCount) {
            m_estimation = null;
        }
        m_spanCount = value;
    }

    /**
     *
     * @param value
     */
    public void setMinSpanCount(int value) {
        if (value < 2) {
            throw new DiagnosticException(
                    DiagnosticException.InvalidSlidingSpanArgument);
        }
        m_spanMin = value;
    }

    /**
     *
     * @param value
     */
    public void setSpanLength(int value) {
        if (value != m_spanLength) {
            m_estimation = null;
        }
        m_spanLength = value;
    }

    /**
     *
     * @param info
     * @param function Extractor
     * @return
     */
    public TsData statistics(DiagnosticInfo info, Function<I, TsData> function) {
        if (m_estimation == null && !process()) {
            return null;
        }
        if (getSpanCount() < getMinSpanCount()) {
            return null;
        }
        HashMap<TsPeriod, MaxMin> buffer = new HashMap<>();
        for (int i = 0; i < m_estimation.length; ++i) {
            TsData data = function.apply(m_estimation[i].estimation);
            if (data != null) {
                switch (info) {
                    case PeriodToPeriodGrowthDifference ->
                        addPct(1, buffer, data);
                    case AnnualGrowthDifference ->
                        addPct(data.getAnnualFrequency(), buffer, data);
                    case PeriodToPeriodDifference ->
                        addDel(1, buffer, data);
                    case AnnualDifference ->
                        addDel(data.getAnnualFrequency(), buffer, data);
                    default ->
                        addValue(buffer, data);
                }
            }
        }

        double[] x = new double[m_domainT.length()];
        for (int i=0; i<x.length; ++i)
            x[i]=Double.NaN;
        TsPeriod start = m_domainT.getStartPeriod();
        for (Entry<TsPeriod, MaxMin> kv : buffer.entrySet()) {
            if (kv.getValue().count >= m_spanMin) {
                int idx = start.until(kv.getKey());
                x[idx] = kv.getValue().value(info);
            }
        }
        return TsData.ofInternal(start, x).cleanExtremities();
    }
}
