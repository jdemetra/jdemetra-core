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
package demetra.tsprovider;

import demetra.util.UriBuilder;
import internal.util.SortedMaps;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.StringValue;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

/**
 * Simple structure that defines a source of data such as a file, a database or
 * any resource.<p>
 * This object doesn't hold data but only the parameters used to get the
 * data.<br>It is immutable and therefore thread-safe.<br>It is created by a
 * builder.
 *
 * @author Philippe Charles
 */
@StringValue
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataSource {

    @lombok.NonNull
    String providerName;

    @lombok.NonNull
    String version;

    @lombok.NonNull
    @lombok.Singular
    SortedMap<String, String> parameters;

    public @Nullable String getParameter(@NonNull String key) {
        return getParameters().get(key);
    }

    @Override
    public String toString() {
        return formatAsUri(this);
    }

    @StaticFactoryMethod
    public static @NonNull DataSource parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return parseAsUri(input);
    }

    @StaticFactoryMethod
    public static @NonNull DataSource of(@NonNull String providerName, @NonNull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, Collections.emptySortedMap());
    }

    @StaticFactoryMethod
    public static @NonNull DataSource of(@NonNull String providerName, @NonNull String version, @NonNull String key, @NonNull String value) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, SortedMaps.immutableOf(key, value));
    }

    @StaticFactoryMethod
    public static @NonNull DataSource deepCopyOf(@NonNull String providerName, @NonNull String version, @NonNull Map<String, String> params) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, SortedMaps.immutableCopyOf(params));
    }

    public static @NonNull Builder builder(@NonNull String providerName, @NonNull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new Builder().providerName(providerName).version(version);
    }

    private static final String SCHEME = "demetra";
    private static final String HOST = "tsprovider";

    private static DataSource parseAsUri(CharSequence input) throws IllegalArgumentException {
        URI uri = URI.create(input.toString());
        if (!SCHEME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("Invalid scheme: " + uri.getScheme());
        }
        if (!HOST.equals(uri.getHost())) {
            throw new IllegalArgumentException("Invalid host: " + uri.getHost());
        }
        String[] path = UriBuilder.getPathArray(uri, 2);
        if (path == null) {
            throw new IllegalArgumentException("Invalid path: " + uri.getRawPath());
        }
        Map<String, String> query = UriBuilder.getQueryMap(uri);
        if (query == null) {
            throw new IllegalArgumentException("Invalid query: " + uri.getRawQuery());
        }
        return new DataSource(path[0], path[1], SortedMaps.immutableCopyOf(query));
    }

    private static String formatAsUri(DataSource value) {
        return new UriBuilder(SCHEME, HOST)
                .path(value.getProviderName(), value.getVersion())
                .query(value.getParameters())
                .buildString();
    }

    /**
     * Tool that loads/stores values from/to a key-value structure. It provides a
     * best-effort retrieval behavior where a failure returns a default value
     * instead of an error. All implementations must be thread-safe.
     *
     * @param <P>
     * @author Philippe Charles
     */
    @ThreadSafe
    public interface Converter<P> {

        @NonNull P getDefaultValue();

        @NonNull P get(@NonNull DataSource config);

        void set(@NonNull Builder builder, @Nullable P value);
    }
}
