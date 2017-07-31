/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.timeseries.simplets;

import demetra.data.DataBlock;
import demetra.data.DoubleReader;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriodSelector;
import java.time.LocalDateTime;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.IFiniteFilter;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsDataToolkit {

    public TsData fn(TsData s, DoubleUnaryOperator fn) {
        double[] data = s.values().toArray();
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(data[i]);
        }
        return TsData.ofInternal(s.getStart(), data);
    }

    public TsData fastFn(TsData s, DoubleUnaryOperator fn) {
        return TsData.ofInternal(s.getStart(), DoubleSequence.of(s.length(), i -> fn.applyAsDouble(s.getValue(i))));
    }

    public TsData commit(TsData s) {
        return TsData.of(s.getStart(), s.values());
    }

    public TsData fastFn(TsData left, TsData right, DoubleBinaryOperator fn) {
        TsDomain lDomain = left.domain();
        TsDomain rDomain = right.domain();
        TsDomain iDomain = intersection(lDomain, rDomain);
        if (iDomain == null) {
            return null;
        }
        TsPeriod istart = iDomain.getStart();
        int li = lDomain.search(istart), ri = rDomain.search(istart);
        return TsData.ofInternal(istart, DoubleSequence.of(iDomain.length(),
                i -> fn.applyAsDouble(left.getValue(li + i), right.getValue(ri + i))));
    }

    public TsData fn(TsData left, TsData right, DoubleBinaryOperator fn) {
        TsDomain lDomain = left.domain();
        TsDomain rDomain = right.domain();
        TsDomain iDomain = intersection(lDomain, rDomain);
        if (iDomain == null) {
            return null;
        }

        TsPeriod istart = iDomain.getStart();
        int li = lDomain.search(istart), ri = rDomain.search(istart);
        double[] data = new double[iDomain.length()];
        DoubleReader lreader = left.values().reader(), rreader = right.values().reader();
        lreader.setPosition(li);
        rreader.setPosition(ri);
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(lreader.next(), rreader.next());
        }
        return TsData.ofInternal(istart, data);
    }

    public TsData fn(TsData s, int lag, DoubleBinaryOperator fn) {
        int n = s.length() - lag;
        if (n <= 0) {
            return null;
        }
        double[] nvalues = new double[n];
        for (int j = 0; j < lag; ++j) {
            double prev = s.getValue(j);
            for (int i = j; i < n; i += lag) {
                double next = s.getValue(i + lag);
                nvalues[i] = fn.applyAsDouble(prev, next);
                prev = next;
            }
        }
        return TsData.ofInternal(s.getStart().plus(lag), DoubleSequence.ofInternal(nvalues));
    }

    public TsData drop(TsData s, int nbeg, int nend) {
        TsDomain ndomain = TsDataToolkit.drop(s.domain(), nbeg, nend);
        return TsData.of(ndomain.getStart(), s.values().extract(nbeg, ndomain.length()));
    }

    public TsData select(TsData s, TsPeriodSelector selector) {
        TsDomain ndomain = select(s.domain(), selector);
        final int beg = ndomain.getStart().minus(s.getStart());
        return TsData.of(ndomain.getStart(), s.values().extract(beg, ndomain.length()));
    }

    public TsDomain drop(final TsDomain domain, @Nonnegative int nbeg, @Nonnegative int nend) {
        TsPeriod start = domain.get(nbeg);
        int len = domain.length() - nbeg - nend;
        if (len < 0) {
            len = 0;
        }
        return TsDomain.of(start, len);
    }

    public TsDomain intersection(final TsDomain d1, final TsDomain d2) {
        if (d1 == d2) {
            return d1;
        }
        TsFrequency freq = d1.getFrequency();
        if (freq != d2.getFrequency()) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }

        int n1 = d1.length(), n2 = d2.length();

        int lbeg = d1.id(), rbeg = d2.id();

        int lend = lbeg + n1, rend = rbeg + n2;
        int beg = lbeg <= rbeg ? rbeg : lbeg;
        int end = lend >= rend ? rend : lend;

        return TsDomain.of(TsPeriod.ofInternal(freq, beg), Math.max(0, end - beg));
    }

    /**
     * Returns the union between this domain and another one.
     *
     * @param d1
     * @param d2 Another domain. Should have the same frequency.
     * @return <I>null</I> if the frequencies are not the same. If the actual
     * union contains a hole, it is removed in the returned domain.
     *
     */
    public TsDomain union(final TsDomain d1, final TsDomain d2) {
        if (d2 == d1) {
            return d1;
        }
        TsFrequency freq = d1.getFrequency();
        if (freq != d2.getFrequency()) {
            return null;
        }

        int ln = d1.length(), rn = d2.length();
        int lbeg = d1.id(), rbeg = d2.id();
        int lend = lbeg + ln, rend = rbeg + rn;
        int beg = lbeg <= rbeg ? lbeg : rbeg;
        int end = lend >= rend ? lend : rend;

        return TsDomain.of(TsPeriod.ofInternal(freq, beg), end - beg);
    }

    /**
     * Makes a new domain from this domain and a period selector.
     *
     * @param domain
     * @param ps The selector.
     * @return The corresponding domain. May be Empty.
     */
    public TsDomain select(final TsDomain domain, final TsPeriodSelector ps) {
        if (domain.isEmpty()) {
            return domain;
        }
        // throw new ArgumentNullException("ps");

        int len = domain.length(), freq = domain.getFrequency().getAsInt();
        int nf = 0, nl = 0;
        TsPeriodSelector.SelectionType type = ps.getType();
        if (null != type) {
            switch (type) {
                case None:
                    nf = len;
                    break;
                case First: {
                    int nobs = ps.getN0();
                    nl = len - nobs;
                    break;
                }
                case Last: {
                    int nobs = ps.getN1();
                    nf = len - nobs;
                    break;
                }
                case Excluding:
                    nf = ps.getN0();
                    nl = ps.getN1();
                    if (nf < 0) {
                        nf = -nf * freq;
                    }
                    if (nl < 0) {
                        nl = -nl * freq;
                    }
                    break;
                default:
                    if ((type == TsPeriodSelector.SelectionType.From)
                            || (type == TsPeriodSelector.SelectionType.Between)) {
                        LocalDateTime d = ps.getD0();
                        int pos = domain.search(d);
                        if (pos < -1) {
                            nf = len;
                        } else if (pos >= 0) {
                            if (domain.get(pos).start().isBefore(d)) {
                                nf = pos + 1;
                            } else {
                                nf = pos;
                            }
                        }
                    }
                    if ((type == TsPeriodSelector.SelectionType.To)
                            || (type == TsPeriodSelector.SelectionType.Between)) {
                        LocalDateTime d = ps.getD1();
                        int pos = domain.search(d);
                        if (pos == -1) {
                            nl = len; // on ne garde rien
                        } else if (pos >= 0) {
                            if (domain.get(pos + 1).start().isBefore(d)) {
                                nl = len - pos;
                            } else {
                                nl = len - pos - 1;
                            }
                        }
                    }
                    break;
            }
        }
        if (nf < 0) {
            nf = 0;
        }
        if (nl < 0) {
            nl = 0;
        }
        return TsDomain.of(TsPeriod.ofInternal(domain.getFrequency(), domain.id() + nf), len - nf - nl);
    }

    // some useful shortcuts
    public TsData log(TsData s) {
        return fastFn(s, x -> Math.log(x));
    }

    public TsData exp(TsData s) {
        return fastFn(s, x -> Math.exp(x));
    }

    public TsData inv(TsData s) {
        return fastFn(s, x -> 1 / x);
    }

    public TsData chs(TsData s) {
        return fastFn(s, x -> -x);
    }

    public TsData abs(TsData s) {
        return fastFn(s, x -> Math.abs(x));
    }

    public TsData add(TsData l, TsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return fn(l, r, (a, b) -> a + b);
        }
    }

    public TsData add(TsData l, double d) {
        if (d == 0) {
            return l;
        } else {
            return fastFn(l, x -> x + d);
        }
    }

    public TsData subtract(TsData l, double d) {
        if (d == 0) {
            return l;
        } else {
            return fastFn(l, x -> x - d);
        }
    }

    public TsData subtract(double d, TsData l) {
        if (d == 0) {
            return chs(l);
        } else {
            return fastFn(l, x -> d - x);
        }
    }

    public TsData subtract(TsData l, TsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return fastFn(l, r, (a, b) -> a - b);
        }
    }

    public TsData multiply(TsData l, TsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return fastFn(l, r, (a, b) -> a * b);
        }
    }

    public TsData multiply(TsData l, double d) {
        if (d == 1) {
            return l;
        } else if (d == 0) {
            return fastFn(l, x -> 0);
        } else {
            return fastFn(l, x -> x - d);
        }
    }

    public TsData divide(TsData l, TsData r) {
        if (l == null) {
            return inv(r);
        } else if (r == null) {
            return l;
        } else {
            return fastFn(l, r, (a, b) -> a / b);
        }
    }

    public TsData divide(TsData l, double d) {
        if (d == 1) {
            return l;
        } else {
            return fastFn(l, x -> x / d);
        }
    }

    public TsData divide(double d, TsData l) {
        return fastFn(l, x -> d / x);
    }

    public TsData delta(TsData s, int lag) {
        return fn(s, lag, (x, y) -> y - x);
    }

    public TsData pctVariation(TsData s, int lag) {
        return fn(s, lag, (x, y) -> (y / x - 1) * 100);
    }

    public TsData normalize(TsData s) {
        double[] data = s.values().toArray();
        DataBlock values = DataBlock.ofInternal(data);
        final double mean = values.average();
        double ssqc = values.ssqc(mean);
        final double std = Math.sqrt(ssqc / values.length());
        for (int i = 0; i < data.length; ++i) {
            data[i] = (data[i] - mean) / std;
        }
        return TsData.ofInternal(s.getStart(), data);
    }

    public TsData lead(TsData s, @Nonnegative int lead) {
        return lead == 0 ? s : TsData.ofInternal(s.getStart().minus(lead), s.values());
    }

    public TsData lag(TsData s, @Nonnegative int lag) {
        return lag == 0 ? s : TsData.ofInternal(s.getStart().plus(lag), s.values());
    }

    public TsData apply(IFiniteFilter filter, TsData s){
        double[] data = s.values().toArray();
        double[] result=new double[data.length-filter.length()+1];
        filter.apply(DataBlock.ofInternal(data), DataBlock.ofInternal(result));
        return TsData.ofInternal(s.getStart().minus(filter.getLowerBound()), result);
    }
}
