/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.time;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class ISO8601 {

    public interface Representable {

        @NonNull
        String toISO8601();
    }

    public interface Converter<T> {

        @NonNull
        CharSequence format(@NonNull T value);

        @NonNull
        T parse(@NonNull CharSequence text) throws DateTimeParseException;

        @NonNull
        static <T> Converter<T> of(@NonNull Function<T, CharSequence> formatter, @NonNull Function<CharSequence, T> parser) {
            return new DefaultConverter(formatter, parser);
        }

        Converter<LocalDateTime> LOCAL_DATE_TIME = of(LocalDateTime::toString, LocalDateTime::parse);

        Converter<Period> PERIOD = of(Period::toString, Period::parse);
        Converter<Duration> DURATION = of(Duration::toString, Duration::parse);
    }

    @lombok.AllArgsConstructor
    public enum LocalDateConverter implements Converter<LocalDate> {

        LOCAL_DATE(DateTimeFormatter.ISO_DATE),
        BASIC_DATE(DateTimeFormatter.BASIC_ISO_DATE),
        ORDINAL_DATE(DateTimeFormatter.ISO_ORDINAL_DATE),
        WEEK_DATE(DateTimeFormatter.ISO_WEEK_DATE);

        private final DateTimeFormatter formatter;

        @Override
        public CharSequence format(LocalDate value) {
            return formatter.format(value);
        }

        @Override
        public LocalDate parse(CharSequence text) throws DateTimeParseException {
            return formatter.parse(text, LocalDate::from);
        }
    }

    @lombok.AllArgsConstructor
    private static final class DefaultConverter<T> implements Converter<T> {

        @lombok.NonNull
        private final Function<T, CharSequence> formatter;

        @lombok.NonNull
        private final Function<CharSequence, T> parser;

        @Override
        public CharSequence format(T value) {
            Objects.requireNonNull(value);
            return Objects.requireNonNull(formatter.apply(value));
        }

        @Override
        public T parse(CharSequence text) {
            Objects.requireNonNull(text);
            return Objects.requireNonNull(parser.apply(text));
        }
    }
}
