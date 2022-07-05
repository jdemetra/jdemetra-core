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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor(staticName = "of")
public final class TimeRecurrenceFormatter {

    @lombok.NonNull
    private final TimeIntervalFormatter intervalFormatter;

    @lombok.NonNull
    private final TimeIntervalQuery<? extends TimeInterval<?, ?>> intervalQuery;

    @NonNull
    public String format(@NonNull TimeRecurrence<?> timeRecurrence) {
        StringBuilder result = new StringBuilder(32);
        formatTo(timeRecurrence, result);
        return result.toString();
    }

    public void formatTo(@NonNull TimeRecurrence<?> timeRecurrence, @NonNull Appendable appendable) {
        try {
            appendable.append(RECURRENCE_CHAR);
            appendable.append(String.valueOf(timeRecurrence.length()));
            appendable.append(RECURRENCE_SEPARATOR);
            intervalFormatter.formatTo(timeRecurrence.getInterval(), appendable);
        } catch (IOException ex) {
            throw new DateTimeException(ex.getMessage(), ex);
        }
    }

    @NonNull
    public <R extends TimeRecurrence<?>> R parse(@NonNull CharSequence text, @NonNull TimeRecurrenceQuery<R> query) throws DateTimeParseException {
        if (text.charAt(0) != RECURRENCE_CHAR) {
            throw new DateTimeParseException("Cannot found recurrence character", text, 0);
        }
        int index = getRecurrenceSeparatorIndex(text);
        CharSequence left = text.subSequence(1, index);
        CharSequence right = text.subSequence(index + 1, text.length());
        return query.queryFrom(
                new TimeRecurrenceAccessor() {
                    @Override
                    public TimeInterval<?, ?> getInterval() {
                        return intervalFormatter.parse(right, intervalQuery);
                    }

                    @Override
                    public int length() {
                        return parseLength(left);
                    }
                });
    }

    private static int getRecurrenceSeparatorIndex(CharSequence text) throws DateTimeParseException {
        int intervalDesignatorIdx = TimeIntervalFormatter.indexOf(text, RECURRENCE_SEPARATOR);
        if (intervalDesignatorIdx == -1) {
            throw new DateTimeParseException("Cannot find recurrence separator", text, 0);
        }
        return intervalDesignatorIdx;
    }

    private static int parseLength(CharSequence text) throws DateTimeParseException {
        try {
            return Integer.parseInt(text.toString());
        } catch (NumberFormatException ex) {
            throw new DateTimeParseException("Cannot parse length", text, 0, ex);
        }
    }

    private static final char RECURRENCE_CHAR = 'R';
    private static final char RECURRENCE_SEPARATOR = '/';
}
