/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import demetra.design.IBuilder;
import internal.util.Strings;
import internal.util.URLEncoder2;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * http://en.wikipedia.org/wiki/URI_scheme
 * http://msdn.microsoft.com/en-us/library/aa767914%28v=vs.85%29.aspx
 *
 * @author Philippe Charles
 */
public final class UriBuilder implements IBuilder<URI> {

    // PROPERTIES
    private final String scheme;
    private final String host;
    private String[] path;
    private SortedMap<String, String> query;
    private SortedMap<String, String> fragment;

    public UriBuilder(@Nonnull String scheme, @Nonnull String host) {
        if (Strings.isNullOrEmpty(scheme)) {
            throw new IllegalArgumentException("scheme can't be null or empty");
        }
        if (Strings.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("host can't be null or empty");
        }
        this.scheme = scheme;
        this.host = host;
        reset();
    }

    @Nonnull
    public UriBuilder reset() {
        this.path = null;
        this.query = null;
        this.fragment = null;
        return this;
    }

    @Nonnull
    public UriBuilder path(@Nullable String... path) {
        this.path = path;
        return this;
    }

    @Nonnull
    public UriBuilder query(@Nullable SortedMap<String, String> query) {
        this.query = query;
        return this;
    }

    @Nonnull
    public UriBuilder fragment(@Nullable SortedMap<String, String> fragment) {
        this.fragment = fragment;
        return this;
    }

    @Nonnull
    public String buildString() {
        StringBuilder result = new StringBuilder();
        result.append(scheme);
        result.append("://");
        result.append(host);
        if (path != null) {
            appendArray(result.append('/'), path, '/');
        }
        if (query != null) {
            appendMap(result.append('?'), query, '&', '=');
        }
        if (fragment != null) {
            appendMap(result.append('#'), fragment, '&', '=');
        }
        return result.toString();
    }

    @Override
    public URI build() {
        return URI.create(buildString());
    }

    private static String decodeUrlUtf8(String o) {
        try {
            return URLDecoder.decode(o, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nonnull
    private static StringBuilder appendEntry(@Nonnull StringBuilder sb, @Nonnull Entry<String, String> o, char sep) {
        URLEncoder2.encode(sb, o.getKey(), StandardCharsets.UTF_8);
        sb.append(sep);
        URLEncoder2.encode(sb, o.getValue(), StandardCharsets.UTF_8);
        return sb;
    }

    @Nonnull
    private static StringBuilder appendMap(@Nonnull StringBuilder sb, @Nonnull Map<String, String> keyValues, char sep1, char sep2) {
        if (!keyValues.isEmpty()) {
            Iterator<Entry<String, String>> iterator = keyValues.entrySet().iterator();
            appendEntry(sb, iterator.next(), sep2);
            while (iterator.hasNext()) {
                appendEntry(sb.append(sep1), iterator.next(), sep2);
            }
        }
        return sb;
    }

    @Nonnull
    private static StringBuilder appendArray(@Nonnull StringBuilder sb, @Nonnull String[] array, char sep) {
        if (array.length > 0) {
            int i = 0;
            URLEncoder2.encode(sb, array[i], StandardCharsets.UTF_8);
            while (++i < array.length) {
                URLEncoder2.encode(sb.append(sep), array[i], StandardCharsets.UTF_8);
            }
        }
        return sb;
    }

    @Nullable
    public static String[] getPathArray(@Nonnull URI uri) {
        String path = uri.getRawPath();
        return path != null && !path.isEmpty() ? splitToArray(path.subSequence(1, path.length())) : null;
    }

    @Nullable
    public static String[] getPathArray(@Nonnull URI uri, int expectedSize) {
        String path = uri.getRawPath();
        return path != null && !path.isEmpty() ? splitToArray(path.subSequence(1, path.length()), expectedSize) : null;
    }

    @Nullable
    public static Map<String, String> getQueryMap(@Nonnull URI uri) {
        String query = uri.getRawQuery();
        return query != null ? splitMap(query) : null;
    }

    @Nullable
    public static Map<String, String> getFragmentMap(@Nonnull URI uri) {
        String fragment = uri.getRawFragment();
        return fragment != null ? splitMap(fragment) : null;
    }

    @Nullable
    private static String[] splitToArray(@Nonnull CharSequence input) {
        return Strings.splitToStream('/', input).map(UriBuilder::decodeUrlUtf8).toArray(String[]::new);
    }

    @Nullable
    private static String[] splitToArray(@Nonnull CharSequence input, int expectedSize) {
        Iterator<String> items = Strings.splitToIterator('/', input);
        if (expectedSize == 0 || !items.hasNext()) {
            return null;
        }
        String[] result = new String[expectedSize];
        int index = 0;
        do {
            result[index++] = decodeUrlUtf8(items.next());
        } while (index < expectedSize && items.hasNext());
        return !items.hasNext() && index == expectedSize ? result : null;
    }

    @Nullable
    private static Map<String, String> splitMap(@Nonnull CharSequence input) {
        if (input.length() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        return splitMapTo(input, result::put) ? result : null;
    }

    private static boolean splitMapTo(@Nonnull CharSequence input, @Nonnull BiConsumer<String, String> consumer) {
        Iterator<String> iter = Strings.splitToIterator('&', input);
        while (iter.hasNext()) {
            String entry = iter.next();
            Iterator<String> entryFields = Strings.splitToIterator('=', entry);
            if (!entryFields.hasNext()) {
                return false;
            }
            String key = entryFields.next();
            if (!entryFields.hasNext()) {
                return false;
            }
            String value = entryFields.next();
            if (entryFields.hasNext()) {
                return false;
            }
            consumer.accept(decodeUrlUtf8(key), decodeUrlUtf8(value));
        }
        return true;
    }
}
