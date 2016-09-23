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
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSortedMap;
import ec.tss.tsproviders.utils.AbstractConfigBuilder;
import ec.tss.tsproviders.utils.Formatters;
import ec.tss.tsproviders.utils.Formatters.Formatter;
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
import javax.annotation.Nonnull;
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
 */
@Immutable
@XmlJavaTypeAdapter(DataSource.XmlAdapter.class)
public final class DataSource implements IConfig, Serializable {

    private final String providerName;
    private final String version;
    private final ImmutableSortedMap<String, String> params;

    @VisibleForTesting
    DataSource(@Nonnull String providerName, @Nonnull String version, @Nonnull ImmutableSortedMap<String, String> params) {
        this.providerName = providerName;
        this.version = version;
        this.params = params;
    }

    @Nonnull
    public String getProviderName() {
        return providerName;
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    @Override
    public String get(String key) {
        return params.get(key);
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

    @VisibleForTesting
    DataSourceBean toBean() {
        DataSourceBean bean = new DataSourceBean();
        bean.providerName = providerName;
        bean.version = version;
        bean.params = ParamBean.fromSortedMap(params);
        return bean;
    }

    @Nonnull
    public static DataSource deepCopyOf(@Nonnull String providerName, @Nonnull String version, @Nonnull Map<String, String> params) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(params, "params");
        return new DataSource(providerName, version, ImmutableSortedMap.copyOf(params));
    }

    @Nonnull
    public static Builder builder(@Nonnull String providerName, @Nonnull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new Builder(providerName, version);
    }

    @Nonnull
    public static Builder builder(@Nonnull DataSource dataSource) {
        Objects.requireNonNull(dataSource, "dataSource");
        return new Builder(dataSource.getProviderName(), dataSource.getVersion()).putAll(dataSource.getParams());
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
    @Nonnull
    public static Formatters.Formatter<DataSource> xmlFormatter(boolean formattedOutput) {
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
    @Nonnull
    public static Parsers.Parser<DataSource> xmlParser() {
        return XML.get().defaultParser;
    }

    /**
     * Returns a convenient DataSource formatter that produces uri output.<p>
     * This formatter is thread-safe.
     *
     * @return a DataSource formatter
     */
    @Nonnull
    public static Formatters.Formatter<DataSource> uriFormatter() {
        return URI_FORMATTER;
    }

    /**
     * Returns a convenient DataSource parser that consumes uri input.<p>
     * This parser is thread-safe.
     *
     * @return a DataSource parser
     */
    @Nonnull
    public static Parsers.Parser<DataSource> uriParser() {
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
            return DataSource.deepCopyOf(providerName, version, params);
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
    private static final ThreadLocal<Xml> XML = new ThreadLocal<Xml>() {
        @Override
        protected Xml initialValue() {
            return new Xml();
        }
    };

    private static final class Xml {

        final static JAXBContext BEAN_CONTEXT;

        static {
            try {
                BEAN_CONTEXT = JAXBContext.newInstance(DataSourceBean.class);
            } catch (JAXBException ex) {
                throw Throwables.propagate(ex);
            }
        }

        final Parsers.Parser<DataSource> defaultParser = Parsers.<DataSourceBean>onJAXB(BEAN_CONTEXT).compose(DataSourceBean::toId);
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
            String[] path = UriBuilder.getPathArray(uri);
            if (path == null || path.length != 2) {
                return null;
            }
            Map<String, String> query = UriBuilder.getQueryMap(uri);
            if (query == null) {
                return null;
            }
            return DataSource.builder(path[0], path[1]).putAll(query).build();
        }
    };

    private static final Formatters.Formatter<DataSource> URI_FORMATTER = new Formatter<DataSource>() {
        @Override
        public CharSequence format(DataSource value) {
            return new UriBuilder(SCHEME, HOST)
                    .path(value.getProviderName(), value.getVersion())
                    .query(value.getParams())
                    .buildString();
        }
    };
    //</editor-fold>
}
