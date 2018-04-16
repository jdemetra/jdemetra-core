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
package ec.tss.tsproviders.jdbc.dsm.datasource;

import com.google.common.base.Preconditions;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import ec.tss.tsproviders.jdbc.dsm.datasource.interfaces.IManagedDataSource;
import ec.tss.tsproviders.jdbc.dsm.identification.AccountManager;
import ec.tss.tsproviders.jdbc.dsm.identification.aes.AESContentManager;
import ec.tss.tsproviders.jdbc.dsm.identification.aes.KeyGen;
import ec.tstoolkit.utilities.*;
import ioutil.Jaxb;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton; handles all the connection information to the databases and their
 * persistence. It replaces, in a much simpler way, the ODBC manager from
 * Windows.
 *
 * @author Demortier Jeremy
 */
@Deprecated
public enum DataSourceManager {

    INSTANCE;
    private final Map<String, Map<String, IManagedDataSource>> m_dataSources;
    private final ListMultimap<String, String> m_registered;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);
    final File defaultFile;

    private DataSourceManager() {
        m_dataSources = new HashMap<>();
        m_registered = ArrayListMultimap.create();
        File defaultFolder = Files2.fromPath(StandardSystemProperty.USER_HOME.value(), ".jdemetra");
        defaultFile = new File(defaultFolder, "datasources.xml");
        AccountManager.INSTANCE.setManager(new AESContentManager(defaultFolder, KeyGen.retrieveKeySpec()));

        // Register known types of databases
        registerDataSourceProvider(DataSourceType.MYSQL.getSourceQualifier(), "Server", "Database");
        registerDataSourceProvider(DataSourceType.ORACLE.getSourceQualifier(), "Server", "Port", "SID");

        load();
    }

    /**
     * Loads previously saved configuration
     *
     * @param path2file Path to the xml file containing the configuration
     */
    public void load(final File path2file) {

        // No point in trying to parse a file not existing
        if (!path2file.exists()) {
            return;
        }

        try {
            XmlDataSources root = Jaxb.Parser.of(XmlDataSources.class).parseFile(path2file);
            for (XmlDataSource xmlProvider : root.dataSources) {
                IManagedDataSource mds = new DefaultManagedDataSource(xmlProvider.provider, null);
                mds.setName(xmlProvider.dbName);
                for (XmlProperty o : xmlProvider.properties) {
                    mds.setProperty(o.name, o.value);
                }
                add(xmlProvider.provider, xmlProvider.dbName, mds);
            }
        } catch (Exception ex) {
            LOGGER.warn("While loading", ex);
        }
    }

    /**
     * Reloads previously saved configuration from the default xml file
     */
    public void load() {
        load(defaultFile);
    }

    public void save(final File path2file) {
        XmlDataSources root = new XmlDataSources();
        for (String provider : m_dataSources.keySet()) {
            for (String dbName : m_dataSources.get(provider).keySet()) {
                XmlDataSource xmlProvider = new XmlDataSource();
                xmlProvider.provider = provider;
                xmlProvider.dbName = dbName;
                IManagedDataSource mds = getManagedDataSource(provider, dbName);
                for (String property : mds.listProperties()) {
                    XmlProperty xmlProperty = new XmlProperty();
                    xmlProperty.name = property;
                    xmlProperty.value = mds.getProperty(property);
                }
                root.dataSources.add(xmlProvider);
            }
        }
        File parent = path2file.getParentFile();
        if (!parent.exists()) {
            Preconditions.checkState(parent.mkdir(), "Cannot create parent directory");
        }
        try {
            JAXBContext context = JAXBContext.newInstance(XmlDataSources.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(root, path2file);
        } catch (Exception ex) {
            LOGGER.error("While saving", ex);
        }
    }

    /**
     * Writes content into the default XML file.
     */
    public void save() {
        save(defaultFile);
    }

    public Set<String> getDatabases(final DataSourceType provider) {
        return getDatabases(provider.getSourceQualifier());
    }

    /**
     * Lists all the databases defined with this type of provider.
     *
     * @param providerQualifier Specified provider.
     * @return Names of the databases.
     */
    public Set<String> getDatabases(final String providerQualifier) {
        return m_dataSources.get(providerQualifier).keySet();
    }

    /**
     * Retrieves the connection information about a database in the manager.
     *
     * @param providerQualifier Specified provider.
     * @param dbName Name of the database in the manager.
     * @return The information corresponding to the values specified; null if
     * there was nothing to be found.
     */
    public IManagedDataSource getManagedDataSource(final String providerQualifier, final String dbName) {
        return m_dataSources.get(providerQualifier).get(dbName);
    }

    /**
     * Retrieves the map corresponding to a specified provider; if there was
     * none, creates and return a new one.
     *
     * @param providerQualifier Provider asked for.
     * @return Map corresponding to the provider; never null.
     */
    protected Map<String, IManagedDataSource> getMap(final String providerQualifier) {
        Map<String, IManagedDataSource> result = m_dataSources.get(providerQualifier);
        if (result == null) {
            result = new HashMap<>();
            m_dataSources.put(providerQualifier, result);
        }
        return result;
    }

    public void add(final String providerQualifier, final String dbName, final IManagedDataSource dsDetail) {
        getMap(providerQualifier).put(dbName, dsDetail);
    }

    public void remove(final String providerQualifier, final String dbName) {
        getMap(providerQualifier).remove(dbName);
    }

    /**
     * Transforms the hashmaps into a list used to create a TableModel.
     *
     * @return The content of the hashmaps in a single list.
     * @see DataSourceTableModel
     */
    public List<IManagedDataSource> toList() {
        List<IManagedDataSource> list = new ArrayList<>();

        for (String provider : m_dataSources.keySet()) {
            for (String dbName : m_dataSources.get(provider).keySet()) {
                list.add(getManagedDataSource(provider, dbName));
            }
        }

        return list;
    }

    public void registerDataSourceProvider(String providerQualifier, String... properties) {
        m_registered.putAll(providerQualifier, Arrays.asList(properties));
    }

    public void unregisterDataSourceProvider(final String providerQualifier) {
        m_registered.removeAll(providerQualifier);
    }

    /**
     * Return the providers currently loaded in the manager.
     *
     * @return
     */
    public Set<String> getProviders() {
        return m_dataSources.keySet();
    }

    /**
     * Returns the registered providers.
     *
     * @return
     */
    public Set<String> listDataSourceProviders() {
        return m_registered.keySet();
    }

    public List<String> listDataSourceProperties(final String provider) {
        return Collections.unmodifiableList(m_registered.get(provider));
    }

    @XmlRootElement(name = "DataSources")
    private static class XmlDataSources {

        @XmlElement(name = "DataSource")
        List<XmlDataSource> dataSources = new ArrayList<>();
    }

    private static class XmlDataSource {

        @XmlAttribute(name = "Provider")
        String provider = "";
        @XmlElement(name = "Name")
        String dbName = "";
        @XmlElement(name = "Property")
        List<XmlProperty> properties = new ArrayList<>();
    }

    private static class XmlProperty {

        @XmlAttribute(name = "PropertyName")
        String name = "";
        @XmlValue
        String value = "";
    }
}
