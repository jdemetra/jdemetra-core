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
package internal.util;

import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class InternalFormatter {

    public CharSequence formatTemporalAccessor(DateTimeFormatter formatter, TemporalAccessor value) {
        try {
            return formatter.format(value);
        } catch (DateTimeException ex) {
            return null;
        }
    }

    public CharSequence formatNumber(NumberFormat format, Number value) {
        return format.format(Objects.requireNonNull(value));
    }

    public CharSequence formatDoubleArray(double[] value) {
        return Arrays.toString(Objects.requireNonNull(value));
    }

    public CharSequence formatStringArray(String[] value) {
        return Arrays.toString(Objects.requireNonNull(value));
    }

    public CharSequence formatStringList(Function<Stream<CharSequence>, String> joiner, List<String> value) {
        try {
            return joiner.apply(value.stream().map(CharSequence.class::cast));
        } catch (Exception ex) {
            return null;
        }
    }

    public <T> CharSequence formatConstant(CharSequence constant, T value) {
        Objects.requireNonNull(value);
        return constant;
    }

    public <T> CharSequence formatNull(T value) {
        Objects.requireNonNull(value);
        return null;
    }
}
