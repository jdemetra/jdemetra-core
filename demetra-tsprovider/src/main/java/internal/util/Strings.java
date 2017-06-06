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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Strings {

    @Nonnull
    public String nullToEmpty(@Nullable String o) {
        return (o == null) ? "" : o;
    }

    public boolean isNullOrEmpty(@Nullable String o) {
        return o == null || o.isEmpty();
    }

    @Nonnull
    public Stream<String> splitToStream(@Nonnull String separator, @Nonnull CharSequence input) {
        return separator.length() == 1
                ? splitToStream(separator.charAt(0), input)
                : StreamSupport.stream(Spliterators.spliteratorUnknownSize(splitToIterator(separator, input), 0), false);
    }

    @Nonnull
    public Stream<String> splitToStream(char separator, @Nonnull CharSequence input) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(splitToIterator(separator, input), 0), false);
    }

    @Nonnull
    public Iterator<String> splitToIterator(@Nonnull String separator, @Nonnull CharSequence input) {
        return asIterator((seq, index) -> match(separator, seq, index), separator.length(), input);
    }

    @Nonnull
    public Iterator<String> splitToIterator(char separator, @Nonnull CharSequence input) {
        return asIterator((seq, index) -> match(separator, seq, index), 1, input);
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
                    if (matcher.match(input, end)) {
                        return true;
                    }
                }
                return start <= input.length();
            }
        };
    }

    private boolean match(char separator, CharSequence seq, int index) {
        return seq.charAt(index) == separator;
    }

    private boolean match(String separator, CharSequence seq, int index) {
        for (int i = 0; i < separator.length(); i++) {
            if (seq.charAt(index + i) != separator.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    interface CharMatcher {

        boolean match(CharSequence seq, int index);
    }
}
