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

import ec.tstoolkit.arima.ArimaModelBuilder;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.data.Values;
import java.util.Arrays;
import java.util.Iterator;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.Unsafe;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.random.IRandomNumberGenerator;
import ec.tstoolkit.random.JdkRNG;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A TsData is a raw time series, containing only the actual data. TsData can
 * only handle regular time series, with observations corresponding to the usual
 * time decomposition of an year (frequency lower or equal to the monthly
 * frequency). Observations are represented by double values. Missing values are
 * allowed; they are represented by Double.NaN.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class TsData implements Cloneable, Iterable<TsObservation>, IReadDataBlock {

    private static final class TsIterator implements Iterator<TsObservation> {

        private final double[] m_vals;
        private final TsPeriod m_start;
        private int m_cur = -1;

        TsIterator(final TsData ts) {
            m_start = ts.start;
            m_vals = ts.vals;
        }

        @Override
        public boolean hasNext() {
            if (m_cur >= m_vals.length) {
                return false;
            }
            for (int i = m_cur + 1; i < m_vals.length; i++) {
                if (Double.isFinite(m_vals[i])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public TsObservation next() {
            ++m_cur;
            searchNext();
            return new TsObservation(m_start.plus(m_cur), m_vals[m_cur]);
        }

        // Search the next non Missing value, from the current position
        // (included !)
        // be careful to move first m_cur if needed
        //
        private void searchNext() {
            for (; m_cur < m_vals.length; ++m_cur) {
                if (Double.isFinite(m_vals[m_cur])) {
                    break;
                }
            }
        }
    }

    /**
     * Returns the sum of a given value with a time series.
     *
     * @param d The double value
     * @param ts The time series
     * @return A new time series is returned. T[i] = d + ts[i].
     * @see #plus(double)
     */
    public static TsData add(final double d, final TsData ts) {
        return ts.plus(d);
    }

    /**
     * Returns the sum of two time series. The time series must have the same
     * frequency, but not necessary the same time domain. The sum is computed on
     * the common time domain.
     *
     * @param tsl The left operand
     * @param tsr The right operand
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #plus(TsData)
     */
    public static TsData add(final TsData tsl, final TsData tsr) {
        if (tsr == null) {
            return tsl;
        } else if (tsl == null) {
            return tsr;
        }
        return computeOnIntersection(tsl, tsr, (a, b) -> a + b);
    }

    /**
     * Returns the division of a given value by a time series.
     *
     * @param d The double value
     * @param ts The time series
     * @return A new time series is returned. T[i] = d /ts[i].
     * @see #times(double)
     */
    public static TsData divide(final double d, final TsData ts) {
        TsData s = ts.clone();
        if (d != 0) {
            s.apply(a -> d / a);
        } else {
            s.set(() -> 0);
        }
        return s;
    }

    /**
     * Returns the division of two time series. The time series must have the
     * same frequency, but not necessary the same time domain. The quotient is
     * computed on the common time domain.
     *
     * @param tsl The left operand
     * @param tsr The right operand
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #plus(TsData)
     */
    public static TsData divide(final TsData tsl, final TsData tsr) {
        /*
         * if (tsl == null) throw new ArgumentNullException("tsl"); if (tsr ==
         * null) throw new ArgumentNullException("tsr");
         */
        if (tsr == null) {
            return tsl;
        } else if (tsl == null) {
            return tsr.inv();
        }
        return computeOnIntersection(tsl, tsr, (a, b) -> a / b);
    }

    /**
     * Returns the product of a given value by a time series.
     *
     * @param d The double value
     * @param ts The time series
     * @return A new time series is returned. T[i] = d * ts[i].
     * @see #times(double)
     */
    public static TsData multiply(final double d, final TsData ts) {
        return ts.times(d);
    }

    /**
     * Returns the product of two time series. The time series must have the
     * same frequency, but not necessary the same time domain. The product is
     * computed on the common time domain.
     *
     * @param tsl The left operand
     * @param tsr The right operand
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #plus(TsData)
     */
    public static TsData multiply(final TsData tsl, final TsData tsr) {
        if (tsr == null) {
            return tsl;
        } else if (tsl == null) {
            return tsr;
        }

        return computeOnIntersection(tsl, tsr, (a, b) -> a * b);
    }

    /**
     * Returns the difference between a given value and a time series.
     *
     * @param d The double value
     * @param ts The time series
     * @return A new time series is returned. T[i] = d - ts[i].
     * @see #minus(double)
     */
    public static TsData subtract(double d, final TsData ts) {
        return ts.plus(-d);
    }

    /**
     * Returns the subtraction of two time series. The time series must have the
     * same frequency, but not necessary the same time domain. The difference is
     * computed on the common time domain.
     *
     * @param tsl The left operand
     * @param tsr The right operand
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #plus(TsData)
     */
    public static TsData subtract(final TsData tsl, final TsData tsr) {
        if (tsr == null) {
            return tsl;
        } else if (tsl == null) {
            return tsr.chs();
        }

        return computeOnIntersection(tsl, tsr, (a, b) -> a - b);
    }

    private static final IRandomNumberGenerator RNG = JdkRNG.newRandom();

    /**
     * Creates a random time series
     *
     * @param freq The frequency of the series.
     * @return A time series with a random length (<600 observations), a random
     * starting period (between 1970 and 1990) and random observations is
     * generated
     */
    public static TsData random(TsFrequency freq) {
        int beg = RNG.nextInt(240);
        int count = RNG.nextInt(600);
        TsData ts = new TsData(freq, beg, count);
        double cur = RNG.nextDouble() + 100;
        for (int i = 0; i < ts.getLength(); ++i) {
            cur = cur + RNG.nextDouble() - .5;
            ts.set(i, cur);
        }
        return ts;
    }

    /**
     * Creates a random time series
     *
     * @param freq The frequency of the series.
     * @param seed
     * @return A time series with a random length (<600 observations), a random
     * starting period (between 1970 and 1990) and random observations is
     * generated
     */
    public static TsData random(TsFrequency freq, int seed) {
        Random rnd = new Random(seed);
        int beg = rnd.nextInt(240);
        int count = rnd.nextInt(600);
        TsData ts = new TsData(freq, beg, count);
        double cur = rnd.nextDouble() + 100;
        for (int i = 0; i < ts.getLength(); ++i) {
            cur = cur + rnd.nextDouble() - .5;
            ts.set(i, cur);
        }
        return ts;
    }

    public void randomAirline() {
        SarimaModelBuilder sb = new SarimaModelBuilder();
        SarimaModel airline = sb.createAirlineModel(this.getFrequency().intValue(), -.6, -.8);
        airline = sb.randomize(airline, .2);
        vals = new ArimaModelBuilder().generate(airline, vals.length);
    }

    /**
     * Computes the average difference between to time series. It is defined as
     * the root mean square of their differences.
     *
     * @param s The comparing time series
     * @return
     */
    public double distance(TsData s) {
        TsData del = this.minus(s);
        int n = del.getObsCount();
        double ssq = del.ssq();
        return Math.sqrt(ssq / n);
    }

    private TsPeriod start;
    private double[] vals;

    /**
     * Creates a new time series with the specified domain. All values are
     * Missing.
     *
     * @param dom The time domain of the series
     */
    public TsData(final TsDomain dom) {
        start = dom.getStart();
        vals = new double[dom.getLength()];
        Arrays.fill(vals, Double.NaN);
    }

    public TsData(final TsDomain dom, double val) {
        start = dom.getStart();
        vals = new double[dom.getLength()];
        for (int i = 0; i < vals.length; ++i) {
            vals[i] = val;
        }
    }

    /**
     * Creates a new time series. All the data are missing
     *
     * @param freq Frequency of the time series
     * @param beg identifier of the starting period
     * @param count Length of the series
     */
    TsData(final TsFrequency freq, final int beg, final int count) {
        start = new TsPeriod(freq, beg);
        vals = new double[count];
    }

    /**
     * Creates a new time series for a given array of observations.
     *
     * @param freq The frequency of the series
     * @param firstyear Year of the first period . .
     * @param firstperiod 0-based Position of the first period in the first
     * year.
     * @param data The given observations
     * @param copydata Indicates if the observations are copied or if the array
     * is taken as is in the internal state of the object. If copydata is false,
     * users should no longer use the given data array.
     */
    public TsData(final TsFrequency freq, final int firstyear,
            final int firstperiod, final double[] data, boolean copydata) {
        start = new TsPeriod(freq, firstyear, firstperiod);
        vals = copydata ? data.clone() : data;
    }

    /**
     * Creates a new time series. All values are Missing.
     *
     * @param freq The frequency of the series
     * @param firstyear Year of the first period . .
     * @param firstperiod 0-based Position of the first period in the first
     * year.
     * @param count Number of periods. A positive integer. It is not checked
     * that all the periods are in the accepted range [1000, 3000].
     */
    public TsData(final TsFrequency freq, final int firstyear,
            final int firstperiod, final int count) {
        start = new TsPeriod(freq, firstyear, firstperiod);
        vals = new double[count];
    }

    /**
     * Creates a new time series for a given array of observations.
     *
     * @param start The starting period. The given object is copied.
     * @param data The given observations
     * @param copydata Indicates if the observations are copied or if the array
     * is taken as is in the internal state of the object. If copydata is false,
     * users should no longer use the given data array.
     */
    public TsData(final TsPeriod start, final double[] data, boolean copydata) {
        this.start = start.clone();
        vals = copydata ? data.clone() : data;
    }

    /**
     * Creates a new time series starting from the specified period and with a
     * given length. All values are Missing.
     *
     * @param start The starting period. The given object is copied.
     * @param count The length of the series
     */
    public TsData(final TsPeriod start, final int count) {
        this.start = start.clone();
        vals = new double[count];
    }

    /**
     * Creates a new series from a starting period and a block of data. The data
     * are copied in a new internal buffer.
     *
     * @param start The starting period
     * @param vals The read only data
     */
    public TsData(final TsPeriod start, final IReadDataBlock vals) {
        this.start = start;
        this.vals = new double[vals.getLength()];
        vals.copyTo(this.vals, 0);
    }

    @Override
    public void copyTo(double[] buffer, int start) {
        System.arraycopy(vals, 0, buffer, start, vals.length);
    }

    @Override
    public IReadDataBlock rextract(int start, int length) {
        return new ReadDataBlock(vals, start, length);
    }

    /**
     * Returns a new series containing the absolute values of the current
     * observations.
     *
     * @return A new time series (T) is returned. T[i] = |S[i]|, where S is the
     * current series.
     */
    public TsData abs() {
        return transformFinite(x -> Math.abs(x));
    }

    /**
     * Makes a frequency change of this series.
     *
     * @param newfreq The new frequency. Must be la divisor of the present
     * frequency.
     * @param conversion Aggregation mode.
     * @param complete If true, the observation for a given period in the new
     * series is set to Missing if some data in the original series are Missing.
     * @return A new time series is returned.
     */
    public TsData changeFrequency(final TsFrequency newfreq,
            final TsAggregationType conversion, final boolean complete) {
        int freq = start.getFrequency().intValue(), nfreq = newfreq.intValue();
        if (freq % nfreq != 0) {
            return null;
        }
        if (freq == nfreq) {
            return clone();
        }
        int nconv = freq / nfreq;
        int c = getLength();
        int z0 = 0;
        int beg = start.id();

        // d0 and d1
        int nbeg = beg / nconv;
        // nbeg is the first period in the new frequency
        // z0 is the number of periods in the old frequency being dropped
        int n0 = nconv, n1 = nconv;
        if (beg % nconv != 0) {
            if (complete) {
                // Attention! Different treatment if beg is negative 
                // We always have that x = x/q + x%q
                // but the integer division is rounded towards 0
                if (beg > 0) {
                    ++nbeg;
                    z0 = nconv - beg % nconv;
                } else {
                    z0 = -beg % nconv;
                }
            } else {
                if (beg < 0) {
                    --nbeg;
                }
                n0 = (nbeg + 1) * nconv - beg;
            }
        }

        int end = beg + c; // excluded
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
        TsData tmp = new TsData(newfreq, nbeg, n);
        if (n > 0) {
            for (int i = 0, j = z0; i < n; ++i) {
                int nmax = nconv;
                if (i == 0) {
                    nmax = n0;
                } else if (i == n - 1) {
                    nmax = n1;
                }
                double d = 0;
                int ncur = 0;

                for (int k = 0; k < nmax; ++k, ++j) {
                    double dcur = vals[j];
                    if (Double.isFinite(dcur)) {
                        switch (conversion) {
                            case Last:
                                d = dcur;
                                break;
                            case First:
                                if (ncur == 0) {
                                    d = dcur;
                                }
                                break;
                            case Min:
                                if ((ncur == 0) || (dcur < d)) {
                                    d = dcur;
                                }
                                break;
                            case Max:
                                if ((ncur == 0) || (dcur > d)) {
                                    d = dcur;
                                }
                                break;
                            default:
                                d += dcur;
                                break;
                        }
                        ++ncur;
                    }
                }
                if ((ncur == nconv) || (!complete && (ncur != 0))) {
                    if (conversion == TsAggregationType.Average) {
                        d /= ncur;
                    }
                    tmp.vals[i] = d;
                }
            }
        }
        return tmp;
    }

    /**
     * Returns a new series containing the opposite values of the current
     * observations.
     *
     * @return A new time series (T) is returned. T[i] = -S[i], where S is the
     * current series.
     */
    public TsData chs() {
        return transformFinite(x -> -x);
    }

    /**
     * Erases the Missing values at the extremities of this series.
     *
     * @return A new series is returned.
     */
    public TsData cleanExtremities() {
        int n = vals.length, nm = getMissingValuesCount();
        if (n == nm) {
            return drop(0, n);
        }
        int nf = 0, nl = 0;
        while (nf < n) {
            if (Double.isFinite(vals[nf])) {
                break;
            }
            ++nf;
        }
        while (nl < n) {
            if (Double.isFinite(vals[n - nl - 1])) {
                break;
            }
            ++nl;
        }
        return drop(nf, nl);
    }

    @Override
    public TsData clone() {
        try {
            TsData data = (TsData) super.clone();
            data.start = start.clone();
            data.vals = vals.clone();
            return data;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     * Returns the differenced time series. The filter (1 - B^lag) is applied to
     * this series. y(t) = x(t) - x(t-lag).
     *
     * @param lag The lag of the differencing operator.
     * @return A new time series is returned. May be empty, but not null.
     */
    public TsData delta(final int lag) {
        return autoTransform(lag, (x1, x0) -> x1 - x0);
    }

    /**
     * Returns the differenced time series (at a given power). The filter (1 -
     * B^lag)**power is applied to this series.
     *
     * @param lag The lag of the differencing operator.
     * @param power The number of times the differencing operator is applied.
     * @return A new time series is returned. May be empty, but not null.
     */
    public TsData delta(final int lag, final int power) {
        if (power == 0) {
            return clone();
        }
        if ((power < 0) || (lag < 1)) {
            return null;
        }
        if (power == 1) {
            return delta(lag);
        }
        TsData tmp = this;
        for (int i = 0; i < power; ++i) {
            tmp = tmp.delta(lag);
        }
        return tmp;
    }

    /**
     * Returns the product of the series with a given value.
     *
     * @param d The multiplier
     * @return A new time series is returned. T[i] = S[i] * d, where S is the
     * current series.
     * @see #multiply(double, TsData)
     */
    public TsData div(final double d) {
        if (d == 1) {
            return clone();
        } else if (d == -1) {
            return chs();
        } else if (d == 0) {
            return new TsData(new TsDomain(start, vals.length), Double.NaN);
        }
        return transformFinite(x -> x / d);
    }

    /**
     * Returns the division of two time series. The time series must have the
     * same frequency, but not necessary the same time domain. The division is
     * computed on the common time domain.
     *
     * @param ts The added time series
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #divide(TsData, TsData)
     */
    public TsData div(final TsData ts) {
        return divide(this, ts);
    }

    /**
     * Shortens/lengthens the times series, by removing/adding observations at
     * the beginning and/or at the d1.
     *
     * @param nfirst Number of periods to drop at the beginning of the series.
     * If nfirst < 0, -nfirst periods are added (with Missing values). @param
     * nlast Number of peri ods to drop at th e d1 of the series. If nlast < 0,
     * -nlast periods are added (with Missing values). @return The returned time
     * series may be empty, but the returne d value is never null. @see
     * #extend(int, int) @param nlast @return
     */
    public TsData drop(final int nfirst, final int nlast) {
        TsPeriod s = getStart();
        s.move(nfirst);
        int n = vals.length - nfirst - nlast;
        if (n < 0) {
            n = 0;
        }
        TsData nts = new TsData(s, n);
        if (n == 0) {
            return nts;
        }
        int nc = n;
        int s0, t0;
        if (nfirst < 0) {
            nc += nfirst;
            t0 = -nfirst;
            s0 = 0;
            Arrays.fill(nts.vals, 0, -nfirst, Double.NaN);
        } else {
            s0 = nfirst;
            t0 = 0;
        }
        if (nlast < 0) {
            nc += nlast;
            Arrays.fill(nts.vals, nts.vals.length + nlast, nts.vals.length, Double.NaN);
        }
        if (nc > 0) {
            System.arraycopy(vals, s0, nts.vals, t0, nc);
        }
        return nts;
    }

    public TsData fullYears() {
        int pos = start.getPosition();
        int beg = pos > 0 ? (this.start.getFrequency().intValue() - pos) : 0;

        return drop(beg, this.getEnd().getPosition());
    }

    /**
     * Returns a new series containing the exponentials of the current
     * observations.
     *
     * @return A new time series (T) is returned. T[i] = exp(S[i]), where S is
     * the current series. Missing observations can be generated when the
     * original values are too high
     */
    public TsData exp() {
        return transformFinite(x -> Math.exp(x));
    }

    public TsData round(int ndec) {
        return transformFinite(x -> IReadDataBlock.round(x, ndec));
    }

    /**
     * Lengthens/shortens the times series, by adding/removing observations at
     * the beginning and/or at the d1.
     *
     * @param nbefore Number of periods to add (with Missing values) at the
     * beginning of the series. If nbefore < 0, -nbefore periods are removed .
     * @param nafter Number of per
     * iods to add (with Missing values) at the d1 of the series. If nafter < 0,
     * -nafter periods are removed. @return The returned time series may be
     * empty, but the returned value is never null. @see #extend(int, int)
     */
    public TsData extend(final int nbefore, final int nafter) {
        return drop(-nbefore, -nafter);
    }

    public TsData extendTo(final Day lastday) {
        TsPeriod s = new TsPeriod(start.getFrequency(), lastday);
        if (!lastday.equals(s.lastday())) {
            s.move(-1);
        }
        int n = s.minus(start) + 1;
        return extend(0, n - vals.length);
    }

    /**
     * Extends the series to the specified domain. Missing values are added (or
     * some values are removed if necessary).
     *
     * @param dom The domain of the new series. Must have the same frequency
     * than the original series.
     * @return A new (possibly empty) series is returned (or null if the domain
     * hasn't the right frequency.
     */
    public TsData fittoDomain(final TsDomain dom) {
        // if (dom == null)
        // throw new ArgumentNullException("dom");
        TsFrequency freq = start.getFrequency();
        if (dom.getFrequency() != freq) {
            return null;
        }
        int firstid = dom.firstid();
        int n = dom.getLength();
        int beg = start.id();
        int count = vals.length;
        return extend(beg - firstid, firstid + n - beg - count);
    }

    /**
     * Checks that a given observation is missing
     *
     * @param idx 0-based position of the series. Should be in the range [0,
     * getLength()[)
     * @return True if the idx-th observation is missing, false otherwise.
     */
    public boolean isMissing(final int idx) {
        return !Double.isFinite(vals[idx]);
    }

    /**
     * Sets the idx-th observation to missing. Equivalent to set(idx,
     * Double.NaN)
     *
     * @param idx 0-based position of the series. Should be in the range [0,
     * getLength()[)
     */
    public void setMissing(final int idx) {
        vals[idx] = Double.NaN;
    }

    /**
     * Gets an observation of the series
     *
     * @param idx 0-based position of the series. Should be in the range [0,
     * getLength()[)
     * @return The idx-th observation.
     */
    @Override
    public double get(final int idx) {
        return vals[idx];
    }

    /**
     * Gets the time domain of the series
     *
     * @return The domain of the series. Copy of the internal state. Can be
     * modified
     */
    @NewObject
    public TsDomain getDomain() {
        return new TsDomain(start, vals.length);
    }

    @Deprecated
    public Values getValues() {
        return new Values(vals, false);
    }

    /**
     * Gets the first period after the d1 of the series. That period doesn't
     * belong to the time domain.
     *
     * @return The first period after the d1 of the series (=d0 + length). New
     * object that can be modified.
     */
    @NewObject
    public TsPeriod getEnd() {
        return start.plus(vals.length);
    }

    /**
     * Gets the frequency of the series.
     *
     * @return The frequency.
     */
    public TsFrequency getFrequency() {
        return start.getFrequency();
    }

    /**
     * Gets the last period of the series. That period belongs to the time
     * domain.
     *
     * @return The last period of the series (=d0 + length-1) = d1 - 1). New
     * object that can be modified.
     */
    @NewObject
    public TsPeriod getLastPeriod() {
        return start.plus(vals.length - 1);
    }

    /**
     * Gets the length of the series.
     *
     * @return The length of the series. Can be 0.
     */
    @Override
    public int getLength() {
        return vals.length;
    }

    // TSObservations...
    /**
     * Number of actual (non missing) observations.
     *
     * @return The number of observations. Belongs to [0, getLength()]
     */
    public int getObsCount() {
        return count(x -> Double.isFinite(x));
    }

    public int getMissingValuesCount() {
        return count(x -> !Double.isFinite(x));
    }

    public boolean hasMissingValues() {
        return !check(x -> Double.isFinite(x));
    }

    /**
     * Gets the first period of the series
     *
     * @return The starting period of the series. Copy of the internal state.
     * Can be modified
     */
    @NewObject
    public TsPeriod getStart() {
        return start.clone();
    }

    /**
     * Returns an index calculated on this series.
     *
     * @param refperiod Period of reference.
     * @param refvalue Average value of the index on the reference period
     * (usually 100).
     * @return A new series is returned. The average of the new series on that
     * period equals the refvalue. null is returned if no period of this series
     * is inside the refperiod.
     */
    public TsData index(final TsPeriod refperiod, final double refvalue) {
        Day d0 = refperiod.firstday(), d1 = refperiod.lastday();
        TsDomain dom = getDomain();
        int i0 = dom.search(d0), i1 = dom.search(d1);

        if (i0 < 0) {
            i0 = -1 - i0;
        }
        if (i1 < 0) {
            i1 = -i1; // pï¿½riode suivante
        }
        int n = dom.getLength();
        if (i1 >= n) {
            i1 = n - 1;
        }
        if (i0 > i1) {
            return null;
        }
        double s = 0;
        n = 0;
        for (int i = i0; i <= i1; ++i) {
            double d = vals[i];
            if (Double.isFinite(d)) {
                s += d;
                ++n;
            }
        }
        if (s == 0) {
            return null;
        }

        return this.times(refvalue * n / s);
    }

    /**
     * Returns a new series containing the inverse values of the current
     * observations.
     *
     * @return A new time series (T) is returned. T[i] = 1/S[i], where S is the
     * current series. Missing observations are generated when the original
     * values are 0.
     */
    public TsData inv() {
        return transformFinite(x -> x == 0 ? Double.NaN : 1 / x);
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return vals.length == 0;
    }

    /**
     * Returns an iterator on the observations (non missing values)
     *
     * @return The iterator.
     */
    @Override
    public Iterator<TsObservation> iterator() {
        return new TsIterator(this);
    }

    public Stream<TsObservation> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns this time series lagged by a given number of period.
     *
     * @param nperiods The number of periods. If nperiods is negative, the
     * operation correspond to lead.
     * @return A new time series is returned.
     * @see #lead(int)
     */
    public TsData lag(int nperiods) {
        TsPeriod s = getStart();
        s.move(-nperiods);
        return new TsData(s, vals, true);
    }

    /**
     * Returns this time series led by a given number of period.
     *
     * @param nperiods The number of periods. If that number is negative, the
     * operation correspond to lag.
     * @return A new time series is returned.
     * @see #lag(int)
     */
    public TsData lead(final int nperiods) {
        TsPeriod s = getStart();
        s.move(nperiods);
        return new TsData(s, vals, true);
    }

    /**
     * Returns a new series containing the logs of the current observations.
     *
     * @return A new time series (T) is returned. T[i] = log(S[i]), where S is
     * the current series. Missing observations are generated when the original
     * values are <=0.
     */
    public TsData log() {
        return transformFinite(x -> Math.log(x));
    }

    /**
     * Returns a new series containing the logs of the current observations in a
     * given base.
     *
     * @param b The base for the log. A strictly positive double.
     * @return A new time series (T) is returned. T[i] = log b (S[i]), where S
     * is the current series and b is the base of the log. Missing observations
     * are generated when the original values are <=0.
     */
    public TsData log(final double b) {
        final double c = Math.log(b);
        return transformFinite(x -> Math.log(x) / c);
    }

    /**
     * Returns a new series containing the square roots of the current
     * observations.
     *
     * @return A new time series (T) is returned. T[i] = sqrt(S[i]), where S is
     * the current series. Missing observations are generated when the original
     * values are <0.
     */
    public TsData sqrt() {
        return transformFinite(x -> Math.sqrt(x));
    }

    /**
     * Returns the difference of the series with a given value.
     *
     * @param d The subtracted value
     * @return A new time series is returned. T[i] = S[i] - d, where S is the
     * current series.
     * @see #subtract(double, TsData)
     */
    public TsData minus(final double d) {
        if (d == 0) {
            return clone();
        }
        return transformFinite(x -> x - d);
    }

    /**
     * Returns the difference of two time series. The time series must have the
     * same frequency, but not necessary the same time domain. The difference is
     * computed on the common time domain.
     *
     * @param ts The added time series
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #subtract(TsData, TsData)
     */
    public TsData minus(final TsData ts) {
        return subtract(this, ts);
    }

    /**
     * Returns a moving average of this time series.
     *
     * @param weights array of double.
     * @param bcentred true if the moving average is centred.
     * @param bnormalized if true, the sum of the weights is set to 1 (they are
     * divided by their sum, if it is not equal to 0).
     * @return A new time series is returned. If the length of weight is lower
     * than 2, null is returned. If the moving average is centred and if the
     * number of weights is even, a null is also returned.
     */
    public TsData movingAverage(final double[] weights, final boolean bcentred,
            final boolean bnormalized) {
        double[] w = weights.clone();
        int nw = w.length;
        if (nw < 2 || (bcentred && nw % 2 == 0)) {
            return null;
        }
        if (bnormalized) {
            double s = 0;
            for (int i = 0; i < nw; ++i) {
                s += w[i];
            }
            if (s == 0) {
                return null;
            }
            for (int i = 0; i < nw; ++i) {
                w[i] /= s;
            }
        }

        TsDomain dout = null;
        TsDomain dom = getDomain();

        if (bcentred) {
            int nw2 = (nw - 1) / 2;
            dout = dom.drop(nw - 1 - nw2, nw2);
        } else {
            dout = dom.drop(nw - 1, 0);
        }

        TsData rslt = new TsData(dout);
        int n = dout.getLength();
        if (n == 0) {
            return rslt;
        }
        for (int i = 0; i < n; ++i) {
            double wval = 0;
            int j = 0;
            for (; j < nw; ++j) {
                double tmp = vals[i + j];
                if (Double.isFinite(tmp)) {
                    wval += w[j] * tmp;
                }
            }
            if (j == nw) {
                rslt.vals[i] = wval;
            }
        }
        return rslt;
    }

    /**
     * Returns a moving median of this time series. When the length of the
     * moving median is even, the smoothed value is the average of the two
     * central (sorted) values.
     *
     * @param nperiods Number of periods to take into account for the
     * calculation of the median.
     * @param bcentred true if the moving median is centred.
     * @return A new time series is returned. If the length of moving median is
     * lower than 2, null is returned. If the moving median is centred and if is
     * length is even, a null is also returned.
     */
    public TsData movingMedian(final int nperiods, final boolean bcentred) {
        if (nperiods < 2 || (bcentred && nperiods % 2 == 0)) {
            return null;
        }
        int np2 = (nperiods - 1) / 2;
        TsDomain dout = null;
        TsDomain dom = getDomain();
        if (bcentred) {
            dout = dom.drop(nperiods - 1 - np2, np2);
        } else {
            dout = dom.drop(nperiods - 1, 0);
        }

        TsData rslt = new TsData(dout);
        int n = dout.getLength();
        if (n == 0) {
            return rslt;
        }
        double[] tmp = new double[nperiods];
        boolean bPair = (nperiods % 2) == 0;
        for (int i = 0; i < n; ++i) {
            boolean bmissing = false;
            for (int j = 0; j < nperiods; ++j) {
                double x = vals[i + j];
                if (!Double.isFinite(x)) {
                    bmissing = true;
                    break;
                }
                tmp[j] = x;
            }
            if (!bmissing) {
                Arrays.sort(tmp);
                if (bPair) // moyenne des 2 valeurs centrales
                {
                    rslt.vals[i] = (tmp[np2] + tmp[np2 + 1]) / 2;
                } else {
                    rslt.vals[i] = tmp[np2];
                }
            }
        }
        return rslt;
    }

    /**
     * Returns the percentages of variation of this time series. y(t) = ( x(t) /
     * x(t-lag) - 1)* 100. *
     *
     * @param lag The lag of the differences.
     * @return A new time series is returned. May be empty, but not null.
     */
    public TsData pctVariation(final int lag) {
        return autoTransform(lag, (x1, x0) -> (x1 / x0 - 1) * 100);
    }

    /**
     * Returns the sum of the series with a given value.
     *
     * @param d The added value
     * @return A new time series is returned. T[i] = S[i] + d, where S is the
     * current series.
     * @see #add(double,TsData)
     */
    public TsData plus(final double d) {
        return transformFinite(x -> x + d);
    }

    /**
     * Returns the sum of two time series. The time series must have the same
     * frequency, but not necessary the same time domain. The sum is computed on
     * the common time domain.
     *
     * @param ts The added time series
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #add(TsData, TsData)
     */
    public TsData plus(final TsData ts) {
        return add(this, ts);
    }

    /**
     * Returns a new series containing the power of the current observations for
     * a given base.
     *
     * @param e
     * @return A new time series (T) is returned. T[i] = a ** (S[i]), where S is
     * the current series and b is the base of the log. Missing observations can
     * be generated when the original values are too high
     */
    public TsData pow(final double e) {
        if (e == 1) {
            return clone();
        } else if (e == 2) {
            return transformFinite(x -> x * x);
        } else {
            return transformFinite(x -> Math.pow(x, e));
        }
    }

    /**
     * Selects a part in the current series.
     *
     * @param ps The period selector.
     * @return The series corresponding to the selection. A new object is always
     * returned.
     */
    public TsData select(final TsPeriodSelector ps) {
        if (ps == null) {
            return clone();
        }
        TsDomain domain = getDomain().select(ps);
        TsData rslt = new TsData(domain);
        int diff = domain.firstid() - start.id();
        System.arraycopy(vals, diff, rslt.vals, 0, domain.getLength());
        return rslt;
    }

    /**
     * Sets an observation of the series
     *
     * @param idx 0-based position of the series. Should be in the range [0,
     * getLength()[)
     * @param value The new observation (or Double.NaN for a missing value).
     */
    public void set(final int idx, final double value) {
        vals[idx] = value;
    }

    /**
     * Returns the product of the series with a given value.
     *
     * @param d The multiplier
     * @return A new time series is returned. T[i] = S[i] * d, where S is the
     * current series.
     * @see #multiply(double, TsData)
     */
    public TsData times(final double d) {
        if (d == 0) {
            return new TsData(start, vals.length);
        }
        TsData s = clone();
        if (d == 1) {
            return s;
        }
        s.applyOnFinite(x -> x * d);
        return s;
    }

    /**
     * Returns the product of two time series. The time series must have the
     * same frequency, but not necessary the same time domain. The product is
     * computed on the common time domain.
     *
     * @param ts The added time series
     * @return A new time series, defined on the common time domain, is
     * returned. When the frequencies are not the same, null is returned. When
     * the frequencies are identical and the common time domain is empty, a new
     * empty (length=0) time series is returned.
     * @see #amultiply(TsData, TsData)
     */
    public TsData times(final TsData ts) {
        return multiply(this, ts);
    }

    /**
     * Updates this series with the specified time series. The new series has as
     * domain the union of both domains.
     *
     * @param ts The updating series.
     * @return The updated series. Null if the series don't have the same
     * frequency.
     */
    public TsData update(final TsData ts) {
        if (ts == null) {
            return clone();
        }
        TsDomain dom = getDomain(), rdom = ts.getDomain();
        TsDomain uDomain = dom.union(rdom);
        if (uDomain == null) {
            return null;
        }
        int rn = rdom.getLength();
        int r0 = rdom.firstid(), u0 = uDomain.firstid();

        TsData rslt = fittoDomain(uDomain);
        int d0 = r0 - u0;
        System.arraycopy(ts.vals, 0, rslt.vals, d0, rn);
        return rslt;
    }

    public static TsData concatenate(TsData l, TsData r) {
        if (l == null) {
            return r;
        }
        if (r == null) {
            return l;
        }
        return l.update(r);
    }

    /**
     * Removes the mean of this time series ts=ts-m
     */
    public void removeMean() {
        DescriptiveStatistics stats = new DescriptiveStatistics(vals);
        double m = stats.getAverage();
        applyOnFinite(x -> x - m);
    }

    /**
     * Normalises this time series: ts=(ts-m)/stdev
     */
    public void normalize() {
        DescriptiveStatistics stats = new DescriptiveStatistics(vals);
        double m = stats.getAverage();
        double e = stats.getStdev();
        applyOnFinite(x -> (x - m) / e);
    }

    /**
     * *
     * Gets the data corresponding to a given period. The period should have the
     * same frequency of the time series, otherwise an exception will be thrown.
     *
     * @param period The considered period.
     * @return The corresponding data or Nan if the period doesn't belong to
     * this time series
     */
    public double get(TsPeriod period) {
        int pos = period.minus(start);
        return (pos < 0 || pos >= vals.length) ? Double.NaN : vals[pos];
    }

    /**
     * *
     * Sets the data corresponding to a given period. The period should have the
     * same frequency of the time series and must belong to the domain of the
     * series otherwise an exception will be thrown.
     *
     * @param period The considered period.
     * @param value The new observation.
     */
    public void set(TsPeriod period, double value) {
        int pos = period.minus(start);
        vals[pos] = value;
    }

    @Unsafe
    public double[] internalStorage() {
        return vals;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TsObservation obs : this) {
            builder.append(obs.getPeriod()).append('\t').append(obs.getValue());
            builder.append("\r\n");
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.start != null ? this.start.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsData && equals((TsData) obj));
    }

    public boolean equals(TsData other) {
        if (!start.equals(other.start)) {
            return false;
        }
        return Arrays.equals(vals, other.vals);
    }

    //<editor-fold defaultstate="collapsed" desc="functional methods">
    @Override
    public double computeRecursively(final double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        for (int i = 0; i < vals.length; i++) {
            cur = fn.applyAsDouble(cur, vals[i]);
        }
        return cur;
    }

    public void apply(DoubleUnaryOperator fn) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = fn.applyAsDouble(vals[i]);
        }
    }

    public void applyIf(DoublePredicate pred, DoubleUnaryOperator fn) {
        for (int i = 0; i < vals.length; i++) {
            double cur = vals[i];
            if (pred.test(cur)) {
                vals[i] = fn.applyAsDouble(cur);
            }
        }
    }

    public void applyOnFinite(DoubleUnaryOperator fn) {
        applyIf(x -> Double.isFinite(x), fn);
    }

    public void applyRecursively(final double initial, DoubleBinaryOperator fn) {
        double cur = initial;
        for (int i = 0; i < vals.length; i++) {
            cur = fn.applyAsDouble(cur, vals[i]);
            vals[i] = cur;
        }
    }

    @Override
    public boolean check(DoublePredicate pred) {
        for (int i = 0; i < vals.length; i++) {
            if (!pred.test(vals[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int count(DoublePredicate pred) {
        int n = 0;
        for (int i = 0; i < vals.length; i++) {
            if (pred.test(vals[i])) {
                n++;
            }
        }
        return n;
    }

    @Override
    public int first(DoublePredicate pred) {
        for (int i = 0; i < vals.length; i++) {
            if (pred.test(vals[i])) {
                return i;
            }
        }
        return vals.length;
    }

    @Override
    public int last(DoublePredicate pred) {
        for (int i = 0; i < vals.length; i++) {
            if (pred.test(vals[i])) {
                return i;
            }
        }
        return -1;
    }

    public TsData transform(DoubleUnaryOperator fn) {
        TsData ns = new TsData(start, vals.length);
        for (int i = 0; i < vals.length; i++) {
            ns.vals[i] = fn.applyAsDouble(vals[i]);
        }
        return ns;
    }

    public TsData transformFinite(DoubleUnaryOperator fn) {
        TsData ns = new TsData(start, vals.length);
        for (int i = 0; i < vals.length; i++) {
            double cur = vals[i];
            if (Double.isFinite(cur)) {
                ns.vals[i] = fn.applyAsDouble(cur);
            } else {
                ns.vals[i] = Double.NaN;
            }
        }
        return ns;
    }

    public TsData autoTransform(int lag, DoubleBinaryOperator fn) {
        int n = vals.length - lag;
        if (n <= 0) {
            return null;
        }
        TsData ns = new TsData(start.plus(lag), n);
        for (int i = 0; i < n; i++) {
            double d0 = vals[i], d1 = vals[i + lag];
            if (Double.isFinite(d0) && Double.isFinite(d1)) {
                ns.vals[i] = fn.applyAsDouble(d1, d0);
            } else {
                ns.vals[i] = Double.NaN;
            }
        }
        return ns;
    }

    public void apply(IReadDataBlock x, DoubleBinaryOperator fn) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = fn.applyAsDouble(vals[i], x.get(i));
        }
    }

    public void applyOnFinite(IReadDataBlock x, DoubleBinaryOperator fn) {
        for (int i = 0; i < vals.length; i++) {
            double cur = vals[i];
            if (Double.isFinite(cur)) {
                vals[i] = fn.applyAsDouble(cur, x.get(i));
            }
        }
    }

    public void set(DoubleSupplier fn) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = fn.getAsDouble();
        }
    }

    public void setIf(DoublePredicate pred, DoubleSupplier fn) {
        for (int i = 0; i < vals.length; i++) {
            if (pred.test(vals[i])) {
                vals[i] = fn.getAsDouble();
            }
        }
    }

    public void set(IntToDoubleFunction fn) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = fn.applyAsDouble(i);
        }
    }

    public void set(IReadDataBlock x, DoubleUnaryOperator fn) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = fn.applyAsDouble(x.get(i));
        }
    }

    public void set(IReadDataBlock x, IReadDataBlock y, DoubleBinaryOperator fn) {
        for (int i = 0; i < vals.length; i++) {
            vals[i] = fn.applyAsDouble(x.get(i), y.get(i));
        }
    }

    public static TsData computeOnIntersection(final TsData tsl, final TsData tsr, final DoubleBinaryOperator fn) {

        TsDomain rDomain = tsr.getDomain();
        TsDomain lDomain = tsl.getDomain();
        TsDomain iDomain = lDomain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }
        int ni = iDomain.getLength();
        TsData rslt = new TsData(iDomain);
        if (ni == 0) {
            return rslt;
        }

        int rbeg = rDomain.firstid(), lbeg = tsl.start.id(), ibeg = iDomain.firstid();
        int li = ibeg - lbeg, ri = ibeg - rbeg;
        rslt.set(tsl.rextract(li, ni), tsr.rextract(ri, ni),
                (a, b) -> {
                    if (Double.isFinite(a) && Double.isFinite(b)) {
                        return fn.applyAsDouble(a, b);
                    } else {
                        return Double.NaN;
                    }
                }
        );
        return rslt;
    }

//</editor-fold>
}
