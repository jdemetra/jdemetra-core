/*
 * Copyright 2018 National Bank of Belgium
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
package demetra.util;

import internal.util.Strings.CharMatcher;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import nbbrd.design.LombokWorkaround;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder
public final class Substitutor {

    @NonNull
    public static Substitutor of(@NonNull Map<? super String, ? extends Object> mapper) {
        return builder().mapper(mapper::get).build();
    }

    @NonNull
    public static Substitutor of(@NonNull Function<? super String, ? extends Object> mapper) {
        return builder().mapper(mapper).build();
    }

    @NonNull
    public static Substitutor ofBean(@NonNull Object bean) throws IntrospectionException {
        return builder().mapper(mapperOfBean(bean)).build();
    }

    @lombok.NonNull
    private Function<? super String, ? extends Object> mapper;

    @lombok.NonNull
    private String prefix;

    @lombok.NonNull
    private String suffix;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .prefix("${")
                .suffix("}");
    }

    @NonNull
    public String replace(@NonNull CharSequence input) {
        Objects.requireNonNull(input);
        if (isBlank(input)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        try {
            fill(input, result);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result.toString();
    }

    public void replaceInto(@NonNull CharSequence input, @NonNull Appendable output) throws IOException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        if (isBlank(input)) {
            return;
        }

        fill(input, output);
    }

    public void replaceInto(@NonNull CharSequence input, @NonNull StringBuilder output) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        if (isBlank(input)) {
            return;
        }

        try {
            fill(input, output);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isBlank(CharSequence input) {
        return input.length() < prefix.length() + suffix.length();
    }

    private void fill(@NonNull CharSequence input, @NonNull Appendable output) throws IOException {
        int start = 0;
        int end = 0;
        State state = State.TEXT;
        CharMatcher prefixMatcher = CharMatcher.of(prefix);
        CharMatcher suffixMatcher = CharMatcher.of(suffix);
        while (end < input.length()) {
            switch (state) {
                case TEXT:
                    if (prefixMatcher.matches(input, end)) {
                        output.append(input, start, end);
                        end += prefix.length();
                        start = end;
                        state = State.VAR;
                    } else {
                        end++;
                    }
                    break;
                case VAR:
                    if (suffixMatcher.matches(input, end)) {
                        String key = input.subSequence(start, end).toString();
                        Object value = mapper.apply(key);
                        output.append(value != null ? value.toString() : null);
                        end += suffix.length();
                        start = end;
                        state = State.TEXT;
                    } else {
                        end++;
                    }
                    break;
            }
        }
        switch (state) {
            case TEXT:
                if (start < end) {
                    output.append(input, start, end);
                }
                break;
            case VAR:
                output.append(prefix);
                if (start < end) {
                    output.append(input, start, end);
                }
                break;
        }
    }

    private enum State {
        TEXT, VAR
    }

    private static Function<String, Object> mapperOfBean(Object bean) throws IntrospectionException {
        PropertyDescriptor[] properties = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
        return new Function<String, Object>() {
            @Override
            public Object apply(String o) {
                for (PropertyDescriptor property : properties) {
                    if (property.getName().equals(o)) {
                        Method reader = property.getReadMethod();
                        if (reader != null) {
                            try {
                                return reader.invoke(bean);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                }
                return null;
            }
        };
    }
}
