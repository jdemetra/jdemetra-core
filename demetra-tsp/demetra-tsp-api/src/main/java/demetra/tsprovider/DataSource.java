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

import nbbrd.design.Immutable;
import nbbrd.design.VisibleForTesting;
import demetra.tsprovider.util.IConfig;
import demetra.util.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import internal.util.SortedMaps;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;

/**
 * Simple structure that defines a source of data such as a file, a database or
 * any resource.<p>
 * This object doesn't hold data but only the parameters used to get the
 * data.<br>It is immutable and therefore thread-safe.<br>It is created by a
 * builder.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@Immutable
@lombok.ToString
@lombok.EqualsAndHashCode
public final class DataSource implements IConfig {

    private final String providerName;
    private final String version;
    private final SortedMap<String, String> params;

    @VisibleForTesting
    DataSource(@NonNull String providerName, @NonNull String version, @NonNull SortedMap<String, String> params) {
        this.providerName = providerName;
        this.version = version;
        this.params = params;
    }

    @NonNull
    public String getProviderName() {
        return providerName;
    }

    @NonNull
    public String getVersion() {
        return version;
    }

    @Override
    public SortedMap<String, String> getParams() {
        return params;
    }

    /**
     * Creates a new builder with the content of this datasource.
     *
     * @return a non-null builder
     * @since 2.2.0
     */
    @NonNull
    public Builder toBuilder() {
        return new Builder(providerName, version).putAll(params);
    }

    @NonNull
    public static DataSource of(@NonNull String providerName, @NonNull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, Collections.emptySortedMap());
    }

    @NonNull
    public static DataSource of(@NonNull String providerName, @NonNull String version, @NonNull String key, @NonNull String value) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, SortedMaps.immutableOf(key, value));
    }

    @NonNull
    public static DataSource deepCopyOf(@NonNull String providerName, @NonNull String version, @NonNull Map<String, String> params) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, SortedMaps.immutableCopyOf(params));
    }

    @NonNull
    public static Builder builder(@NonNull String providerName, @NonNull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new Builder(providerName, version);
    }

    /**
     * Returns a convenient DataSource formatter that produces uri output.<p>
     * This formatter is thread-safe.
     *
     * @return a DataSource formatter
     */
    @NonNull
    public static Formatter<DataSource> uriFormatter() {
        return DataSource::formatAsUri;
    }

    /**
     * Returns a convenient DataSource parser that consumes uri input.<p>
     * This parser is thread-safe.
     *
     * @return a DataSource parser
     */
    @NonNull
    public static Parser<DataSource> uriParser() {
        return DataSource::parseAsUri;
    }

    public static class Builder implements IConfig.Builder<Builder, DataSource> {

        final String providerName;
        final String version;
        final Map<String, String> params;

        @VisibleForTesting
        Builder(String providerName, String version) {
            this.providerName = providerName;
            this.version = version;
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
        public DataSource build() {
            return new DataSource(providerName, version, SortedMaps.immutableCopyOf(params));
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final String SCHEME = "demetra";
    private static final String HOST = "tsprovider";

    private static DataSource parseAsUri(CharSequence input) {
        try {
            return parseAsUri(new URI(input.toString()));
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private static DataSource parseAsUri(URI uri) {
        if (!SCHEME.equals(uri.getScheme()) || !HOST.equals(uri.getHost())) {
            return null;
        }
        String[] path = UriBuilder.getPathArray(uri, 2);
        if (path == null) {
            return null;
        }
        Map<String, String> query = UriBuilder.getQueryMap(uri);
        if (query == null) {
            return null;
        }
        return new DataSource(path[0], path[1], SortedMaps.immutableCopyOf(query));
    }

    private static CharSequence formatAsUri(DataSource value) {
        return new UriBuilder(SCHEME, HOST)
                .path(value.getProviderName(), value.getVersion())
                .query(value.getParams())
                .buildString();
    }
    //</editor-fold>
}
