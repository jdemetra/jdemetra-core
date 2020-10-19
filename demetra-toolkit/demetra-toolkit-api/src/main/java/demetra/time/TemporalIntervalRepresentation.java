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

import demetra.design.SealedType;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @param <T>
 * @param <D>
 *
 * @author Philippe Charles
 */
@SealedType({
    TemporalIntervalRepresentation.StartEnd.class,
    TemporalIntervalRepresentation.StartDuration.class,
    TemporalIntervalRepresentation.DurationEnd.class,
    TemporalIntervalRepresentation.Duration.class
})
public abstract class TemporalIntervalRepresentation<T extends Temporal & Comparable<? super T>, D extends TemporalAmount> {

    protected TemporalIntervalRepresentation() {
    }

    public abstract String format(TemporalInterval<T, D> interval);

    @lombok.AllArgsConstructor
    public static final class StartEnd<T extends Temporal & Comparable<? super T>, D extends TemporalAmount>
            extends TemporalIntervalRepresentation<T, D> {

        @lombok.NonNull
        private final ISO8601.Converter<T> temporalConverter;

        @Override
        public String format(TemporalInterval<T, D> interval) {
            return temporalConverter.format(interval.start()) + "/" + temporalConverter.format(interval.end());
        }

        public String formatConcise(TemporalInterval<T, D> interval) {
            CharSequence first = temporalConverter.format(interval.start());
            CharSequence second = temporalConverter.format(interval.end());
            return first + "/" + compact(first, second);
        }

        public <I extends TemporalInterval<T, ?>> I parse(CharSequence text, BiFunction<T, T, I> factory) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence first = text.subSequence(0, intervalDesignatorIdx);
            CharSequence second = text.subSequence(intervalDesignatorIdx + 1, text.length());
            T start = temporalConverter.parse(first);
            T end = temporalConverter.parse(expand(first, second));
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

    @lombok.AllArgsConstructor
    public static final class Duration<T extends Temporal & Comparable<? super T>, D extends TemporalAmount>
            extends TemporalIntervalRepresentation<T, D> {

        @lombok.NonNull
        private final ISO8601.Converter<D> durationConverter;

        @Override
        public String format(TemporalInterval<T, D> interval) {
            return durationConverter.format(interval.getDuration()).toString();
        }

        public <I extends TemporalInterval<T, ?>> I parse(CharSequence text, Function<D, I> factory) {
            D duration = durationConverter.parse(text);
            return factory.apply(duration);
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

    private static CharSequence compact(CharSequence ref, CharSequence value) {
        int anchor = -1;
        for (int i = 0; i < ref.length(); i++) {
            if (!Character.isDigit(ref.charAt(i))) {
                anchor = i;
            }
            if (ref.charAt(i) != value.charAt(i)) {
                return value.subSequence(anchor + 1, value.length());
            }
        }
        return "";
    }

    private static CharSequence expand(CharSequence ref, CharSequence value) {
        int diff = ref.length() - value.length();
        if (diff <= 0) {
            return value;
        }
        return ref.subSequence(0, diff).toString() + value;
    }
}
