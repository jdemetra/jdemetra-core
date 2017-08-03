/*
 * Copyright 2017 National Bank create Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions create the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy create the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
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
import demetra.timeseries.CalendarTsData;
import demetra.timeseries.IDateDomain;
import demetra.timeseries.IDatePeriod;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import javax.annotation.Nonnegative;

/**
 *
 * @author Mats Maggi
 */
@lombok.experimental.UtilityClass
public class CalendarTsDataToolkit {

    public CalendarTsData fn(CalendarTsData s, DoubleUnaryOperator fn) {
        if (s.domain().isContinuous()) {
            double[] data = s.values().toArray();
            for (int i = 0; i < data.length; ++i) {
                data[i] = fn.applyAsDouble(data[i]);
            }
            return CalendarTsData.ofInternal(s.domain(), data);
        } else {
            throw new UnsupportedOperationException("fn() for non continuous domains");
        }
    }

    public CalendarTsData fastFn(CalendarTsData s, DoubleUnaryOperator fn) {
        if (s.domain().isContinuous()) {
            return CalendarTsData.ofInternal(s.domain(), DoubleSequence.of(s.length(), i -> fn.applyAsDouble(s.getValue(i))));
        } else {
            throw new UnsupportedOperationException("fn() for non continuous domains");
        }
    }

    public CalendarTsData fn(CalendarTsData left, CalendarTsData right, DoubleBinaryOperator fn) {
        if (left.domain().isContinuous() && right.domain().isContinuous()) {
            IDateDomain lDomain = left.domain();
            IDateDomain rDomain = right.domain();
            IDateDomain iDomain = lDomain.intersection(rDomain);
            if (iDomain == null) {
                return null;
            }
            IDatePeriod istart = iDomain.getStart();
            int li = lDomain.search(istart.firstDay()), ri = rDomain.search(istart.firstDay());
            double[] data = new double[iDomain.length()];
            DoubleReader lreader = left.values().reader(), rreader = right.values().reader();
            lreader.setPosition(li);
            rreader.setPosition(ri);
            for (int i = 0; i < data.length; ++i) {
                data[i] = fn.applyAsDouble(lreader.next(), rreader.next());
            }
            return CalendarTsData.ofInternal(iDomain, data);
        } else {
            throw new UnsupportedOperationException("fn() for non continuous domains");
        }
    }

    public CalendarTsData fn(CalendarTsData s, int lag, DoubleBinaryOperator fn) {
        if (s.domain().isContinuous()) {
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

            return CalendarTsData.ofInternal(s.domain().lag(lag), nvalues);
        } else {
            throw new UnsupportedOperationException("fn() for non continuous domains");
        }
    }

    public CalendarTsData drop(CalendarTsData s, int nbeg, int nend) {
        IDateDomain ndomain = drop(s.domain(), nbeg, nend);
        return CalendarTsData.of(ndomain, s.values().extract(nbeg, ndomain.length()));
    }

    public IDateDomain drop(final IDateDomain domain, @Nonnegative int nbeg, @Nonnegative int nend) {
        if (domain.isContinuous()) {
            return domain.range(nbeg, domain.length() - nend);
        } else {
            throw new UnsupportedOperationException("Non continuous domain");
        }
    }

    public CalendarTsData commit(CalendarTsData s) {
        return CalendarTsData.of(s.domain(), s.values());
    }

    public CalendarTsData log(CalendarTsData s) {
        return fastFn(s, x -> Math.log(x));
    }

    public CalendarTsData exp(CalendarTsData s) {
        return fastFn(s, x -> Math.exp(x));
    }

    public CalendarTsData inv(CalendarTsData s) {
        return fastFn(s, x -> 1 / x);
    }

    public CalendarTsData chs(CalendarTsData s) {
        return fastFn(s, x -> -x);
    }

    public CalendarTsData abs(CalendarTsData s) {
        return fastFn(s, x -> Math.abs(x));
    }

    public CalendarTsData add(CalendarTsData l, CalendarTsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return fn(l, r, (a, b) -> a + b);
        }
    }

    public CalendarTsData add(CalendarTsData l, double d) {
        if (d == 0) {
            return l;
        } else {
            return fastFn(l, x -> x + d);
        }
    }

    public CalendarTsData subtract(CalendarTsData l, double d) {
        if (d == 0) {
            return l;
        } else {
            return fastFn(l, x -> x - d);
        }
    }

    public CalendarTsData subtract(double d, CalendarTsData l) {
        if (d == 0) {
            return chs(l);
        } else {
            return fastFn(l, x -> d - x);
        }
    }

    public CalendarTsData multiply(CalendarTsData l, double d) {
        if (d == 1) {
            return l;
        } else if (d == 0) {
            return fastFn(l, x -> 0);
        } else {
            return fastFn(l, x -> x - d);
        }
    }

    public CalendarTsData divide(CalendarTsData l, double d) {
        if (d == 1) {
            return l;
        } else {
            return fastFn(l, x -> x / d);
        }
    }

    public CalendarTsData delta(CalendarTsData s, int lag) {
        return fn(s, lag, (x, y) -> y - x);
    }

    public CalendarTsData pctVariation(CalendarTsData s, int lag) {
        return fn(s, lag, (x, y) -> (y / x - 1) * 100);
    }

    public CalendarTsData normalize(CalendarTsData s) {
        double[] data = s.values().toArray();
        DataBlock values = DataBlock.ofInternal(data);
        final double mean = values.average();
        double ssqc = values.ssqc(mean);
        final double std = Math.sqrt(ssqc / values.length());
        for (int i = 0; i < data.length; ++i) {
            data[i] = (data[i] - mean) / std;
        }
        return CalendarTsData.ofInternal(s.domain(), data);
    }
}
