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

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsDataTable {

    /**
     *
     * @param curid
     * @param freq
     * @param newfreq
     * @return Integer.MIN_VALUE if no id is available
     */
    private static int getid(int curid, final int freq, final int newfreq) {
        if (newfreq == 0) {
            return Integer.MIN_VALUE;
        }
        if (freq == newfreq) {
            return curid;
        }
        // conversion factor
        int c = freq / newfreq;
        curid -= c - 1;
        if (curid % c != 0) {
            return Integer.MIN_VALUE;
        } else {
            return curid / c;
        }
    }
    private final List<TsData> m_data = new ArrayList<>();
    private TsPeriodSelector m_periodselector = new TsPeriodSelector();
    private TsDomain m_domain;
    private int[] m_freqs;
    private int[] m_ids, m_ns;
    private int m_curfreq;
    private int m_firstid, m_count;

    /**
     *
     */
    public TsDataTable() {
    }

    /**
     *
     */
    public void clear() {
        m_data.clear();
        internalClear();
    }

    /**
     *
     * @param periodId
     * @param seriesId
     * @return
     */
    public double getData(final int periodId, final int seriesId) {
        recalc();
        if ((periodId < 0) || (periodId >= m_count) || (seriesId < 0)
                || (seriesId >= m_data.size())) {
            throw new TsException("periodId", "TSTable");
        }
        int id = getid(periodId + m_firstid, m_curfreq, m_freqs[seriesId]); // id is expressed in the frequency of the series.
        if (id == Integer.MIN_VALUE) {
            throw new TsException("periodId", "TSTable");
        }
        if ((id < m_ids[seriesId])
                || (id >= m_ids[seriesId] + m_ns[seriesId])) {
            throw new TsException("seriesId", "TSTable");
        }
        return m_data.get(seriesId).get(id - m_ids[seriesId]);
    }

    /**
     *
     * @param periodId
     * @param seriesId
     * @return
     */
    public TsDataTableInfo getDataInfo(final int periodId, final int seriesId) {
        recalc();
        if ((periodId < 0) || (periodId >= m_count) || (seriesId < 0)
                || (seriesId >= m_data.size())) {
            throw new TsException("Invalid id", "TSTable");
        }
        int id = getid(periodId + m_firstid, m_curfreq, m_freqs[seriesId]);
        if (id == Integer.MIN_VALUE) // id is expressed in the frequency of the series.
        {
            return TsDataTableInfo.Empty;
        }
        if ((id < m_ids[seriesId])
                || (id >= m_ids[seriesId] + m_ns[seriesId])) {
            return TsDataTableInfo.Empty;
        }
        double data = m_data.get(seriesId).get(id - m_ids[seriesId]);
        if (!Double.isFinite(data)) {
            return TsDataTableInfo.Missing;
        } else {
            return TsDataTableInfo.Valid;
        }
    }

    /**
     *
     * @return
     */
    public TsDomain getDomain() {
        recalc();
        return m_domain;
    }

    /**
     *
     * @return
     */
    public TsPeriodSelector getSelector() {
        return m_periodselector.clone();
    }

    /**
     *
     * @return
     */
    public int getSeriesCount() {
        return m_data.size();
    }

    /**
     *
     * @param pos
     * @param ts
     */
    public void insert(final int pos, final TsData ts) {
        internalClear();
        if ((pos < 0) || (pos >= m_data.size())) {
            m_data.add(ts);
        } else {
            m_data.add(pos, ts);
        }
    }

    public void add(final TsData ts) {
        internalClear();
        m_data.add(ts);
    }

    public void add(final Iterable<TsData> ts) {
        internalClear();
        for (TsData s : ts) {
            m_data.add(s);
        }
    }

    public void add(TsData... ts) {
        internalClear();
        for (TsData s : ts) {
            m_data.add(s);
        }
    }

    private void internalClear() {
        m_curfreq = 0;
        m_count = 0;
        m_domain = null;
        m_freqs = null;
        m_ids = null;
        m_ns = null;
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        recalc();
        return m_count == 0;
    }

    private void recalc() {
        if (m_curfreq != 0) {
            return;
        }
        // initialization of the frequencies.
        int n = m_data.size();
        if (n == 0) {
            return;
        }
        m_freqs = new int[n];

        m_ids = new int[n];
        m_ns = new int[n];

        int ifirst = -1;
        for (int i = 0; i < n; ++i) {
            if (m_data.get(i) != null) {
                if (ifirst < 0) {
                    ifirst = i;
                }
                //START CORRECTION 25/04/2008
                //ifirst = i;
                //END CORRECTION 25/04/2008
                TsDomain tmp = m_data.get(i).getDomain();
                m_freqs[i] = tmp.getFrequency().intValue();
                m_ids[i] = tmp.firstid();
                m_ns[i] = tmp.getLength();
            }
        }
        if (ifirst < 0) {
            return;
        }

        // common frequency.
        m_curfreq = m_freqs[ifirst];
        for (int i = ifirst + 1; i < n; ++i) {
            // search for the ppcm	 (only divisor of 12)
            if (m_freqs[i] != 0 && m_curfreq % m_freqs[i] != 0) {
                m_curfreq *= m_freqs[i];
                if (m_curfreq > 12) {
                    m_curfreq = 12;
                }
            }
        }

        // creation of the new domain:
        int c = m_curfreq / m_freqs[ifirst];
        int firstid = (m_ids[ifirst] + 1) * c - 1, lastid = firstid + (m_ns[ifirst] - 1) * c;	//	expressed in m_curfreq...
        for (int i = ifirst + 1; i < n; ++i) {
            if (m_freqs[i] != 0) {
                c = m_curfreq / m_freqs[i];
                int f = (m_ids[i] + 1) * c - 1, l = f + (m_ns[i] - 1) * c;
                if (f < firstid) {
                    firstid = f;
                }
                if (l > lastid) {
                    lastid = l;
                }
            }
        }

        TsPeriod start = new TsPeriod(TsFrequency.valueOf(m_curfreq), firstid);
        TsDomain dtmp = new TsDomain(start, lastid - firstid + 1);
        m_domain = dtmp.select(m_periodselector);

        m_firstid = dtmp.firstid();
        m_count = dtmp.getLength();
    }

    /**
     *
     * @param idx
     * @return
     */
    public TsData series(final int idx) {
        return m_data.get(idx);
    }

    /**
     *
     * @param periodSelector
     */
    public void setSelector(final TsPeriodSelector periodSelector) {
        m_periodselector = periodSelector.clone();
        internalClear();
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(String[] headers) {
        if (headers != null && headers.length != m_data.size()) {
            throw new IllegalArgumentException("Incompatible headers for this TsDataTable");
        }
        StringBuilder builder = new StringBuilder();
        // write headers
        for (int i = 0; i < m_data.size(); ++i) {
            builder.append('\t');
            if (headers == null) {
                builder.append("s").append(i + 1);
            } else {
                builder.append(headers[i]);
            }
        }
        TsDomain dom = this.getDomain();
        if (dom == null || dom.isEmpty()) {
            return builder.toString();
        }
        // write each rows
        for (int j = 0; j < dom.getLength(); ++j) {
            builder.append(System.lineSeparator()).append(dom.get(j));
            for (int i = 0; i < m_data.size(); ++i) {
                TsDataTableInfo dataInfo = getDataInfo(j, i);
                if (dataInfo == TsDataTableInfo.Valid) {
                    builder.append('\t').append(this.getData(j, i));
                } else if (dataInfo == TsDataTableInfo.Missing) {
                    builder.append("\t.");
                } else {
                    builder.append('\t');
                }
            }
        }
        return builder.toString();
    }
}
