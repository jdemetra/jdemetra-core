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

import demetra.design.Immutable;
import demetra.design.VisibleForTesting;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.ParamBean;
import demetra.util.UriBuilder;
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
import internal.util.Strings;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;

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
@lombok.ToString
@lombok.EqualsAndHashCode
public final class DataSource implements IConfig, Serializable {

    private final String providerName;
    private final String version;
    private final SortedMap<String, String> params;

    @VisibleForTesting
    DataSource(@Nonnull String providerName, @Nonnull String version, @Nonnull SortedMap<String, String> params) {
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
    public SortedMap<String, String> getParams() {
        return params;
    }

    /**
     * Creates a new builder with the content of this datasource.
     *
     * @return a non-null builder
     * @since 2.2.0
     */
    @Nonnull
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

    @Nonnull
    public static DataSource of(@Nonnull String providerName, @Nonnull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, Collections.emptySortedMap());
    }

    @Nonnull
    public static DataSource of(@Nonnull String providerName, @Nonnull String version, @Nonnull String key, @Nonnull String value) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, SortedMaps.immutableOf(key, value));
    }

    @Nonnull
    public static DataSource deepCopyOf(@Nonnull String providerName, @Nonnull String version, @Nonnull Map<String, String> params) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new DataSource(providerName, version, SortedMaps.immutableCopyOf(params));
    }

    @Nonnull
    public static Builder builder(@Nonnull String providerName, @Nonnull String version) {
        Objects.requireNonNull(providerName, "providerName");
        Objects.requireNonNull(version, "version");
        return new Builder(providerName, version);
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
    public static Formatter<DataSource> xmlFormatter(boolean formattedOutput) {
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
    public static Parser<DataSource> xmlParser() {
        return XML.get().defaultParser;
    }

    /**
     * Returns a convenient DataSource formatter that produces uri output.<p>
     * This formatter is thread-safe.
     *
     * @return a DataSource formatter
     */
    @Nonnull
    public static Formatter<DataSource> uriFormatter() {
        return DataSource::formatAsUri;
    }

    /**
     * Returns a convenient DataSource parser that consumes uri input.<p>
     * This parser is thread-safe.
     *
     * @return a DataSource parser
     */
    @Nonnull
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

        final Parser<DataSource> defaultParser = Parser.<DataSourceBean>onJAXB(BEAN_CONTEXT).andThen(DataSourceBean::toId);
        final Formatter<DataSource> defaultFormatter = Formatter.<DataSourceBean>onJAXB(BEAN_CONTEXT, false).compose(DataSource::toBean);
        final Formatter<DataSource> formattedOutputFormatter = Formatter.<DataSourceBean>onJAXB(BEAN_CONTEXT, true).compose(DataSource::toBean);
    }

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
