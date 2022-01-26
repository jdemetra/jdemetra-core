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
import nbbrd.design.RepresentableAs;
import nbbrd.design.RepresentableAsString;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

/**
 * Simple structure that defines a set of data such as a time series, a
 * collection or a dummy set.<p>
 * This object doesn't hold data but only the parameters used to retrieve
 * it.<br>It is immutable and therefore thread-safe.<br>It is created by a
 * builder.
 *
 * @author Philippe Charles
 */
@RepresentableAsString
@RepresentableAs(value = URI.class, parseMethodName = "parseURI")
@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataSet {

    /**
     * Defines a DataSet kind.
     */
    public enum Kind {

        /**
         * A time series
         */
        SERIES,
        /**
         * A collection of time series
         */
        COLLECTION,
        /**
         * A dummy set
         */
        DUMMY
    }

    @lombok.NonNull
    DataSource dataSource;

    @lombok.NonNull
    Kind kind;

    @lombok.NonNull
    @lombok.Singular
    SortedMap<String, String> parameters;

    public @Nullable String getParameter(@NonNull String key) {
        return getParameters().get(key);
    }

    /**
     * Creates a new builder with the content of this datasource.
     *
     * @param kind a non-null dataset kind
     * @return a non-null builder
     */
    public @NonNull Builder toBuilder(@NonNull Kind kind) {
        Objects.requireNonNull(kind, "kind");
        return toBuilder().kind(kind);
    }

    @Override
    public String toString() {
        return formatAsUri(this).buildString();
    }

    public @NonNull URI toURI() {
        return formatAsUri(this).build();
    }

    @StaticFactoryMethod
    public static @NonNull DataSet parse(@NonNull CharSequence input) throws IllegalArgumentException {
        return parseAsUri(URI.create(input.toString()));
    }

    @StaticFactoryMethod
    public static @NonNull DataSet parseURI(@NonNull URI input) throws IllegalArgumentException {
        return parseAsUri(input);
    }

    @StaticFactoryMethod
    public static @NonNull DataSet of(@NonNull DataSource dataSource, @NonNull Kind kind) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new DataSet(dataSource, kind, Collections.emptySortedMap());
    }

    @StaticFactoryMethod
    public static @NonNull DataSet of(@NonNull DataSource dataSource, @NonNull Kind kind, @NonNull String key, @NonNull String value) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new DataSet(dataSource, kind, SortedMaps.immutableOf(key, value));
    }

    @StaticFactoryMethod
    public static @NonNull DataSet deepCopyOf(@NonNull DataSource dataSource, @NonNull Kind kind, @NonNull Map<String, String> params) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new DataSet(dataSource, kind, SortedMaps.immutableCopyOf(params));
    }

    public static @NonNull Builder builder(@NonNull DataSource dataSource, @NonNull Kind kind) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new Builder().dataSource(dataSource).kind(kind);
    }

    private static final String SCHEME = "demetra";
    private static final String HOST = "tsprovider";

    private static DataSet parseAsUri(URI uri) throws IllegalArgumentException {
        if (!SCHEME.equals(uri.getScheme())) {
            throw new IllegalArgumentException("Invalid scheme: " + uri.getScheme());
        }
        if (!HOST.equals(uri.getHost())) {
            throw new IllegalArgumentException("Invalid host: " + uri.getHost());
        }
        String[] path = UriBuilder.getPathArray(uri, 3);
        if (path == null) {
            throw new IllegalArgumentException("Invalid path: " + uri.getRawPath());
        }
        Map<String, String> query = UriBuilder.getQueryMap(uri);
        if (query == null) {
            throw new IllegalArgumentException("Invalid query: " + uri.getRawQuery());
        }
        Map<String, String> fragment = UriBuilder.getFragmentMap(uri);
        if (fragment == null) {
            throw new IllegalArgumentException("Invalid fragment: " + uri.getRawFragment());
        }
        DataSource dataSource = new DataSource(path[0], path[1], SortedMaps.immutableCopyOf(query));
        return new DataSet(dataSource, Kind.valueOf(path[2]), SortedMaps.immutableCopyOf(fragment));
    }

    private static UriBuilder formatAsUri(DataSet value) {
        DataSource dataSource = value.getDataSource();
        return new UriBuilder(SCHEME, HOST)
                .path(dataSource.getProviderName(), dataSource.getVersion(), value.getKind().name())
                .query(dataSource.getParameters())
                .fragment(value.getParameters());
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

        @NonNull P get(@NonNull DataSet config);

        void set(@NonNull Builder builder, @Nullable P value);
    }
}
