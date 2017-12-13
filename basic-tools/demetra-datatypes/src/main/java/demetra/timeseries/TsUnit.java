/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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

import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnegative;
import lombok.AccessLevel;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TsUnit implements TemporalAmount {

    @Nonnegative
    long amount;

    @lombok.NonNull
    ChronoUnit chronoUnit;

    public static TsUnit ofAnnualFrequency(int freq) {
        switch (freq) {
            case 1:
                return YEAR;
            case 2:
                return HALF_YEAR;
            case 4:
                return QUARTER;
            case 12:
                return MONTH;
        }
        if (freq % 12 == 0) {
            return new TsUnit(freq / 12, ChronoUnit.MONTHS);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean contains(TsUnit other) {
        return other.ratioOf(this) > 0;
    }

    /**
     * Returns the number of time this unit is contained in the given unit
     * @param other
     * @return 
     */
    public int ratioOf(TsUnit other) {
        double x = 1D * other.chronoUnit.getDuration().getSeconds() / chronoUnit.getDuration().getSeconds() * other.amount / amount;
        if (x < 1) {
            return NO_RATIO;
        }
        if (((int) x) != x) {
            return NO_STRICT_RATIO;
        }
        return (int) x;
    }

    @Override
    public String toString() {
        return toIsoString();
    }

    public String toIsoString() {
        switch (chronoUnit) {
            case MILLIS:
            case MINUTES:
            case HOURS:
            case MICROS:
            case SECONDS:
            case NANOS:
                return Duration.of(amount, chronoUnit).toString();
            case FOREVER:
                return "";
        }
        return Period.from(this).toString();
    }

    @Override
    public long get(TemporalUnit unit) {
        if (!this.chronoUnit.equals(unit)) {
            throw new UnsupportedTemporalTypeException(unit.toString());
        }
        return amount;
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(chronoUnit);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(amount, chronoUnit);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(amount, chronoUnit);
    }

    public static final int NO_RATIO = -1;
    public static final int NO_STRICT_RATIO = 0;

    public static final TsUnit UNDEFINED = new TsUnit(1, ChronoUnit.FOREVER);
    public static final TsUnit CENTURY = new TsUnit(100, ChronoUnit.YEARS);
    public static final TsUnit DECADE = new TsUnit(10, ChronoUnit.YEARS);
    public static final TsUnit YEAR = new TsUnit(1, ChronoUnit.YEARS);
    public static final TsUnit HALF_YEAR = new TsUnit(6, ChronoUnit.MONTHS);
    public static final TsUnit QUARTER = new TsUnit(3, ChronoUnit.MONTHS);
    public static final TsUnit MONTH = new TsUnit(1, ChronoUnit.MONTHS);
    public static final TsUnit WEEK = new TsUnit(7, ChronoUnit.DAYS);
    public static final TsUnit DAY = new TsUnit(1, ChronoUnit.DAYS);
    public static final TsUnit HOUR = new TsUnit(3600, ChronoUnit.SECONDS);
    public static final TsUnit MINUTE = new TsUnit(60, ChronoUnit.SECONDS);
    public static final TsUnit SECOND = new TsUnit(1, ChronoUnit.SECONDS);

    public static TsUnit of(long amount, ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                if (amount == 1) {
                    return YEAR;
                } else if (amount == 10) {
                    return DECADE;
                } else {
                    return new TsUnit(amount, unit);
                }
            case MONTHS:
                if (amount == 1) {
                    return MONTH;
                } else if (amount == 3) {
                    return QUARTER;
                } else if (amount == 6) {
                    return HALF_YEAR;
                } else {
                    return new TsUnit(amount, unit);
                }
            case DAYS:
                if (amount == 1) {
                    return DAY;
                } else if (amount == 7) {
                    return WEEK;
                } else {
                    return new TsUnit(amount, unit);
                }
            case HOURS:
                return amount == 1 ? HOUR : new TsUnit(amount * 3600, ChronoUnit.SECONDS);
            case MINUTES:
                return amount == 1 ? MINUTE : new TsUnit(amount * 60, ChronoUnit.SECONDS);
            case SECONDS:
                return amount == 1 ? SECOND : new TsUnit(amount, ChronoUnit.SECONDS);
            case CENTURIES:
                return new TsUnit(100 * amount, ChronoUnit.YEARS);
            case DECADES:
                return new TsUnit(10 * amount, ChronoUnit.YEARS);
            case WEEKS:
                return new TsUnit(7 * amount, ChronoUnit.DAYS);
            case HALF_DAYS:
                return new TsUnit(43200 * amount, ChronoUnit.SECONDS);
            case FOREVER:
                return UNDEFINED;
//            case MICROS: ??? Not supported for the moment ???
//            case MILLIS:
//            case NANOS:
//                return new TsUnit(amount, unit);
            default:
                throw new UnsupportedTemporalTypeException(unit.toString());
        }
    }

    public static TsUnit parse(CharSequence text) throws DateTimeParseException {
        if (text.length() == 0) {
            return UNDEFINED;
        }
        if (text.length() == 1) {
            throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
        }
        if (text.charAt(0) != 'P') {
            throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
        }
        return text.charAt(1) == 'T' ? parseTimePattern(text) : parseDatePattern(text);
    }

    private static TsUnit parseDatePattern(CharSequence text) {
        Matcher m = DATE_PATTERN.matcher(text);
        if (m.matches()) {
            int amount = Integer.parseInt(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'Y':
                    return TsUnit.of(amount, ChronoUnit.YEARS);
                case 'M':
                    return TsUnit.of(amount, ChronoUnit.MONTHS);
                case 'W':
                    return TsUnit.of(amount, ChronoUnit.WEEKS);
                case 'D':
                    return TsUnit.of(amount, ChronoUnit.DAYS);
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
    }

    private static TsUnit parseTimePattern(CharSequence text) {
        Matcher m = TIME_PATTERN.matcher(text);
        if (m.matches()) {
            double amount = Double.parseDouble(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'H':
                    return TsUnit.of((long) amount, ChronoUnit.HOURS);
                case 'M':
                    return TsUnit.of((long) amount, ChronoUnit.MINUTES);
                case 'S':
                    // NOT supported for the moment:  milli, micro, nano
                    return TsUnit.of((long) amount, ChronoUnit.SECONDS);
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("P(?:([0-9]+)([Y|M|D]))", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_PATTERN = Pattern.compile("PT(?:([0-9]+)([H|M|S]))", Pattern.CASE_INSENSITIVE);
}
