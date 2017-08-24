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
public class TsFrequency implements TemporalAmount {

    @Nonnegative
    long amount;

    @lombok.NonNull
    ChronoUnit unit;

    @Override
    public String toString() {
        return toIsoString();
    }

    public String toIsoString() {
        switch (unit) {
            case HALF_DAYS:
            case MILLIS:
            case MINUTES:
            case HOURS:
            case MICROS:
            case SECONDS:
            case NANOS:
                return Duration.of(amount, unit).toString();
        }
        return Period.from(this).toString();
    }

    @Override
    public long get(TemporalUnit unit) {
        if (!this.unit.equals(unit)) {
            throw new UnsupportedTemporalTypeException(unit.toString());
        }
        return amount;
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return Collections.singletonList(unit);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(amount, unit);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(amount, unit);
    }

    public static final TsFrequency YEARLY = new TsFrequency(1, ChronoUnit.YEARS);
    public static final TsFrequency HALF_YEARLY = new TsFrequency(6, ChronoUnit.MONTHS);
    public static final TsFrequency QUADRI_MONTHLY = new TsFrequency(4, ChronoUnit.MONTHS);
    public static final TsFrequency QUARTERLY = new TsFrequency(3, ChronoUnit.MONTHS);
    public static final TsFrequency BI_MONTHLY = new TsFrequency(2, ChronoUnit.MONTHS);
    public static final TsFrequency MONTHLY = new TsFrequency(1, ChronoUnit.MONTHS);
    public static final TsFrequency DAILY = new TsFrequency(1, ChronoUnit.DAYS);
    public static final TsFrequency HOURLY = new TsFrequency(1, ChronoUnit.HOURS);
    public static final TsFrequency MINUTELY = new TsFrequency(1, ChronoUnit.MINUTES);

    public static TsFrequency of(long amount, ChronoUnit unit) {
        switch (unit) {
            case YEARS:
                return amount == 1 ? YEARLY : new TsFrequency(amount, unit);
            case MONTHS:
                if (amount == 1) {
                    return MONTHLY;
                }
                if (amount == 2) {
                    return BI_MONTHLY;
                }
                if (amount == 3) {
                    return QUARTERLY;
                }
                if (amount == 4) {
                    return QUADRI_MONTHLY;
                }
                if (amount == 6) {
                    return HALF_YEARLY;
                }
                return new TsFrequency(amount, unit);
            case DAYS:
                return amount == 1 ? DAILY : new TsFrequency(amount, unit);
            case HOURS:
                return amount == 1 ? HOURLY : new TsFrequency(amount, unit);
            case MINUTES:
                return amount == 1 ? MINUTELY : new TsFrequency(amount, unit);
            case CENTURIES:
                return new TsFrequency(100 * amount, ChronoUnit.YEARS);
            case HALF_DAYS:
            case DECADES:
                return new TsFrequency(10 * amount, ChronoUnit.YEARS);
            case MICROS:
            case MILLIS:
            case SECONDS:
            case NANOS:
                return new TsFrequency(amount, unit);
            case WEEKS:
            default:
                throw new UnsupportedTemporalTypeException(unit.toString());
        }
    }

    public static TsFrequency parse(CharSequence text) throws DateTimeParseException {
        if (text.length() <= 1) {
            throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
        }
        if (text.charAt(0) != 'P') {
            throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
        }
        return text.charAt(1) == 'T' ? parseTimePattern(text) : parseDatePattern(text);
    }

    private static TsFrequency parseDatePattern(CharSequence text) {
        Matcher m = DATE_PATTERN.matcher(text);
        if (m.matches()) {
            int amount = Integer.parseInt(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'Y':
                    return TsFrequency.of(amount, ChronoUnit.YEARS);
                case 'M':
                    return TsFrequency.of(amount, ChronoUnit.MONTHS);
                case 'W':
                    return TsFrequency.of(amount, ChronoUnit.WEEKS);
                case 'D':
                    return TsFrequency.of(amount, ChronoUnit.DAYS);
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
    }

    private static TsFrequency parseTimePattern(CharSequence text) {
        Matcher m = TIME_PATTERN.matcher(text);
        if (m.matches()) {
            double amount = Double.parseDouble(m.group(1));
            switch (m.group(2).charAt(0)) {
                case 'H':
                    return TsFrequency.of((long) amount, ChronoUnit.HOURS);
                case 'M':
                    return TsFrequency.of((long) amount, ChronoUnit.MINUTES);
                case 'S':
                    // FIXME: milli, micro, nano
                    return TsFrequency.of((long) amount, ChronoUnit.SECONDS);
            }
        }
        throw new DateTimeParseException("Text cannot be parsed to a freq", text, 0);
    }

    private static final Pattern DATE_PATTERN = Pattern.compile("P(?:([0-9]+)([Y|M|W|D]))", Pattern.CASE_INSENSITIVE);
    private static final Pattern TIME_PATTERN = Pattern.compile("PT(?:([0-9]+)([H|M|S]))", Pattern.CASE_INSENSITIVE);
}
