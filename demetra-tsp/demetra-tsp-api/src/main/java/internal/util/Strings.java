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

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Strings {

    @NonNull
    public String nullToEmpty(@Nullable String o) {
        return (o == null) ? "" : o;
    }

    @Nullable
    public String emptyToNull(@Nullable String o) {
        return o != null && o.isEmpty() ? null : o;
    }

    public boolean isNullOrEmpty(@Nullable String o) {
        return o == null || o.isEmpty();
    }

    public boolean isNotEmpty(@Nullable String o) {
        return !isNullOrEmpty(o);
    }

    @NonNull
    public Stream<String> splitToStream(@NonNull String separator, @NonNull CharSequence input) {
        return separator.length() == 1
                ? splitToStream(separator.charAt(0), input)
                : StreamSupport.stream(Spliterators.spliteratorUnknownSize(splitToIterator(separator, input), 0), false);
    }

    @NonNull
    public Stream<String> splitToStream(char separator, @NonNull CharSequence input) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(splitToIterator(separator, input), 0), false);
    }

    @NonNull
    public Iterator<String> splitToIterator(@NonNull String separator, @NonNull CharSequence input) {
        return asIterator(CharMatcher.of(separator), separator.length(), input);
    }

    @NonNull
    public Iterator<String> splitToIterator(char separator, @NonNull CharSequence input) {
        return asIterator(CharMatcher.of(separator), 1, input);
    }

    private Iterator<String> asIterator(CharMatcher matcher, int inc, CharSequence input) {
        return new AbstractIterator<String>() {
            private int start = -1;
            private int end = -1;

            @Override
            protected String get() {
                return start == end ? "" : input.subSequence(start, end).toString();
            }

            @Override
            protected boolean moveNext() {
                end = end + inc;
                start = end;
                for (; end < input.length(); end++) {
                    if (matcher.matches(input, end)) {
                        return true;
                    }
                }
                return start <= input.length();
            }
        };
    }

    @FunctionalInterface
    public interface CharMatcher {

        boolean matches(CharSequence seq, int index);

        static CharMatcher of(char value) {
            return (seq, index) -> seq.charAt(index) == value;
        }

        static CharMatcher of(CharSequence value) {
            return (seq, index) -> {
                if (value.length() > seq.length() - index) {
                    return false;
                }
                for (int i = 0; i < value.length(); i++) {
                    if (seq.charAt(index + i) != value.charAt(i)) {
                        return false;
                    }
                }
                return true;
            };
        }
    }
}
