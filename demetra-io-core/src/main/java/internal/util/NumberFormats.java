/*
 * Copyright 2019 National Bank of Belgium
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
package internal.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
final class NumberFormats {

    @Nullable
    public Number parseAll(@NonNull NumberFormat numberFormat, @NonNull CharSequence input) {
        String source = input.toString();
        ParsePosition pos = new ParsePosition(0);
        Number result = numberFormat.parse(source, pos);
        return pos.getIndex() == input.length() ? result : null;
    }

    @NonNull
    public CharSequence simplify(@NonNull NumberFormat numberFormat, @NonNull CharSequence input) {
        Objects.requireNonNull(numberFormat);
        return NumberFormats.hasGroupingSpaceChar(numberFormat)
                ? NumberFormats.removeGroupingSpaceChars(input)
                : Objects.requireNonNull(input);
    }

    private boolean hasGroupingSpaceChar(NumberFormat format) {
        return format instanceof DecimalFormat
                && hasGroupingSpaceChar(((DecimalFormat) format).getDecimalFormatSymbols());
    }

    private boolean hasGroupingSpaceChar(DecimalFormatSymbols symbols) {
        return Character.isSpaceChar(symbols.getGroupingSeparator());
    }

    private CharSequence removeGroupingSpaceChars(CharSequence input) {
        if (input.length() < 2) {
            return input;
        }
        StringBuilder result = new StringBuilder(input.length());
        result.append(input.charAt(0));
        for (int i = 1; i < input.length() - 1; i++) {
            if (!isGroupingSpaceChar(input, i)) {
                result.append(input.charAt(i));
            }
        }
        result.append(input.charAt(input.length() - 1));
        return result.length() != input.length() ? result.toString() : input;
    }

    private boolean isGroupingSpaceChar(CharSequence array, int index) {
        return Character.isSpaceChar(array.charAt(index))
                && Character.isDigit(array.charAt(index - 1))
                && Character.isDigit(array.charAt(index + 1));
    }
}
