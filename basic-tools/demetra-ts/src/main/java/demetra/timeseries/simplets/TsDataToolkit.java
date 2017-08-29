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
package demetra.timeseries.simplets;

import demetra.data.DataBlock;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;
import demetra.maths.linearfilters.IFiniteFilter;
import demetra.timeseries.Fixme;
import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsException;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsPeriodSelector;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;

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
        RegularDomain lDomain = left.domain();
        RegularDomain rDomain = right.domain();
        RegularDomain iDomain = lDomain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }
        TsPeriod istart = iDomain.getStartPeriod();
        int li = lDomain.indexOf(istart), ri = rDomain.indexOf(istart);
        return TsData.ofInternal(istart, DoubleSequence.of(iDomain.length(),
                i -> fn.applyAsDouble(left.getValue(li + i), right.getValue(ri + i))));
    }

    public TsData fn(TsData left, TsData right, DoubleBinaryOperator fn) {
        RegularDomain lDomain = left.domain();
        RegularDomain rDomain = right.domain();
        RegularDomain iDomain = lDomain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }

        TsPeriod istart = iDomain.getStartPeriod();
        int li = lDomain.indexOf(istart), ri = rDomain.indexOf(istart);
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
        RegularDomain ndomain = s.domain().select(TsPeriodSelector.excluding(nbeg, nend));
        return TsData.of(ndomain.getStartPeriod(), s.values().extract(nbeg, ndomain.length()));
    }

    public TsData select(TsData s, TsPeriodSelector selector) {
        RegularDomain ndomain = s.domain().select(selector);
        final int beg = s.getStart().until(ndomain.getStartPeriod());
        return TsData.of(ndomain.getStartPeriod(), s.values().extract(beg, ndomain.length()));
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
    public TsData fitToDomain(TsData s, RegularDomain domain) {
        if (!s.getUnit().equals(domain.getStartPeriod().getUnit())) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        RegularDomain sdomain = s.domain();
        int nbeg = Fixme.getId(domain) - Fixme.getId(sdomain);
        RegularDomain idomain = domain.intersection(sdomain);
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
            s.values().extract(Fixme.getId(idomain) - Fixme.getId(sdomain), ncommon).copyTo(data, cur);
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
        return lead == 0 ? s : TsData.ofInternal(s.getStart().plus(-lead), s.values());
    }

    public TsData lag(TsData s, @Nonnegative int lag) {
        return lag == 0 ? s : TsData.ofInternal(s.getStart().plus(lag), s.values());
    }

    public TsData apply(IFiniteFilter filter, TsData s) {
        double[] data = s.values().toArray();
        double[] result = new double[data.length - filter.length() + 1];
        filter.apply(DataBlock.ofInternal(data), DataBlock.ofInternal(result));
        return TsData.ofInternal(s.getStart().plus(-filter.getLowerBound()), result);
    }
}
