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
package demetra.timeseries.simplets;

import demetra.design.Development;
import demetra.design.Immutable;
import demetra.design.Internal;
import demetra.timeseries.IDatePeriod;
import demetra.timeseries.IRegularPeriod;
import demetra.timeseries.TsException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoUnit;

@Development(status = Development.Status.Beta)
@Immutable
@lombok.EqualsAndHashCode
public final class TsPeriod implements IDatePeriod, IRegularPeriod, Comparable<TsPeriod> {

    /**
     * Creates a new TsPeriod, having a given freq and containing a given date.
     *
     * @param frequency Frequency of the period.
     * @param date A date in the period
     * @return
     */
    public static TsPeriod of(TsFrequency frequency, LocalDate date) {
        return new TsPeriod(frequency, calcId(frequency.getAsInt(), date));
    }

    /**
     * Creates a new TsPeriod.
     *
     * @param frequency Frequency of the period
     * @param year Year of the period
     * @param position 0-based position of the period in the year
     * @return
     */
    public static TsPeriod of(TsFrequency frequency, int year, int position) {
        int ifreq = frequency.getAsInt();
        if ((position < 0) || (position >= ifreq)) {
            throw new TsException(TsException.INVALID_PERIOD);
        }
        return new TsPeriod(frequency, calcId(ifreq, year, position));
    }

    public static TsPeriod ofInternal(TsFrequency frequency, int id) {
        return new TsPeriod(frequency, id);
    }

    /**
     * *
     * Creates a period corresponding to the given year
     *
     * @param y Year
     * @return The year
     */
    public static TsPeriod year(int y) {
        return TsPeriod.of(TsFrequency.Yearly, y, 0);
    }

    private final TsFrequency freq;
    private final int id;

    TsPeriod(TsFrequency frequency, int id) {
        this.freq = frequency;
        this.id = id;
    }

    /**
     * Compare to periods. The periods can have different frequencies.
     *
     * @param other The compared period.
     * @return 0 is returned if the periods are equal. 1 is returned if the
     * second period is strictly before the current period. -1 is returned if
     * the second period is strictly after the current period.
     */
    @Override
    public int compareTo(final TsPeriod other) {
        if (other.freq == freq) {
            if (id == other.id) {
                return 0;
            } else if (id < other.id) {
                return -1;
            } else {
                return 1;
            }
        }
        throw new TsException(TsException.INCOMPATIBLE_FREQ);
    }

    /**
     * Verifies that a date belongs to the period.
     *
     * @param dt Tested date
     * @return true if the given date is inside the period, false otherwise.
     */
    @Override
    public boolean contains(final LocalDate dt) {
        return calcId(freq.getAsInt(), dt) == id;
    }

    /**
     * Returns the first day of the period
     *
     * @return The first day
     */
    @Override
    public LocalDate firstDay() {
        int ifreq = freq.getAsInt();
        int c = 12 / ifreq;
        return LocalDate.of(getYear(), 1 + getPosition() * c, 1);
    }

    /**
     * Gets the freq of the period.
     *
     * @return The freq.
     */
    public TsFrequency getFrequency() {
        return freq;
    }

    /**
     * Returns the last higher-freq period contained in this period
     *
     * @param freq The higher freq
     * @return The last period with the given freq contained in this period
     * @throws TsException if the frequencies are incompatible
     */
    public TsPeriod lastPeriod(TsFrequency freq) {
        if (freq == this.freq) {
            return this;
        }
        int c = freq.ratio(this.freq);
        return new TsPeriod(freq, this.id * c + c - 1);
    }

    /**
     * Returns the first higher-freq period contained in this period
     *
     * @param freq The higher freq
     * @return The first period with the given freq contained in this period
     * @throws TsException if the frequencies are incompatible
     */
    public TsPeriod firstPeriod(TsFrequency freq) {
        if (freq == this.freq) {
            return this;
        }
        int c = freq.ratio(this.freq);
        return new TsPeriod(freq, this.id * c);
    }

    /**
     * Gets a description (independent of the year) of the period corresponding
     * to a freq and a position.
     *
     * @return The description
     * @see #formatShortPeriod(TSFrequency, int).
     */
    public String getPeriodString() {
        return formatPeriod(freq, getPosition());
    }

    /**
     * Gets the 0-based position of the period in the year (for instance
     * February has position 1 in a monthly freq).
     *
     * @return 0-based position of the period in the year. The returned value is
     * in the range [0, getFrequency().getAsInt()[
     */
    public int getPosition() {
        int ifreq = freq.getAsInt();
        if (id >= 0) {
            return id % ifreq;
        } else {
            return ifreq - 1 + (1 + id) % ifreq;
        }
    }

    /**
     * Gets the year the period belongs to.
     *
     * @return Year of the period.
     */
    public int getYear() {
        if (id >= 0) {
            return 1970 + id / freq.getAsInt();
        } else {
            return 1969 + (1 + id) / freq.getAsInt();
        }
    }

    /**
     * Number of periods since 1/1/70 (reference period)
     *
     * @return
     */
    @Internal
    int id() {
        return id;
    }

    static int calcId(final int freq, LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue() - 1; // 0 based
        if (freq == 12) {
            return calcId(freq, year, month);
        } else {
            return calcId(freq, year, month * freq / 12);
        }
    }

