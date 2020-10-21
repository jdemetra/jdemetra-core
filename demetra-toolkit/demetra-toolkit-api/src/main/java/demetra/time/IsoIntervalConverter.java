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
 * @param <I>
 *
 * @author Philippe Charles
 */
@SealedType({
    IsoIntervalConverter.StartEnd.class,
    IsoIntervalConverter.StartDuration.class,
    IsoIntervalConverter.DurationEnd.class,
    IsoIntervalConverter.Duration.class
})
public abstract class IsoIntervalConverter<I extends IsoInterval<?, ?>>
        implements IsoConverter<I> {

    protected IsoIntervalConverter() {
    }

    @lombok.AllArgsConstructor
    public static final class StartEnd<T extends Temporal & Comparable<? super T>, I extends IsoInterval<T, ?>>
            extends IsoIntervalConverter<I> {

        @lombok.NonNull
        private final IsoConverter<T> temporal;

        private boolean concise;

        @lombok.NonNull
        private final BiFunction<T, T, I> interval;

        @Override
        public String format(I interval) {
            CharSequence left = temporal.format(interval.start());
            CharSequence right = temporal.format(interval.end());
            return left + "/" + (concise ? compact(left, right) : right);
        }

        @Override
        public I parse(CharSequence text) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            return interval.apply(
                    temporal.parse(left),
                    temporal.parse(concise ? expand(left, right) : right)
            );
        }
    }

    @lombok.AllArgsConstructor
    public static final class StartDuration<T extends Temporal & Comparable<? super T>, D extends TemporalAmount, I extends IsoInterval<T, D>>
            extends IsoIntervalConverter<I> {

        @lombok.NonNull
        private final IsoConverter<T> temporal;

        @lombok.NonNull
        private final IsoConverter<D> duration;

        @lombok.NonNull
        private final BiFunction<T, D, I> interval;

        @Override
        public String format(I interval) {
            return temporal.format(interval.start()) + "/" + duration.format(interval.getDuration());
        }

        @Override
        public I parse(CharSequence text) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            return interval.apply(
                    temporal.parse(left),
                    duration.parse(right)
            );
        }
    }

    @lombok.AllArgsConstructor
    public static final class DurationEnd<T extends Temporal & Comparable<? super T>, D extends TemporalAmount, I extends IsoInterval<T, D>>
            extends IsoIntervalConverter<I> {

        @lombok.NonNull
        private final IsoConverter<D> duration;

        @lombok.NonNull
        private final IsoConverter<T> temporal;

        @lombok.NonNull
        private final BiFunction<D, T, I> interval;

        @Override
        public String format(I interval) {
            return duration.format(interval.getDuration()) + "/" + temporal.format(interval.end());
        }

        @Override
        public I parse(CharSequence text) {
            int intervalDesignatorIdx = getIntervalDesignatorIndex(text);
            CharSequence left = text.subSequence(0, intervalDesignatorIdx);
            CharSequence right = text.subSequence(intervalDesignatorIdx + 1, text.length());
            return interval.apply(
                    duration.parse(left),
                    temporal.parse(right)
            );
        }
    }

    @lombok.AllArgsConstructor
    public static final class Duration<D extends TemporalAmount, I extends IsoInterval<?, D>>
            extends IsoIntervalConverter<I> {

        @lombok.NonNull
        private final IsoConverter<D> duration;

        @lombok.NonNull
        private final Function<D, I> interval;

        @Override
        public String format(I interval) {
            return duration.format(interval.getDuration()).toString();
        }

        @Override
        public I parse(CharSequence text) {
            return interval.apply(
                    duration.parse(text)
            );
        }
    }

    static int getIntervalDesignatorIndex(CharSequence text) throws DateTimeParseException {
        int intervalDesignatorIdx = indexOf(text, '/');
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find interval designator", text, 0);
        }
        return intervalDesignatorIdx;
    }

    static int indexOf(CharSequence text, char c) {
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
