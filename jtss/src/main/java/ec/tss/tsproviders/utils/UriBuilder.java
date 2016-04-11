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
package ec.tss.tsproviders.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import ec.tstoolkit.design.IBuilder;
import ec.tstoolkit.utilities.URLEncoder2;
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
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * http://en.wikipedia.org/wiki/URI_scheme
 * http://msdn.microsoft.com/en-us/library/aa767914%28v=vs.85%29.aspx
 *
 * @author Philippe Charles
 */
public final class UriBuilder implements IBuilder<URI> {

    private static final Splitter PATH_SLITTER = Splitter.on('/');
    private static final Splitter.MapSplitter KEY_VALUE_SPLITTER = Splitter.on('&').withKeyValueSeparator("=");

    // PROPERTIES
    private final String scheme;
    private final String host;
    private String[] path;
    private SortedMap<String, String> query;
    private SortedMap<String, String> fragment;

    public UriBuilder(@Nonnull String scheme, @Nonnull String host) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(scheme), "scheme can't be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(host), "host can't be null or empty");
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
            throw Throwables.propagate(ex);
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

    @Nonnull
    private static Map<String, String> transformKeyValues(@Nonnull Map<String, String> keyValues, @Nonnull Function<String, String> func) {
        Map<String, String> result = new HashMap<>();
        for (Entry<String, String> o : keyValues.entrySet()) {
            result.put(func.apply(o.getKey()), func.apply(o.getValue()));
        }
        return result;
    }

    @Nullable
    public static String[] getPathArray(@Nonnull URI uri) {
        String path = uri.getRawPath();
        return path != null && !path.isEmpty() ? PATH_SLITTER.splitToList(path.substring(1)).stream().map(UriBuilder::decodeUrlUtf8).toArray(String[]::new) : null;
    }

    @Nullable
    public static Map<String, String> getQueryMap(@Nonnull URI uri) {
        String query = uri.getRawQuery();
        return query != null ? (query.isEmpty() ? Collections.emptyMap() : transformKeyValues(KEY_VALUE_SPLITTER.split(query), UriBuilder::decodeUrlUtf8)) : null;
    }

    @Nullable
    public static Map<String, String> getFragmentMap(@Nonnull URI uri) {
        String fragment = uri.getRawFragment();
        return fragment != null ? (fragment.isEmpty() ? Collections.emptyMap() : transformKeyValues(KEY_VALUE_SPLITTER.split(fragment), UriBuilder::decodeUrlUtf8)) : null;
    }
}
