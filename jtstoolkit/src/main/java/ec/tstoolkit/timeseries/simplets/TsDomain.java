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
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.timeseries.*;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Represents a regular time domain. Such a domain is defined as a continuous
 * set of regular periods, i.e. period characterised by a TsFrequency (from
 * monthly to yearly).
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public final class TsDomain implements IDomain, Serializable, Iterable<TsPeriod> {

    private static final class TSPeriodIterator implements Iterator<TsPeriod> {

        private final TsDomain m_dom;
        private int m_cur = 0;

        TSPeriodIterator(final TsDomain domain) {
            m_dom = domain;
        }

        @Override
        public boolean hasNext() {
            return m_cur < m_dom.getLength();
        }

        @Override
        public TsPeriod next() {
            return m_dom.get(m_cur++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    private static final long serialVersionUID = 3500593038737276467L;
    private final TsFrequency m_freq;
    private final int m_beg;
    private final int m_c;

    TsDomain(final TsFrequency freq, final int beg, final int count) {
        m_freq = freq;
        m_beg = beg;
        m_c = count;
    }

    /**
     * Creates a new time domain, identified by its frequency, the year and the
     * position of the first period and the length of the domain.
     *
     * @param freq The frequency.
     * @param firstyear Year of the first period
     * @param firstperiod (0-based) position in the year of the first period.
     * @param count Length of the domain (number of periods).
     */
    public TsDomain(final TsFrequency freq, final int firstyear,
            final int firstperiod, final int count) {
        this(freq, TsPeriod.calcId(freq.intValue(), firstyear, firstperiod),
                count);
    }

    /**
     * Creates a new time domain from its first period and its length.
     *
     * @param start First period. This Object is not used internally.
     * @param count Number of periods.
     */
    public TsDomain(final TsPeriod start, final int count) {
        this(start.getFrequency(), start.id(), count);
    }

    /**
     * Checks that a given domain is inside another one. Both domains must have
     * the same frequency.
     *
     * @param domain The other domain
     * @return true if the given domain is (not strictly) included in this
     * domain.
     * @exception A TsException is thrown when the frequencies are not
     * identical.
     */
    public boolean contains(TsDomain domain) {
        if (this.m_freq != domain.m_freq) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        return this.m_beg <= domain.m_beg
                && this.m_beg + this.m_c >= domain.m_beg + domain.m_c;
    }

    /**
     * Shortens this domain.
     *
     * @param nfirst Number of periods to drop at the beginning of the domain.
     * If nfirst &lt 0, -nfirst periods are added.
     * @param nlast Number of periods to drop at the end of the domain. If nlast
     * &lt 0, -nlast periods are added.
     * @return The returned domain may be Empty.
     */
    public TsDomain drop(int nfirst, int nlast) {
        return extend(-nfirst, -nlast);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsDomain && equals((TsDomain) obj));
    }

    public boolean equals(TsDomain other) {
        return (m_freq == other.m_freq) && (m_beg == other.m_beg)
                && (m_c == other.m_c);
    }

    /**
     * Extends this domain.
     *
     * @param nbefore Number of periods to add at the beginning of the domain.
     * If nbefore &lt 0, -nbefore periods are dropped.
     * @param nafter Number of periods to add at the end of the domain. If
     * nafter &lt 0, -nafter periods are dropped.
     * @return The returned domain may be Empty.
     */
    public TsDomain extend(final int nbefore, final int nafter) {
        int c = Math.max(0, m_c + nbefore + nafter);
        return new TsDomain(m_freq, m_beg - nbefore, c);
    }

    int firstid() {
        return m_beg;
    }

    @Override
    public TsPeriod get(final int idx) {
        return new TsPeriod(m_freq, m_beg + idx);
    }

    /**
     * Return the first period at the end of the domain.
     *
     * @return The end of the domain. That period doesn't belong to the domain!
     */
    public TsPeriod getEnd() {
        return new TsPeriod(m_freq, m_beg + m_c);
    }

    /**
     * Returns the frequency of each periods.
     *
     * @return The frequency of the domain. Even an empty domain must have a
     * frequency.
     */
    public TsFrequency getFrequency() {
        return m_freq;
    }

    /**
     * Counts the number of years (complete or not).
     *
     * @return The number of years.
     */
    public int getYearsCount() {
        return getLast().getYear() - getStart().getYear() + 1;
    }

    /**
     * Counts the number of full years.
     *
     * @return The number of full years.
     */
    public int getFullYearsCount() {
        int ifreq = m_freq.intValue();
        int start = m_beg;
        int pos = start % ifreq;
        if (pos > 0) {
            start += ifreq - pos;
        }
        int end = m_beg + m_c;
        end -= end % ifreq;
        return (end - start) / ifreq;
    }

    /**
     * Returns the last period of the domain (which is just before getEnd().
     *
     * @return A new period is returned. Should not be used on empty domain,
     */
    public TsPeriod getLast() {
        return new TsPeriod(m_freq, m_beg + m_c - 1);
    }

    @Override
    public int getLength() {
        return m_c;
    }

    /**
     * Returns the first period of the domain.
     *
     * @return A new period is returned, even for empty domain,
     */
    public TsPeriod getStart() {
        return new TsPeriod(m_freq, m_beg);
    }

    @Override
    public int hashCode() {
        return m_freq.hashCode() + m_beg + m_c;
    }

    public static TsDomain and(TsDomain l, TsDomain r) {
        if (l == null) {
            return r;
        }
        if (r == null) {
            return l;
        }
        return l.intersection(r);
    }

    public static TsDomain or(TsDomain l, TsDomain r) {
        if (l == null) {
            return l;
        }
        if (r == null) {
            return l;
        }
        return l.union(r);
    }

    /**
     * Returns the intersection between this domain and another domain.
     *
     * @param d The other domain. Should have the same frequency.
     * @return <I>null</I> if the frequencies are not the same. May be Empty.
     */
    public TsDomain intersection(final TsDomain d) {
        if (d == this) {
            return this;
        }
        if (d.m_freq != m_freq) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        int ln = m_c, rn = d.m_c;
        int lbeg = m_beg, rbeg = d.m_beg;

        int lend = lbeg + ln, rend = rbeg + rn;
        int beg = lbeg <= rbeg ? rbeg : lbeg;
        int end = lend >= rend ? rend : lend;

        return new TsDomain(m_freq, beg, Math.max(0, end - beg));
    }

    /**
     * Checks if a domain is empty.
     *
     * @return true if the domain is empty. false otherwise.
     */
    public boolean isEmpty() {
        return this.m_c == 0;
    }

    /**
     * Returns an iterator on the periods of the domain
     *
     * @return A new iterator. The first call to the iterator should be
     * "has+Next()".
     */
    @Override
    public Iterator<TsPeriod> iterator() {
        return new TSPeriodIterator(this);
    }

    /**
     * Moves forward or backward this domain.
     *
     * @param nperiods Any integer values. Move forward if nperiods is &gt> 0,
     * backward otherwise.
     * @return The start of the returned TsDomain is changed of nperiods
     * position. The current object is not modified.
     */
    public TsDomain move(final int nperiods) {
        return new TsDomain(m_freq, m_beg + nperiods, m_c);
    }

    @Override
    public int search(final Day day) {
        TsPeriod p = new TsPeriod(m_freq);
        p.set(day);
        return search(p);
    }

    /**
     * Searches the place of a given period.
     *
     * @param p The period searched in the domain.
     * @return The index of the period is returned if it is found (exact match),
     * -1 otherwise.
     */
    public int search(final TsPeriod p) {

        if (p.getFrequency() != m_freq) {
            return -1;
        }
        int id = p.id();
        id -= m_beg;
        if ((id < 0) || (id >= m_c)) {
            return -1;
        } else {
            return id;
        }
    }

    /**
     * Makes a new domain from this domain and a period selector.
     *
     * @param ps The selector.
     * @return The corresponding domain. May be Empty.
     */
    public TsDomain select(final TsPeriodSelector ps) {
        if (m_c == 0) {
            return this;
        }
        // throw new ArgumentNullException("ps");

        int nf = 0, nl = 0;
        PeriodSelectorType type = ps.getType();
        if (type == PeriodSelectorType.None) {
            nf = m_c;
        } else if (type == PeriodSelectorType.First) {
            int nobs = ps.getN0();
            nl = m_c - nobs;
        } else if (type == PeriodSelectorType.Last) {
            int nobs = ps.getN1();
            nf = m_c - nobs;
        } else if (type == PeriodSelectorType.Excluding) {
            nf = ps.getN0();
            nl = ps.getN1();
            if (nf < 0) {
                nf = -nf * m_freq.intValue();
            }
            if (nl < 0) {
                nl = -nl * m_freq.intValue();
            }

        } else {
            if ((type == PeriodSelectorType.From)
                    || (type == PeriodSelectorType.Between)) {
                Day d = ps.getD0();
                TsPeriod cur = new TsPeriod(m_freq);
                cur.set(d);
                int c = cur.id() - m_beg;
                if (c >= m_c) {
                    nf = m_c; // on ne garde rien
                } else if (c >= 0) {
                    if (cur.firstday().isBefore(d)) {
                        nf = c + 1;
                    } else {
                        nf = c;
                    }
                }
            }
            if ((type == PeriodSelectorType.To)
                    || (type == PeriodSelectorType.Between)) {
                Day d = ps.getD1();
                TsPeriod cur = new TsPeriod(m_freq);
                cur.set(d);

                int c = cur.id() - m_beg;
                if (c < 0) {
                    nl = m_c; // on ne garde rien
                } else if (c < m_c) {
                    if (cur.lastday().isAfter(d)) {
                        nl = m_c - c;
                    } else {
                        nl = m_c - c - 1;
                    }
                }
            }
        }
        if (nf < 0) {
            nf = 0;
        }
        if (nl < 0) {
            nl = 0;
        }
        return new TsDomain(m_freq, m_beg + nf, m_c - nf - nl);
    }

    /**
     * Returns the union between this domain and another one.
     *
     * @param d Another domain. Should have the same frequency.
     * @return <I>null</I> if the frequencies are not the same. If the actual
     * union contains a hole, it is removed in the returned domain.
     *
     */
    public TsDomain union(final TsDomain d) {
        if (d == this) {
            return this;
        }
        if (d.m_freq != m_freq) {
            return null;
        }

        int ln = m_c, rn = d.m_c;
        int lbeg = m_beg, rbeg = d.m_beg;
        int lend = lbeg + ln, rend = rbeg + rn;
        int beg = lbeg <= rbeg ? lbeg : rbeg;
        int end = lend >= rend ? lend : rend;

        return new TsDomain(m_freq, beg, end - beg);
    }

    public TsDomain changeFrequency(final TsFrequency newfreq, final boolean complete) {
        int freq = m_freq.intValue(), nfreq = newfreq.intValue();
        if (freq == nfreq) {
            return this;
        }

        if (freq > nfreq) {
            if (freq % nfreq != 0) {
                return null;
            }

            int nconv = freq / nfreq;

            int z0 = 0;

            // beginning and end
            int nbeg = m_beg / nconv;
            int n0 = nconv, n1 = nconv;
            if (m_beg % nconv != 0) {
                if (complete) {
                    if (m_beg > 0) {
                        ++nbeg;
                    }
                    z0 = nconv - m_beg % nconv;
                } else {
                    if (m_beg < 0) {
                        --nbeg;
                    }
                    n0 = (nbeg + 1) * nconv - m_beg;
                }
            }

            int end = m_beg + m_c; // excluded
            int nend = end / nconv;

            if (end % nconv != 0) {
                if (complete) {
                    if (end < 0) {
                        --nend;
                    }
                } else {
                    if (end > 0) {
                        ++nend;
                    }
                    n1 = end - (nend - 1) * nconv;
                }
            }
            int n = nend - nbeg;
            return new TsDomain(newfreq, nbeg, n);
        } else { // The new frequency is higher than the current one
            if (nfreq % freq != 0) {
                return null;
            }
            TsPeriod start=getStart().firstPeriod(newfreq);
            return new TsDomain(start, m_c*nfreq/freq);
        }
    }
    
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getStart()).append((" - ")).append(getLast());
        return builder.toString();
    }
}