    static int calcId(final int freq, final int year, final int placeinyear) {
        return (year - 1970) * freq + placeinyear;
    }

    /**
     * Checks that the period is after a given day.
     *
     * @param day The given day
     * @return true if the start of the period is strictly after the given date,
     * false otherwise
     */
    public boolean isAfter(final LocalDate day) {
        return firstDay().compareTo(day) > 0;
    }

    /**
     * Verifies that a period is after another one
     *
     * @param rp The reference period
     * @return Is equivalent to compareTo(rp) &gt 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isAfter(final TsPeriod rp) {
        return compareTo(rp) > 0;
    }

    /**
     * Checks that the period is before a given day.
     *
     * @param day The reference day.
     * @return true if the end of the period is strictly before the given day,
     * false otherwise
     */
    public boolean isBefore(final LocalDate day) {
        return lastDay().compareTo(day) < 0;
    }

    /**
     * Verifies that a period is before another one.
     *
     * @param rp The reference period
     * @return Is equivalent to compareTo(rp) &lt 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isBefore(final TsPeriod rp) {
        return compareTo(rp) < 0;
    }

    /**
     * Checks that the current period is inside a given period
     *
     * @param p The containing period.
     * @return true if the current period is inside p, else otherwise
     */
    public boolean isInside(final TsPeriod p) {
        int ifreq = freq.getAsInt(), pfreq = p.freq.getAsInt();
        if (pfreq > ifreq) {
            return false;
        }
        // express in months.
        if (ifreq == pfreq) {
            return id == p.id;
        }
        int id0 = id * 12 / ifreq;
        int id1 = p.id * 12 / pfreq;
        if (id0 < id1) {
            return false;
        }
        return id0 + 12 / ifreq <= id1 + 12 / pfreq;
    }

    /**
     * Verifies that a period is not after the second one
     *
     * @param rp The reference period
     * @return Is equivalent to compareTo(rp) &lt= 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isNotAfter(final TsPeriod rp) {
        return compareTo(rp) <= 0;
    }

    /**
     * Verifies that a period is not before the second one
     *
     * @param rp The reference period
     * @return Is equivalent to compareTo(rp) &gt= 0;
     * @see #compareTo(TsPeriod)
     */
    public boolean isNotBefore(final TsPeriod rp) {
        // if (lp == null)
        // throw new ArgumentNullException("lp");
        return compareTo(rp) >= 0;
    }

    @Override
    public LocalDate lastDay() {
        int ifreq = freq.getAsInt();
        int c = 12 / ifreq;
        int y = getYear();
        Month month = Month.of(getPosition() * c + c);
        return LocalDate.of(y, month, month.length(Year.isLeap(y)));
    }

    @Override
    public Period length() {
        return Period.between(firstDay(), lastDay().plus(1, ChronoUnit.DAYS));
    }

    public TsPeriod minus(long nperiods) {
        return new TsPeriod(freq, (int) (id - nperiods));
    }

    /**
     * Number of periods between two periods with the same freq.
     *
     * @param p
     * @return Number of period between the current Object and p. > 0 if the
     * current Object is after p, = 0 if both objects are equals, < 0 if the
     * current Object is before p. @param p The period used in the comparison.
     */
    public int minus(final TsPeriod p) {
        if (freq != p.freq) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        return id - p.id;
    }
    
    @Override
    public long until(final IRegularPeriod p){
        if (p instanceof TsPeriod){
            return ((TsPeriod)p).minus(this);
        }else
            throw new TsException(TsException.INVALID_OPERATION);
    }

    @Override
    public TsPeriod plus(long nperiods) {
        return new TsPeriod(freq, (int) (id + nperiods));
    }

   @Override
    public TsPeriod moveTo(LocalDateTime dt) {
        return of(freq, dt.toLocalDate());
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(32);
        int p = getPosition();
        int y = getYear();

        int ifreq = freq.getAsInt();

        switch (ifreq) {
            case 2:
            case 3:
            case 4: {
                switch (p) {
                    case 0:
                        buffer.append('I');
                        break;
                    case 1:
                        buffer.append("II");
                        break;
                    case 2:
                        buffer.append("III");
                        break;
                    case 3:
                        buffer.append("IV");
                        break;
                }
                buffer.append('-');
                break;
            }
            case 6:
            case 12:
                buffer.append(1 + p).append('-');
                break;

        }
        buffer.append(y);
        return buffer.toString();
    }

    /**
     * Gets a description (independent of the year) of the period corresponding
     * to a freq and to a 0-based position. For example: January.. for monthly
     * periods Q1... for quarterly periods
     *
     * @param freq The freq of the period
     * @param pos Its 0-based position in the year
     * @return The corresponding text
     */
    public static String formatPeriod(TsFrequency freq, int pos) {
        switch (freq) {
            case Monthly:
                return Month.of(pos + 1).toString();
            case Yearly:
            case Undefined:
                return "";
            default:
                StringBuilder builder = new StringBuilder();
                switch (freq) {
                    case Quarterly:
                        builder.append('Q');
                        break;
                    case HalfYearly:
                        builder.append('H');
                        break;
                    default:
                        builder.append('P');
                        break;

                }
                builder.append(pos + 1);
                return builder.toString();
        }
    }
}
