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
import demetra.tsprovider.util.IConfig;
import demetra.design.Immutable;
import demetra.design.VisibleForTesting;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import demetra.util.Parser;
import demetra.util.Formatter;
import internal.util.SortedMaps;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;

/**
 * Simple structure that defines a set of data such as a time series, a
 * collection or a dummy set.<p>
 * This object doesn't hold data but only the parameters used to retrieve
 * it.<br>It is immutable and therefore thread-safe.<br>It is created by a
 * builder.<br>A default xml serializer is provided but its use is not
 * mandatory.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@Immutable
@lombok.ToString
@lombok.EqualsAndHashCode
public final class DataSet implements IConfig {

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
        DUMMY;
    };
    private final DataSource dataSource;
    private final Kind kind;
    private final SortedMap<String, String> params;

    @VisibleForTesting
    DataSet(@Nonnull DataSource dataSource, @Nonnull Kind kind, @Nonnull SortedMap<String, String> params) {
        this.dataSource = dataSource;
        this.kind = kind;
        this.params = params;
    }

    @Nonnull
    public DataSource getDataSource() {
        return dataSource;
    }

    @Nonnull
    public Kind getKind() {
        return kind;
    }

    @Override
    public SortedMap<String, String> getParams() {
        return params;
    }

    /**
     * Creates a new builder with the content of this datasource.
     *
     * @param kind a non-null dataset kind
     * @return a non-null builder
     * @since 2.2.0
     */
    @Nonnull
    public Builder toBuilder(@Nonnull Kind kind) {
        Objects.requireNonNull(kind, "kind");
        return new Builder(dataSource, kind).putAll(params);
    }

    @Nonnull
    public static DataSet of(@Nonnull DataSource dataSource, @Nonnull Kind kind) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new DataSet(dataSource, kind, Collections.emptySortedMap());
    }

    @Nonnull
    public static DataSet of(@Nonnull DataSource dataSource, @Nonnull Kind kind, @Nonnull String key, @Nonnull String value) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new DataSet(dataSource, kind, SortedMaps.immutableOf(key, value));
    }

    @Nonnull
    public static DataSet deepCopyOf(@Nonnull DataSource dataSource, @Nonnull Kind kind, @Nonnull Map<String, String> params) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new DataSet(dataSource, kind, SortedMaps.immutableCopyOf(params));
    }

    @Nonnull
    public static Builder builder(@Nonnull DataSource dataSource, @Nonnull Kind kind) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new Builder(dataSource, kind);
    }

    /**
     * Returns a convenient DataSet formatter that produces uri output.<p>
     * This formatter is thread-safe.
     *
     * @return a DataSource formatter
     */
    @Nonnull
    public static Formatter<DataSet> uriFormatter() {
        return DataSet::formatAsUri;
    }

    /**
     * Returns a convenient DataSet parser that consumes uri input.<p>
     * This parser is thread-safe.
     *
     * @return a DataSource parser
     */
    @Nonnull
    public static Parser<DataSet> uriParser() {
        return DataSet::parseAsUri;
    }

    public static class Builder implements IConfig.Builder<Builder, DataSet> {

        final DataSource dataSource;
        final Kind kind;
        final Map<String, String> params;

        @VisibleForTesting
        Builder(DataSource dataSource, Kind kind) {
            this.dataSource = dataSource;
            this.kind = kind;
            this.params = new HashMap<>();
        }

        @Override
        public Builder put(String key, String value) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");
            params.put(key, value);
            return this;
        }

        public Builder clear() {
            params.clear();
            return this;
        }

        @Override
        public DataSet build() {
            return new DataSet(dataSource, kind, SortedMaps.immutableCopyOf(params));
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final String SCHEME = "demetra";
    private static final String HOST = "tsprovider";

    private static DataSet parseAsUri(CharSequence input) {
        try {
            return parseAsUri(new URI(input.toString()));
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private static DataSet parseAsUri(URI uri) {
        if (!SCHEME.equals(uri.getScheme()) || !HOST.equals(uri.getHost())) {
            return null;
        }
        String[] path = UriBuilder.getPathArray(uri, 3);
        if (path == null) {
            return null;
        }
        Map<String, String> query = UriBuilder.getQueryMap(uri);
        if (query == null) {
            return null;
        }
        Map<String, String> fragment = UriBuilder.getFragmentMap(uri);
        if (fragment == null) {
            return null;
        }
        DataSource dataSource = new DataSource(path[0], path[1], SortedMaps.immutableCopyOf(query));
        return new DataSet(dataSource, Kind.valueOf(path[2]), SortedMaps.immutableCopyOf(fragment));
    }

    private static CharSequence formatAsUri(DataSet value) {
        DataSource dataSource = value.getDataSource();
        return new UriBuilder(SCHEME, HOST)
                .path(dataSource.getProviderName(), dataSource.getVersion(), value.getKind().name())
                .query(dataSource.getParams())
                .fragment(value.getParams())
                .buildString();
    }
    //</editor-fold>
}
