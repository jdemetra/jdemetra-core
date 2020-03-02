/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import demetra.data.AggregationType;
import demetra.data.DoubleSeqCursor;
import demetra.design.Development;
import internal.timeseries.InternalAggregator;
import java.util.Objects;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AccessLevel;
import demetra.data.DoubleSeq;
import demetra.data.Doubles;

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
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsData implements TimeSeriesData<TsPeriod, TsObs> {

    /**
     * Creates a random time series
     *
     * @param freq The frequency of the series.
     * @param seed
     * @return A time series with a random length (<600 observations), a random
     * starting period (between 1970 and 1990) and random observations is
     * generated
     */
    public static TsData random(TsUnit freq, int seed) {
        Random rnd = new Random(seed);
        int beg = rnd.nextInt(240);
        int count = rnd.nextInt(600);
        double[] data = new double[count];
        double cur = rnd.nextDouble() + 100;
        for (int i = 0; i < count; ++i) {
            cur = cur + rnd.nextDouble() - .5;
            data[i] = cur;
        }
        return ofInternal(TsPeriod.of(freq, beg), DoubleSeq.of(data));
    }

    /**
     * Creates a new time series from a copy of this sequence of doubles
     *
     * @param start
     * @param values
     * @return
     */
    @NonNull
    public static TsData of(@NonNull TsPeriod start, @NonNull Doubles values) {
        TsDomain domain = TsDomain.of(start, values.length());
        return domain.isEmpty()
                ? new TsData(domain, values, NO_DATA_CAUSE)
                : new TsData(domain, values, null);
    }

    @NonNull
    public static TsData ofInternal(@NonNull TsPeriod start, @NonNull DoubleSeq values) {
        TsDomain domain = TsDomain.of(start, values.length());
        return domain.isEmpty()
                ? new TsData(domain, Doubles.EMPTY, NO_DATA_CAUSE)
                : new TsData(domain, values, null);
    }

    @NonNull
    public static TsData ofInternal(@NonNull TsPeriod start, @NonNull double[] values) {
        TsDomain domain = TsDomain.of(start, values.length);
        return domain.isEmpty()
                ? new TsData(domain, Doubles.EMPTY, NO_DATA_CAUSE)
                : new TsData(domain, DoubleSeq.of(values), null);
    }

    @NonNull
    public static TsData empty(@NonNull TsPeriod start, @NonNull String cause) {
        return new TsData(TsDomain.of(start, 0), Doubles.EMPTY, Objects.requireNonNull(cause));
    }

    @NonNull
    public static TsData empty(@NonNull String cause) {
        return new TsData(TsDomain.of(TsPeriod.of(TsUnit.YEAR, 0), 0), Doubles.EMPTY, Objects.requireNonNull(cause));
    }

    private static final String NO_DATA_CAUSE = "No data available";

    private final TsDomain domain;
    private final DoubleSeq values;

    /**
     * Message explaining why the time series data is empty.
     */
    private final String cause;

    @Override
    public TsObs get(int index) throws IndexOutOfBoundsException {
        return TsObs.of(getPeriod(index), getValue(index));
    }

    /**
     * Gets the time unit of the series.
     *
     * @return The time unit.
     */
    public TsUnit getTsUnit() {
        return domain.getTsUnit();
    }

    public TsPeriod getStart() {
        return domain.getStartPeriod();
    }

    public TsPeriod getEnd() {
        return domain.getEndPeriod();
    }

    /**
     * Gets the number of periods in one year.
     *
     * @return The number of periods in 1 year or -1 if the unit is not
     * compatible with years
     */
    public int getAnnualFrequency() {
        return domain.getTsUnit().getAnnualFrequency();
    }

    public boolean hasDefaultEpoch() {
        return domain.hasDefaultEpoch();
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
    public double getDoubleValue(TsPeriod period) {
        int pos = domain.indexOf(period);
        return (pos < 0 || pos >= values.length()) ? Double.NaN : values.get(pos);
    }

    public TsData fn(DoubleUnaryOperator fn) {
        return TsData.of(getStart(), Doubles.of(values.fn(fn)));
    }

    public TsData fastFn(DoubleUnaryOperator fn) {
        return TsData.ofInternal(getStart(), DoubleSeq.onMapping(values.length(), i -> fn.applyAsDouble(values.get(i))));
    }

    public TsData commit() {
        return TsData.ofInternal(getStart(), values.toArray());
    }

    public TsData fastFn(@NonNull TsData right, DoubleBinaryOperator fn) {
        TsDomain rDomain = right.getDomain();
        TsDomain iDomain = domain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }
        TsPeriod istart = iDomain.getStartPeriod();
        int li = domain.indexOf(istart), ri = rDomain.indexOf(istart);
        return TsData.ofInternal(istart, DoubleSeq.onMapping(iDomain.length(),
                i -> fn.applyAsDouble(values.get(li + i), right.getValue(ri + i))));
    }

    public TsData fn(TsData right, DoubleBinaryOperator fn) {
        TsDomain rDomain = right.getDomain();
        TsDomain iDomain = domain.intersection(rDomain);
        if (iDomain == null) {
            return null;
        }

        TsPeriod istart = iDomain.getStartPeriod();
        int li = domain.indexOf(istart), ri = rDomain.indexOf(istart);
        double[] data = new double[iDomain.length()];
        DoubleSeqCursor lreader = values.cursor(), rreader = right.getValues().cursor();
        lreader.moveTo(li);
        rreader.moveTo(ri);
        for (int i = 0; i < data.length; ++i) {
            data[i] = fn.applyAsDouble(lreader.getAndNext(), rreader.getAndNext());
        }
        return TsData.ofInternal(istart, data);
    }

    public TsData fn(int lag, DoubleBinaryOperator fn) {
        return TsData.of(getStart().plus(lag), Doubles.of(values.fn(lag, fn)));
    }

    public TsData drop(@NonNegative int nbeg, @NonNegative int nend) {
        int len = length() - nbeg - nend;
        TsPeriod start = getStart().plus(nbeg);
        return TsData.of(start, Doubles.of(values.extract(nbeg, Math.max(0, len))));
    }

    public TsData extend(@NonNegative int nbeg, @NonNegative int nend) {
        TsPeriod start = getStart().plus(-nbeg);
        return TsData.ofInternal(start, values.extend(nbeg, nend));
    }

    public TsData select(TimeSelector selector) {
        TsDomain ndomain = domain.select(selector);
        final int beg = getStart().until(ndomain.getStartPeriod());
        return TsData.of(ndomain.getStartPeriod(), Doubles.of(values.extract(beg, ndomain.length())));
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
    public static TsData fitToDomain(TsData s, TsDomain domain) {
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
    public TsData log() {
        return fastFn(x -> Math.log(x));
    }

    public TsData exp() {
        return fastFn(x -> Math.exp(x));
    }

    public TsData inv() {
        return fastFn(x -> 1 / x);
    }

    public TsData chs() {
        return fastFn(x -> -x);
    }

    public TsData abs() {
        return fastFn(x -> Math.abs(x));
    }

    public static TsData add(TsData l, TsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return l.fn(r, (a, b) -> a + b);
        }
    }

    public TsData add(double d) {
        if (d == 0) {
            return this;
        } else {
            return fastFn(x -> x + d);
        }
    }

    public TsData subtract(double d) {
        if (d == 0) {
            return this;
        } else {
            return fastFn(x -> x - d);
        }
    }

    public double distance(TsData r) {
        DoubleSeq diff = subtract(this, r).getValues();
        int n = diff.count(x -> Double.isFinite(x));
        if (n == 0) {
            return Double.NaN;
        }
        return diff.fastNorm2();
    }

    public static TsData subtract(double d, TsData l) {
        if (d == 0) {
            return l.chs();
        } else {
            return l.fastFn(x -> d - x);
        }
    }

    public static TsData subtract(TsData l, TsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return l.fastFn(r, (a, b) -> a - b);
        }
    }

    public static TsData multiply(TsData l, TsData r) {
        if (l == null) {
            return r;
        } else if (r == null) {
            return l;
        } else {
            return l.fastFn(r, (a, b) -> a * b);
        }
    }

    public TsData multiply(double d) {
        if (d == 1) {
            return this;
        } else if (d == 0) {
            return fastFn(x -> 0);
        } else {
            return fastFn(x -> x * d);
        }
    }

    public static TsData divide(TsData l, TsData r) {
        if (l == null) {
            return r.inv();
        } else if (r == null) {
            return l;
        } else {
            return l.fastFn(r, (a, b) -> a / b);
        }
    }

    public TsData divide(double d) {
        if (d == 1) {
            return this;
        } else {
            return fastFn(x -> x / d);
        }
    }

    public static TsData divide(double d, TsData l) {
        return l.fastFn(x -> d / x);
    }

    public TsData delta(int lag) {
        return fn(lag, (x, y) -> y - x);
    }

    public TsData delta(int lag, int pow) {
        TsData ns = this;
        for (int i = 0; i < pow; ++i) {
            ns = ns.fn(lag, (x, y) -> y - x);
        }
        return ns;
    }

    public TsData pctVariation(int lag) {
        return fn(lag, (x, y) -> (y / x - 1) * 100);
    }

    public TsData normalize() {
        double[] data = values.toArray();
        DoubleSeq values = DoubleSeq.of(data);
        final double mean = values.average();
        double ssqc = values.ssqc(mean);
        final double std = Math.sqrt(ssqc / values.length());
        for (int i = 0; i < data.length; ++i) {
            data[i] = (data[i] - mean) / std;
        }
        return TsData.ofInternal(getStart(), data);
    }

    public TsData lead(@NonNegative int lead) {
        return lead == 0 ? this : TsData.ofInternal(getStart().plus(-lead), values);
    }

    public TsData lag(@NonNegative int lag) {
        return lag == 0 ? this : TsData.ofInternal(getStart().plus(lag), values);
    }

    /**
     * Erases the Missing values at the extremities of this series.
     *
     * @return A new series is returned.
     */
    public TsData cleanExtremities() {
        int n = values.length(), nm = values.count(x -> !Double.isFinite(x));
        if (n == nm) {
            return drop(0, n);
        }
        int nf = 0, nl = 0;
        while (nf < n) {
            if (Double.isFinite(values.get(nf))) {
                break;
            }
            ++nf;
        }
        while (nl < n) {
            if (Double.isFinite(values.get(n - nl - 1))) {
                break;
            }
            ++nl;
        }
        return drop(nf, nl);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "Empty due to: '" + cause + "'";
        }
        StringBuilder builder = new StringBuilder();
        DoubleSeqCursor reader = values.cursor();
        for (int i = 0; i < values.length(); ++i) {
            builder.append(domain.get(i).display()).append('\t').append(reader.getAndNext());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + (cause == null ? 0 : cause.hashCode());
        result = 31 * result + domain.hashCode();
        result = 31 * result + DoubleSeq.getHashCode(values);
        return result;
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof TsData && equals((TsData) that));
    }

    private boolean equals(TsData that) {
        return Objects.equals(this.cause, that.cause)
                && this.domain.equals(that.domain)
                && this.values.hasSameContentAs(that.values);
    }

    /**
     * Makes a frequency change of this series.
     *
     * @param newUnit The new frequency. Must be la divisor of the present
     * frequency.
     * @param conversion Aggregation mode.
     * @param complete If true, the observation for a given period in the new
     * series is set to Missing if some data in the original series are Missing.
     * @return A new time series is returned.
     */
    public TsData aggregate(@NonNull TsUnit newUnit, @NonNull AggregationType conversion, boolean complete) {
        int ratio = this.getTsUnit().ratioOf(newUnit);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            case 1:
                return this;
        }
        if (this.isEmpty()) {
            return TsData.of(this.getStart().withUnit(newUnit), Doubles.of(this.getValues()));
        }
        return changeUsingRatio(this, newUnit, InternalAggregator.of(conversion), ratio, complete);
    }

    public TsData aggregateByPosition(@NonNull TsUnit newUnit, int position) {
        int ratio = this.getTsUnit().ratioOf(newUnit);
        switch (ratio) {
            case TsUnit.NO_STRICT_RATIO:
            case TsUnit.NO_RATIO:
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            case 1:
                return this;
        }
        if (position < 0 || position >= ratio) {
            throw new IllegalArgumentException();
        }
        if (this.isEmpty()) {
            return TsData.of(this.getStart().withUnit(newUnit), Doubles.of(this.getValues()));
        }
        int oldLength = length();
        TsPeriod start = getStart(), nstart = start.withUnit(newUnit);
        int spos = TsDomain.splitOf(nstart, start.getUnit(), false).indexOf(start);
        int head = position - spos;
        if (head < 0) {
            head += ratio;
            nstart=nstart.next();
        }
        int nlength = 1 + (oldLength - head - 1) / ratio;
        return TsData.of(nstart, Doubles.of(values.extract(head, nlength, ratio)));
    }

    private static TsData changeUsingRatio(TsData s, TsUnit newUnit, InternalAggregator aggregator, int ratio, boolean complete) {
        int oldLength = s.length();
        TsPeriod start = s.getStart(), nstart = start.withUnit(newUnit);
        int spos = TsDomain.splitOf(nstart, start.getUnit(), false).indexOf(start);
        int head = spos > 0 ? ratio - spos : 0;
        int tail = (oldLength - head) % ratio;
        int body = oldLength - head - tail;
        if (complete && head > 0) {
            nstart = nstart.next();
        }
        Doubles newValues = aggregate(s.getValues(), aggregator, complete, ratio, head, body, tail);
        return TsData.of(nstart, newValues);
    }

    private static Doubles aggregate(DoubleSeq values, InternalAggregator aggregator, boolean complete, int ratio, int head, int body, int tail) {
        boolean appendHead = !complete && head > 0;
        boolean appendTail = !complete && tail > 0;

        int length = body / ratio + (appendHead ? 1 : 0) + (appendTail ? 1 : 0);

        double[] safeArray = new double[length];
        int i = 0;

        // head
        if (appendHead) {
            safeArray[i++] = aggregator.aggregate(values, 0, head);
        }
        // body
        int tailIndex = body + head;
        for (int j = head; j < tailIndex; j += ratio) {
            safeArray[i++] = aggregator.aggregate(values, j, j + ratio);
        }
        // tail
        if (appendTail) {
            safeArray[i++] = aggregator.aggregate(values, tailIndex, tailIndex + tail);
        }

        return Doubles.ofInternal(safeArray);
    }
}
