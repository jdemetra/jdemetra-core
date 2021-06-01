/*
 * Copyright 2017 National Bank create Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions create the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy create the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.timeseries.simplets;

import demetra.timeseries.TsData;
import jdplus.data.DataBlock;
import demetra.data.DoubleSeqCursor;
import jdplus.math.linearfilters.IFiniteFilter;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TimeSelector;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.checkerframework.checker.index.qual.NonNegative;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TsDataToolkit {

    public TsData fn(TsData s, DoubleUnaryOperator fn) {
        return TsData.ofInternal(s.getStart(), s.getValues().fn(fn).toArray());
    }

    public TsData fastFn(TsData s, DoubleUnaryOperator fn) {
        return TsData.of(s.getStart(), DoubleSeq.onMapping(s.length(), i -> fn.applyAsDouble(s.getValue(i))));
    }

    public TsData commit(TsData s) {
        return TsData.ofInternal(s.getStart(), s.getValues().toArray());
    }

    public TsData fastFn(TsData left, TsData right, DoubleBinaryOperator fn) {
        TsDomain lDomain = left.getDomain();
        TsDomain rDomain = right.getDomain();
        TsDomain iDomain = lDomain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }
        TsPeriod istart = iDomain.getStartPeriod();
        int li = lDomain.indexOf(istart), ri = rDomain.indexOf(istart);
        return TsData.of(istart, DoubleSeq.onMapping(iDomain.length(),
                i -> fn.applyAsDouble(left.getValue(li + i), right.getValue(ri + i))));
    }

    public TsData fn(TsData left, TsData right, DoubleBinaryOperator fn) {
        TsDomain lDomain = left.getDomain();
        TsDomain rDomain = right.getDomain();
        TsDomain iDomain = lDomain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }

        TsPeriod istart = iDomain.getStartPeriod();
        int li = lDomain.indexOf(istart), ri = rDomain.indexOf(istart);
        double[] data = new double[iDomain.length()];
        DoubleSeqCursor lreader = left.getValues().cursor(), rreader = right.getValues().cursor();
        lreader.moveTo(li);
        rreader.moveTo(ri);
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(lreader.getAndNext(), rreader.getAndNext());
        }
        return TsData.ofInternal(istart, data);
    }

    public TsData fn(TsData s, int lag, DoubleBinaryOperator fn) {
        return TsData.ofInternal(s.getStart().plus(lag), s.getValues().fn(lag, fn).toArray());
    }

    public TsData drop(TsData s, @NonNegative int nbeg, @NonNegative int nend) {
        int len=s.length()-nbeg-nend;
        TsPeriod start = s.getStart().plus(nbeg);
        return TsData.ofInternal(start, s.getValues().extract(nbeg, Math.max(0, len)).toArray());
    }

    public TsData extend(TsData s, @NonNegative int nbeg, @NonNegative int nend) {
        TsPeriod start = s.getStart().plus(-nbeg);
        return TsData.of(start, s.getValues().extend(nbeg, nend));
    }

    public TsData select(TsData s, TimeSelector selector) {
        TsDomain ndomain = s.getDomain().select(selector);
        final int beg = s.getStart().until(ndomain.getStartPeriod());
        return TsData.ofInternal(ndomain.getStartPeriod(), s.getValues().extract(beg, ndomain.length()).toArray());
    }

    /**
     * Extends the series to the specified domain. Missing values are added (or
     * some values are removed if necessary).
     *
     * @param s
     * @param domain The domain of the new series. Must have the same frequency
     * than the original series.
     * @return A new (possibly empty) series is returned (or null if the domain
     * hasn't the right frequency.
     */
    public TsData fitToDomain(TsData s, TsDomain domain) {
        if (!s.getTsUnit().equals(domain.getStartPeriod().getUnit())) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        TsDomain sdomain = s.getDomain();
        int nbeg = sdomain.getStartPeriod().until(domain.getStartPeriod());
        TsDomain idomain = domain.intersection(sdomain);
        double[] data = new double[domain.length()];
        int cur = 0;
        if (nbeg < 0) { // before s
            int cmax = Math.min(-nbeg, data.length);
            for (; cur < cmax; ++cur) {
                data[cur] = Double.NaN;
            }
        }
        int ncommon = idomain.length();
        // common data
        if (ncommon > 0) {
            s.getValues().extract(sdomain.getStartPeriod().until(idomain.getStartPeriod()), ncommon).copyTo(data, cur);
            cur += ncommon;
        }
        // after s
        for (; cur < data.length; ++cur) {
            data[cur] = Double.NaN;
        }
        return TsData.ofInternal(domain.getStartPeriod(), data);
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

    public double distance(TsData l, TsData r) {
        DoubleSeq diff = subtract(l, r).getValues();
        int n=diff.count(x->Double.isFinite(x));
        if (n == 0)
            return Double.NaN;
        return Math.sqrt(diff.ssqWithMissing()/n);
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
            return fastFn(l, x -> x * d);
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

    public TsData delta(TsData s, int lag, int pow) {
        TsData ns=s;
        for (int i=0; i<pow; ++i)
            ns=fn(ns, lag, (x, y) -> y - x);
        return ns;
    }

    public TsData pctVariation(TsData s, int lag) {
        return fn(s, lag, (x, y) -> (y / x - 1) * 100);
    }

    public TsData normalize(TsData s) {
        double[] data = s.getValues().toArray();
        DataBlock values = DataBlock.of(data);
        final double mean = values.average();
        double ssqc = values.ssqc(mean);
        final double std = Math.sqrt(ssqc / values.length());
        for (int i = 0; i < data.length; ++i) {
            data[i] = (data[i] - mean) / std;
        }
        return TsData.ofInternal(s.getStart(), data);
    }

    public TsData lead(TsData s, @NonNegative int lead) {
        return lead == 0 ? s : TsData.of(s.getStart().plus(-lead), s.getValues());
    }

    public TsData lag(TsData s, @NonNegative int lag) {
        return lag == 0 ? s : TsData.of(s.getStart().plus(lag), s.getValues());
    }

    public TsData apply(IFiniteFilter filter, TsData s) {
        double[] data = s.getValues().toArray();
        double[] result = new double[data.length - filter.length() + 1];
        filter.apply(DataBlock.of(data), DataBlock.of(result));
        return TsData.ofInternal(s.getStart().plus(-filter.getLowerBound()), result);
    }
}
