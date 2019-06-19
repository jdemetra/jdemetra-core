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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedMap;
import ec.tss.tsproviders.utils.AbstractConfigBuilder;
import ec.tss.tsproviders.utils.Formatters;
import ec.tss.tsproviders.utils.IConfig;
import ec.tss.tsproviders.utils.ParamBean;
import ec.tss.tsproviders.utils.Parsers;
import ec.tss.tsproviders.utils.Parsers.FailSafeParser;
import ec.tss.tsproviders.utils.UriBuilder;
import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.VisibleForTesting;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Simple structure that defines a source of data such as a file, a database or
 * any resource.<p>
 * This object doesn't hold data but only the parameters used to get the
 * data.<br>It is immutable and therefore thread-safe.<br>It is created by a
 * builder.<br>A default xml serializer is provided but its use is not
 * mandatory.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@Immutable
@XmlJavaTypeAdapter(DataSource.XmlAdapter.class)
public final class DataSource implements IConfig, Serializable {

    private final String providerName;
    private final String version;
    private final ImmutableSortedMap<String, String> params;

    @VisibleForTesting
    DataSource(@NonNull String providerName, @NonNull String version, @NonNull ImmutableSortedMap<String, String> params) {
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

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DataSource && equals((DataSource) obj));
    }

    private boolean equals(DataSource that) {
        return this.providerName.equals(that.providerName)
                && this.version.equals(that.version)
                && this.params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerName, version, params);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(providerName + "(" + version + ")");
        params.forEach(helper::add);
        return helper.toString();
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

    @VisibleForTesting
    DataSourceBean toBean() {
        DataSourceBean bean = new DataSourceBean();
        bean.providerName = providerName;
        bean.version = version;
        bean.params = ParamBean.fromSortedMap(params);
        return bean;
    }

    @NonNull
    public static DataSource of(@NonNull String providerName, @NonNull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, ImmutableSortedMap.of());
    }

    @NonNull
    public static DataSource of(@NonNull String providerName, @NonNull String version, @NonNull String key, @NonNull String value) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, ImmutableSortedMap.of(key, value));
    }

    @NonNull
    public static DataSource deepCopyOf(@NonNull String providerName, @NonNull String version, @NonNull Map<String, String> params) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, ImmutableSortedMap.copyOf(params));
    }

    @NonNull
    public static Builder builder(@NonNull String providerName, @NonNull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new Builder(providerName, version);
    }

    @Deprecated
    @NonNull
    public static Builder builder(@NonNull DataSource dataSource) {
        return dataSource.toBuilder();
    }

    /**
     * Returns a convenient DataSource formatter that produces xml output.<p>
     * This formatter is not thread-safe but unique per thread. To use it
     * thread-safely, don't store it but use it directly.
     * <br><code>DataSource.xmlFormatter().format(...)</code>
     *
     * @param formattedOutput
     * @see ThreadLocal
     * @return a DataSource formatter
     */
    public static Formatters.@NonNull Formatter<DataSource> xmlFormatter(boolean formattedOutput) {
        return formattedOutput ? XML.get().formattedOutputFormatter : XML.get().defaultFormatter;
    }

    /**
     * Returns a convenient DataSource parser that consumes xml input.<p>
     * This parser is not thread-safe but unique per thread. To use it
     * thread-safely, don't store it but use it directly.
     * <br><code>DataSource.xmlParser().parse(...)</code>
     *
     * @see ThreadLocal
     * @return a DataSource parser
     */
    public static Parsers.@NonNull Parser<DataSource> xmlParser() {
        return XML.get().defaultParser;
    }

    /**
     * Returns a convenient DataSource formatter that produces uri output.<p>
     * This formatter is thread-safe.
     *
     * @return a DataSource formatter
     */
    public static Formatters.@NonNull Formatter<DataSource> uriFormatter() {
        return URI_FORMATTER;
    }

    /**
     * Returns a convenient DataSource parser that consumes uri input.<p>
     * This parser is thread-safe.
     *
     * @return a DataSource parser
     */
    public static Parsers.@NonNull Parser<DataSource> uriParser() {
        return URI_PARSER;
    }

    public static class Builder extends AbstractConfigBuilder<Builder, DataSource> {

        final String providerName;
        final String version;

        @VisibleForTesting
        Builder(String providerName, String version) {
            this.providerName = providerName;
            this.version = version;
        }

        @Override
        public DataSource build() {
            return new DataSource(providerName, version, Util.toImmutable(params));
        }
    }

    public static class XmlAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<DataSourceBean, DataSource> {

        @Override
        public DataSource unmarshal(DataSourceBean v) throws Exception {
            return v.toId();
        }

        @Override
        public DataSourceBean marshal(DataSource v) throws Exception {
            return v.toBean();
        }
    }

    @XmlRootElement(name = "dataSource")
    public static class DataSourceBean {

        @XmlAttribute(name = "providerName")
        public String providerName;
        @XmlAttribute(name = "version")
        public String version;
        @XmlElement(name = "param")
        public ParamBean[] params;

        public DataSource toId() {
            return new DataSource(
                    Strings.nullToEmpty(providerName),
                    Strings.nullToEmpty(version),
                    ParamBean.toSortedMap(params));
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final ThreadLocal<Xml> XML = ThreadLocal.withInitial(Xml::new);

    private static final class Xml {

        final static JAXBContext BEAN_CONTEXT;

        static {
            try {
                BEAN_CONTEXT = JAXBContext.newInstance(DataSourceBean.class);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }

        final Parsers.Parser<DataSource> defaultParser = Parsers.wrap(Parsers.<DataSourceBean>onJAXB(BEAN_CONTEXT).andThen(DataSourceBean::toId));
        final Formatters.Formatter<DataSource> defaultFormatter = Formatters.<DataSourceBean>onJAXB(BEAN_CONTEXT, false).compose(DataSource::toBean);
        final Formatters.Formatter<DataSource> formattedOutputFormatter = Formatters.<DataSourceBean>onJAXB(BEAN_CONTEXT, true).compose(DataSource::toBean);
    }

    private static final String SCHEME = "demetra";
    private static final String HOST = "tsprovider";

    private static final Parsers.Parser<DataSource> URI_PARSER = new FailSafeParser<DataSource>() {
        @Override
        protected DataSource doParse(CharSequence input) throws Exception {
            URI uri = new URI(input.toString());
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
            return new DataSource(path[0], path[1], Util.toImmutable(query));
        }
    };

    private static final Formatters.Formatter<DataSource> URI_FORMATTER = Formatters.wrap(DataSource::formatAsUri);

    private static CharSequence formatAsUri(DataSource value) {
        return new UriBuilder(SCHEME, HOST)
                .path(value.getProviderName(), value.getVersion())
                .query(value.getParams())
                .buildString();
    }
    //</editor-fold>
}
