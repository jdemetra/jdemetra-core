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
package ec.tss.tsproviders.common.uscb;

import com.google.common.base.Optional;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceListener;
import ec.tss.tsproviders.IDataSourceProvider;
import ec.tss.tsproviders.legacy.FileDataSourceId;
import ec.tss.tsproviders.legacy.InvalidMonikerException;
import ec.tss.tsproviders.utils.DataSourceSupport;
import ec.tss.tsproviders.utils.Parsers;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Files2;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kristof Bayens
 */
public class UscbProvider implements ITsProvider, IDataSourceProvider {

    public static final String SOURCE = "USCB";
    public static final String VERSION = "20111201";
    public static final String X_FILE = "file";
    private static final Logger LOGGER = LoggerFactory.getLogger(UscbProvider.class);
    private static final File DEFAULT_FOLDER = Files2.fromPath(StandardSystemProperty.USER_HOME.value(), "Data", "USCB");
    private final Cache<FileDataSourceId, UscbAccessor> m_accessors;
    private final DataSourceSupport support;
    private final Parsers.Parser<DataSource> legacyDataSourceParser;

    public UscbProvider() {
        m_accessors = CacheBuilder.newBuilder().softValues().build();
        legacyDataSourceParser = FileDataSourceId.legacyParser(SOURCE, VERSION);
        support = DataSourceSupport.create(SOURCE, LOGGER);
        openAll();
    }

    final void openAll() {
        File folder = new File(this.getFolder());
        String[] files = folder.list();
        if (files != null) {
            for (String file : files) {
                Optional<DataSource> dataSource = legacyDataSourceParser.tryParse(file);
                if (dataSource.isPresent()) {
                    support.open(dataSource.get());
                }
            }
        }
    }

    public String getFolder() {
        String folder = "";

        //TODO: check if user has defined a default location.
        if (Strings.isNullOrEmpty(folder)) {
            folder = DEFAULT_FOLDER.getAbsolutePath();
        }
        return folder;
    }

    // IDataSourceProvider methods
    @Override
    public String getDisplayName() {
        return "USCB";
    }

    @Override
    public List<DataSource> getDataSources() {
        return support.getDataSources();
    }

    @Override
    public String getDisplayName(DataSource dataSource) {
        support.check(dataSource);
        return new File(dataSource.get(X_FILE)).getName();
    }

    @Override
    public String getDisplayName(DataSet dataSet) {
        return getDisplayName(dataSet.getDataSource());
    }

    @Override
    public List<DataSet> children(DataSource dataSource) {
        support.check(dataSource);
        return Collections.singletonList(DataSet.builder(dataSource, DataSet.Kind.SERIES).build());
    }

    @Override
    public String getDisplayName(IOException exception) throws IllegalArgumentException {
        return support.getDisplayName(exception);
    }

    @Override
    public void addDataSourceListener(IDataSourceListener listener) {
        support.addDataSourceListener(listener);
    }

    @Override
    public void removeDataSourceListener(IDataSourceListener listener) {
        support.removeDataSourceListener(listener);
    }

    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        return new TsMoniker(getSource(), dataSet.getDataSource().get(X_FILE));
    }

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        return support.toMoniker(dataSource);
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) {
        return null;
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) {
        DataSource result = support.toDataSource(moniker);
        return result != null ? result : legacyDataSourceParser.parse(moniker.getId());
    }
    // --> IDataSourceProvider

    private UscbAccessor getAccessor(FileDataSourceId source) {
        UscbAccessor result = m_accessors.getIfPresent(source);
        if (null == result) {
            result = new UscbAccessor(source);
            m_accessors.put(source, result);
        }
        return result;
    }

    // ITSProvider methods
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public void clearCache() {
        m_accessors.invalidateAll();
    }

    @Override
    public void dispose() {
        clearCache();
    }

    @Override
    public TsAsyncMode getAsyncMode() {
        return TsAsyncMode.None;
    }

    @Override
    public boolean queryTsCollection(TsMoniker tsm, TsInformationType tsit) {
        return false;
    }

    @Override
    public boolean get(TsCollectionInformation tsci) {
        if (!tsci.moniker.getSource().equals(getSource())) {
            return false;
        }

        String identifier = tsci.moniker.getId();
        String fullpath = getFolder() + identifier;
        tsci.name = tsci.moniker.getId();
        String[] files = new File(fullpath).list();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                String cur = new File(files[i]).getName();
                String id = "";
                if (identifier == null) {
                    id = cur;
                } else {
                    File file1 = new File(identifier);
                    File file2 = new File(file1, cur);
                    id = file2.getPath();
                }
                TsMoniker moniker = new TsMoniker(getSource(), id);
                TsInformation sinfo = new TsInformation(cur, moniker, tsci.type);
                if (get(sinfo)) {
                    tsci.items.add(sinfo);
                }
            }
        }
        return true;
    }

    @Override
    public boolean queryTs(TsMoniker tsm, TsInformationType tsit) {
        return false;
    }

    @Override
    public boolean get(TsInformation tsi) {
        try {
            FromUscbId id = new FromUscbId(tsi.moniker.getId());
            FileDataSourceId sourceId = FileDataSourceId.from(new File(id.getFileName()));
            UscbAccessor acc = getAccessor(sourceId);

            if (tsi.type.intValue() >= TsInformationType.Data.intValue()) {
                TsData data = acc.read(this.getFolder());
                if (data != null) {
                    tsi.data = data;
                } else {
                    return false;
                }
            }
            return true;
        } catch (InvalidMonikerException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }
    // --> ITSProvider

    @Override
    public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        return getDisplayName(dataSet);
    }

    @Override
    public void reload(DataSource dataSource) {
    }
}
