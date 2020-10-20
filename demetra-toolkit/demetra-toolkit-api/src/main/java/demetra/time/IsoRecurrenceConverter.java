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
import java.util.function.BiFunction;

/**
 * @param <I>
 * @param <R>
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class IsoRecurrenceConverter<I extends IsoInterval<?, ?>, R extends IsoRecurrence<I>>
        implements IsoConverter<R> {

    @lombok.NonNull
    private final IsoIntervalConverter<I> interval;

    @lombok.NonNull
    private final BiFunction<Integer, I, R> recurrence;

    @Override
    public CharSequence format(R value) {
        return "R" + value.length() + "/" + interval.format(value.getInterval());
    }

    @Override
    public R parse(CharSequence text) throws DateTimeParseException {
        if (text.charAt(0) != 'R') {
            throw new DateTimeParseException("Cannot found recurrence character", text, 0);
        }
        int index = IsoIntervalConverter.getIntervalDesignatorIndex(text);
        CharSequence left = text.subSequence(1, index);
        CharSequence right = text.subSequence(index + 1, text.length());
        return recurrence.apply(
                parseInt(left),
                interval.parse(right)
        );
    }

    private static Integer parseInt(CharSequence text) {
        try {
            return Integer.parseInt(text.toString());
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Cannot parse length", text, 0, ex);
        }
    }
}
