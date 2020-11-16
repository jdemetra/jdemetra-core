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

import internal.time.DefaultConverter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * @param <T>
 *
 * @author Philippe Charles
 */
public interface IsoConverter<T> {

    @NonNull
    CharSequence format(@NonNull T value);

    @NonNull
    T parse(@NonNull CharSequence text) throws DateTimeParseException;

    @NonNull
    static <T> IsoConverter<T> of(@NonNull Function<T, ? extends CharSequence> formatter, @NonNull Function<? super CharSequence, T> parser) {
        return new DefaultConverter<>(formatter, parser);
    }

    @NonNull
    static <T extends IsoRepresentable> IsoConverter<T> of(@NonNull Function<? super CharSequence, T> parser) {
        return new DefaultConverter<>(IsoRepresentable::toISO8601, parser);
    }

    IsoConverter<LocalDateTime> LOCAL_DATE_TIME = of(LocalDateTime::toString, LocalDateTime::parse);
    IsoConverter<Period> PERIOD = of(Period::toString, Period::parse);
    IsoConverter<Duration> DURATION = of(Duration::toString, Duration::parse);
}
