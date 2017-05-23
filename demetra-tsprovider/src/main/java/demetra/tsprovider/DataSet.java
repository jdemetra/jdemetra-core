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
import demetra.tsprovider.util.ParamBean;
import demetra.tsprovider.util.IConfig;
import demetra.design.Immutable;
import demetra.design.VisibleForTesting;
import demetra.tsprovider.DataSource.DataSourceBean;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
@XmlJavaTypeAdapter(DataSet.XmlAdapter.class)
@lombok.ToString
@lombok.EqualsAndHashCode
public final class DataSet implements IConfig, Serializable {

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

    @VisibleForTesting
    DataSetBean toBean() {
        DataSetBean bean = new DataSetBean();
        bean.dataSource = dataSource.toBean();
        bean.kind = kind;
        bean.params = ParamBean.fromSortedMap(params);
        return bean;
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
     * Returns a convenient DataSet formatter that produces xml output.<p>
     * This formatter is not thread-safe but unique per thread. To use it
     * thread-safely, don't store it but use it directly.
     * <br><code>DataSet.xmlFormatter().format(...)</code>
     *
     * @param formattedOutput
     * @see ThreadLocal
     * @return a DataSet formatter
     */
    @Nonnull
    public static Formatter<DataSet> xmlFormatter(boolean formattedOutput) {
        return formattedOutput ? XML.get().formattedOutputFormatter : XML.get().defaultFormatter;
    }

    /**
     * Returns a convenient DataSet parser that consumes xml input.<p>
     * This parser is not thread-safe but unique per thread. To use it
     * thread-safely, don't store it but use it directly.
     * <br><code>DataSet.xmlParser().parse(...)</code>
     *
     * @see ThreadLocal
     * @return a DataSet parser
     */
    @Nonnull
    public static Parser<DataSet> xmlParser() {
        return XML.get().defaultParser;
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

    public static class XmlAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<DataSetBean, DataSet> {

        @Override
        public DataSet unmarshal(DataSetBean v) throws Exception {
            return v.toId();
        }

        @Override
        public DataSetBean marshal(DataSet v) throws Exception {
            return v.toBean();
        }
    }

    @XmlRootElement(name = "dataSet")
    public static class DataSetBean {

        @XmlElement(name = "dataSource")
        public DataSourceBean dataSource;
        @XmlAttribute(name = "kind")
        public Kind kind;
        @XmlElement(name = "param")
        public ParamBean[] params;

        public DataSet toId() {
            return new DataSet(
                    dataSource.toId(),
                    kind != null ? kind : Kind.DUMMY,
                    ParamBean.toSortedMap(params));
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final ThreadLocal<Xml> XML = ThreadLocal.withInitial(Xml::new);

    private static final class Xml {

        final static JAXBContext BEAN_CONTEXT;

        static {
            try {
                BEAN_CONTEXT = JAXBContext.newInstance(DataSetBean.class);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }

        final Parser<DataSet> defaultParser = Parser.<DataSetBean>onJAXB(BEAN_CONTEXT).andThen(DataSetBean::toId);
        final Formatter<DataSet> defaultFormatter = Formatter.<DataSetBean>onJAXB(BEAN_CONTEXT, false).compose(DataSet::toBean);
        final Formatter<DataSet> formattedOutputFormatter = Formatter.<DataSetBean>onJAXB(BEAN_CONTEXT, true).compose(DataSet::toBean);
    }

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
