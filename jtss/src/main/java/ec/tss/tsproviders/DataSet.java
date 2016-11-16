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
package ec.tss.tsproviders;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSortedMap;
import ec.tss.tsproviders.DataSource.DataSourceBean;
import ec.tss.tsproviders.utils.*;
import ec.tss.tsproviders.utils.Parsers.FailSafeParser;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.VisibleForTesting;
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
public final class DataSet implements IConfig, Serializable {

    /**
     * Defines a DataSet kind.
     */
    public enum Kind implements IConstraint<DataSet> {

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

        @Override
        public String check(DataSet t) {
            return t.getKind().equals(this) ? null : String.format("Invalid DataSet Kind. Expected:%s Found:%s", this, t.getKind());
        }
    };
    private final DataSource dataSource;
    private final Kind kind;
    private final ImmutableSortedMap<String, String> params;

    @VisibleForTesting
    DataSet(@Nonnull DataSource dataSource, @Nonnull Kind kind, @Nonnull ImmutableSortedMap<String, String> params) {
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

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DataSet && equals((DataSet) obj));
    }

    private boolean equals(DataSet that) {
        return this.dataSource.equals(that.dataSource)
                && this.kind.equals(that.kind)
                && this.params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSource, kind, params);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(dataSource + "(" + kind + ")");
        params.forEach(helper::add);
        return helper.toString();
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
    public static DataSet deepCopyOf(@Nonnull DataSource dataSource, @Nonnull Kind kind, @Nonnull Map<String, String> params) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(params, "params");
        return new DataSet(dataSource, kind, ImmutableSortedMap.copyOf(params));
    }

    @Nonnull
    public static Builder builder(@Nonnull DataSource dataSource, @Nonnull Kind kind) {
        Objects.requireNonNull(dataSource, "dataSource");
        Objects.requireNonNull(kind, "kind");
        return new Builder(dataSource, kind);
    }

    @Deprecated
    @Nonnull
    public static Builder builder(@Nonnull DataSet dataSet, @Nonnull Kind kind) {
        return dataSet.toBuilder(kind);
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
    public static Formatters.Formatter<DataSet> xmlFormatter(boolean formattedOutput) {
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
    public static Parsers.Parser<DataSet> xmlParser() {
        return XML.get().defaultParser;
    }

    /**
     * Returns a convenient DataSet formatter that produces uri output.<p>
     * This formatter is thread-safe.
     *
     * @return a DataSource formatter
     */
    @Nonnull
    public static Formatters.Formatter<DataSet> uriFormatter() {
        return URI_FORMATTER;
    }

    /**
     * Returns a convenient DataSet parser that consumes uri input.<p>
     * This parser is thread-safe.
     *
     * @return a DataSource parser
     */
    @Nonnull
    public static Parsers.Parser<DataSet> uriParser() {
        return URI_PARSER;
    }

    public static class Builder extends AbstractConfigBuilder<Builder, DataSet> {

        final DataSource dataSource;
        final Kind kind;

        @VisibleForTesting
        Builder(DataSource dataSource, Kind kind) {
            this.dataSource = dataSource;
            this.kind = kind;
        }

        @Override
        public DataSet build() {
            return DataSet.deepCopyOf(dataSource, kind, params);
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
                throw Throwables.propagate(ex);
            }
        }

        final Parsers.Parser<DataSet> defaultParser = Parsers.wrap(Parsers.<DataSetBean>onJAXB(BEAN_CONTEXT).andThen(DataSetBean::toId));
        final Formatters.Formatter<DataSet> defaultFormatter = Formatters.wrap(Formatters.<DataSetBean>onJAXB(BEAN_CONTEXT, false).compose2(DataSet::toBean));
        final Formatters.Formatter<DataSet> formattedOutputFormatter = Formatters.wrap(Formatters.<DataSetBean>onJAXB(BEAN_CONTEXT, true).compose2(DataSet::toBean));
    }

    private static final String SCHEME = "demetra";
    private static final String HOST = "tsprovider";

    private static final Parsers.Parser<DataSet> URI_PARSER = new FailSafeParser<DataSet>() {
        @Override
        protected DataSet doParse(CharSequence input) throws Exception {
            URI uri = new URI(input.toString());
            if (!SCHEME.equals(uri.getScheme()) || !HOST.equals(uri.getHost())) {
                return null;
            }
            String[] path = UriBuilder.getPathArray(uri);
            if (path == null || path.length != 3) {
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
            DataSource dataSource = DataSource.deepCopyOf(path[0], path[1], query);
            return DataSet.deepCopyOf(dataSource, Kind.valueOf(path[2]), fragment);
        }
    };

    private static final Formatters.Formatter<DataSet> URI_FORMATTER = Formatters.wrap(DataSet::formatAsUri);

    private static CharSequence formatAsUri(DataSet value) {
        DataSource dataSource = value.getDataSource();
        return new UriBuilder(SCHEME, HOST)
                .path(dataSource.getProviderName(), dataSource.getVersion(), value.getKind().name())
                .query(dataSource.getParams())
                .fragment(value.getParams())
                .build().toString();
    }
    //</editor-fold>
}
