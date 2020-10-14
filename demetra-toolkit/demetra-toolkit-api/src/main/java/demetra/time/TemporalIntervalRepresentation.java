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

import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.function.BiFunction;

/**
 * @param <T>
 * @param <D>
 *
 * @author Philippe Charles
 */
public abstract class TemporalIntervalRepresentation<T extends Temporal & Comparable<? super T>, D extends TemporalAmount> {

    protected TemporalIntervalRepresentation() {
    }

    public abstract String format(TemporalInterval<T, D> interval);

    @lombok.AllArgsConstructor
    public static final class StartEnd<T extends Temporal & Comparable<? super T>>
            extends TemporalIntervalRepresentation<T, TemporalAmount> {

        @lombok.NonNull
        private final ISO8601.Converter<T> temporalConverter;

        @Override
        public String format(TemporalInterval<T, TemporalAmount> interval) {
            return temporalConverter.format(interval.start()) + "/" + temporalConverter.format(interval.end());
        }

        public <I extends TemporalInterval<T, ?>> I parse(CharSequence text, BiFunction<T, T, I> factory) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            T start = temporalConverter.parse(text.subSequence(0, intervalDesignatorIdx));
            T end = temporalConverter.parse(text.subSequence(intervalDesignatorIdx + 1, text.length()));
            return factory.apply(start, end);
        }
    }

    @lombok.AllArgsConstructor
    public static final class StartDuration<T extends Temporal & Comparable<? super T>, D extends TemporalAmount>
            extends TemporalIntervalRepresentation<T, D> {

        @lombok.NonNull
        private final ISO8601.Converter<T> temporalConverter;

        @lombok.NonNull
        private final ISO8601.Converter<D> durationConverter;

        @Override
        public String format(TemporalInterval<T, D> interval) {
            return temporalConverter.format(interval.start()) + "/" + durationConverter.format(interval.getDuration());
        }

        public <I extends TemporalInterval<T, ?>> I parse(CharSequence text, BiFunction<T, D, I> factory) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            T start = temporalConverter.parse(text.subSequence(0, intervalDesignatorIdx));
            D duration = durationConverter.parse(text.subSequence(intervalDesignatorIdx + 1, text.length()));
            return factory.apply(start, duration);
        }
    }

    @lombok.AllArgsConstructor
    public static final class DurationEnd<T extends Temporal & Comparable<? super T>, D extends TemporalAmount>
            extends TemporalIntervalRepresentation<T, D> {

        @lombok.NonNull
        private final ISO8601.Converter<D> durationConverter;

        @lombok.NonNull
        private final ISO8601.Converter<T> temporalConverter;

        @Override
        public String format(TemporalInterval<T, D> interval) {
            return durationConverter.format(interval.getDuration()) + "/" + temporalConverter.format(interval.end());
        }

        public <I extends TemporalInterval<T, ?>> I parse(CharSequence text, BiFunction<D, T, I> factory) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            D duration = durationConverter.parse(text.subSequence(0, intervalDesignatorIdx));
            T end = temporalConverter.parse(text.subSequence(intervalDesignatorIdx + 1, text.length()));
            return factory.apply(duration, end);
        }
    }

    private static int getIntervalDesignatorIndex(CharSequence text) throws DateTimeParseException {
        int intervalDesignatorIdx = indexOf(text, '/');
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find interval designator", text, 0);
        }
        return intervalDesignatorIdx;
    }

    private static int indexOf(CharSequence text, char c) {
        if (text instanceof String) {
            return ((String) text).indexOf(c);
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
}
