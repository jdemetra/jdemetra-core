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
package ec.tss.tsproviders.utils;

import com.google.common.base.Strings;
import com.google.common.cache.LoadingCache;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataMoniker;
import ec.tss.tsproviders.HasDataSourceMutableList;
import ec.tss.tsproviders.HasFilePaths;
import ec.tss.tsproviders.IDataSourceListener;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Files2;
import ec.tstoolkit.utilities.GuavaCaches;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
public class DataSourceSupport implements HasDataSourceMutableList, HasDataMoniker, HasFilePaths {

    @Nonnull
    public static DataSourceSupport create(@Nonnull String providerName, @Nonnull Logger logger) {
        return new DataSourceSupport(providerName, new LinkedHashSet<>(), DataSourceEventSupport.create(logger));
    }

    protected final String providerName;
    protected final Set<DataSource> dataSources;
    protected final List<DataSource> dataSourcesAsList;
    protected final DataSourceEventSupport eventSupport;
    protected final IConstraint<String> providerNameConstraint;
    private final HasFilePaths filePathsSupport;

    public DataSourceSupport(@Nonnull String providerName, @Nonnull Set<DataSource> dataSources, @Nonnull DataSourceEventSupport eventSupport) {
        this.providerName = providerName;
        this.dataSources = dataSources;
        this.dataSourcesAsList = new ArrayList(dataSources);
        this.eventSupport = eventSupport;
        this.providerNameConstraint = onProviderName(providerName);
        this.filePathsSupport = HasFilePaths.of();
    }

    @Nonnull
    public String getProviderName() {
        return providerName;
    }

    @Nonnull
    public DataSourceEventSupport getEventSupport() {
        return eventSupport;
    }

    //<editor-fold defaultstate="collapsed" desc="Preconditions">
    public boolean checkQuietly(@Nullable TsMoniker moniker) {
        if (moniker == null) {
            return false;
        }
        if (!doCheckQuietly(providerNameConstraint, moniker.getSource())) {
            return false;
        }
        return true;
    }

    @Nonnull
    public DataSource check(@Nonnull DataSource dataSource, IConstraint<DataSource>... constraints) throws IllegalArgumentException {
        doCheck(providerNameConstraint, dataSource.getProviderName());
        for (IConstraint<DataSource> o : constraints) {
            doCheck(o, dataSource);
        }
        return dataSource;
    }

    public boolean checkQuietly(@Nullable DataSource dataSource, IConstraint<DataSource>... constraints) {
        if (dataSource == null) {
            return false;
        }
        if (!doCheckQuietly(providerNameConstraint, dataSource.getProviderName())) {
            return false;
        }
        for (IConstraint<DataSource> o : constraints) {
            if (!doCheckQuietly(o, dataSource)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public DataSet check(@Nonnull DataSet dataSet, IConstraint<DataSet>... constraints) throws IllegalArgumentException {
        check(dataSet.getDataSource());
        for (IConstraint<DataSet> o : constraints) {
            doCheck(o, dataSet);
        }
        return dataSet;
    }

    public boolean checkQuietly(@Nullable DataSet dataSet, IConstraint<DataSet>... constraints) {
        if (dataSet == null) {
            return false;
        }
        if (!checkQuietly(dataSet.getDataSource())) {
            return false;
        }
        for (IConstraint<DataSet> o : constraints) {
            if (!doCheckQuietly(o, dataSet)) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    public <T> T checkBean(@Nonnull Object bean, final Class<T> clazz) {
        Objects.requireNonNull(bean);
        doCheck(new IConstraint<Object>() {
            @Override
            public String check(Object t) {
                return clazz.isInstance(t) ? null : "Not valid bean";
            }
        }, bean);
        return (T) bean;
    }

    <T> void doCheck(@Nonnull IConstraint<T> constraint, @Nonnull T value) throws IllegalArgumentException {
        String message = constraint.check(value);
        if (message != null) {
            eventSupport.logger.debug(message);
            throw new IllegalArgumentException(message);
        }
    }

    <T> boolean doCheckQuietly(@Nonnull IConstraint<T> constraint, @Nonnull T value) {
        String message = constraint.check(value);
        if (message != null) {
            eventSupport.logger.debug(message);
            return false;
        }
        return true;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasDataSourceMutableList">
    @Override
    public boolean open(DataSource dataSource) {
        check(dataSource);
        synchronized (dataSources) {
            if (dataSources.add(dataSource)) {
                dataSourcesAsList.add(dataSource);
                eventSupport.fireOpened(dataSource);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean close(DataSource dataSource) {
        check(dataSource);
        synchronized (dataSources) {
            if (dataSources.remove(dataSource)) {
                dataSourcesAsList.remove(dataSource);
                eventSupport.fireClosed(dataSource);
                return true;
            }
        }
        return false;
    }

    @Override
    public void closeAll() {
        synchronized (dataSources) {
            dataSources.clear();
            dataSourcesAsList.clear();
            eventSupport.fireAllClosed(providerName);
        }
    }

    @Override
    public List<DataSource> getDataSources() {
        synchronized (dataSources) {
            return Collections.unmodifiableList(dataSourcesAsList);
        }
    }

    @Override
    public void addDataSourceListener(IDataSourceListener listener) {
        eventSupport.add(listener);
    }

    @Override
    public void removeDataSourceListener(IDataSourceListener listener) {
        eventSupport.remove(listener);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasDataMoniker">
    @Override
    public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
        check(dataSet);
        return new TsMoniker(providerName, DataSet.uriFormatter().tryFormatAsString(dataSet).get());
    }

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        check(dataSource);
        return new TsMoniker(providerName, DataSource.uriFormatter().tryFormatAsString(dataSource).get());
    }

    @Override
    public DataSet toDataSet(TsMoniker moniker) {
        doCheck(providerNameConstraint, moniker.getSource());
        String id = moniker.getId();
        DataSet result = DataSet.uriParser().parse(id);
        return result != null ? result : DataSet.xmlParser().parse(id);
    }

    @Override
    public DataSource toDataSource(TsMoniker moniker) {
        doCheck(providerNameConstraint, moniker.getSource());
        String id = moniker.getId();
        DataSource result = DataSource.uriParser().parse(id);
        return result != null ? result : DataSource.xmlParser().parse(id);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasFilePaths">
    @Override
    public File[] getPaths() {
        return filePathsSupport.getPaths();
    }

    @Override
    public void setPaths(File[] paths) {
        filePathsSupport.setPaths(paths);
    }

    @Override
    public File resolveFilePath(File file) throws FileNotFoundException {
        return getRealFile(file);
    }
    //</editor-fold>

    @Nonnull
    public File getRealFile(@Nonnull File file) throws FileNotFoundException {
        return filePathsSupport.resolveFilePath(file);
    }

    @Nonnull
    public String getDisplayName(@Nonnull IOException ex) throws IllegalArgumentException {
        String name = ex.getClass().getSimpleName();
        String message = ex.getMessage();
        return !Strings.isNullOrEmpty(message) ? message : name;
    }

    @Deprecated
    @Nonnull
    public TsInformation fillSeries(@Nonnull TsInformation info, @Nonnull OptionalTsData data) {
        return fillSeries(info, data, true);
    }

    @Nonnull
    public TsInformation fillSeries(@Nonnull TsInformation info, @Nonnull OptionalTsData data, boolean cleanExtremities) {
        if (!data.isPresent()) {
            info.data = null;
            info.invalidDataCause = data.getCause();
        } else {
            TsData tmp = data.get();
            info.data = cleanExtremities && hasMissingValuesAtExtremities(tmp) ? tmp.cleanExtremities() : tmp;
            info.invalidDataCause = null;
        }
        return info;
    }

    @Nonnull
    public TsInformation fillSeries(@Nonnull TsInformation info, @Nonnull Exception exception) {
        eventSupport.logger.error("While getting series", exception);
        info.data = null;
        info.invalidDataCause = exception.getMessage();
        return info;
    }

    @Nonnull
    public TsCollectionInformation fillCollection(@Nonnull TsCollectionInformation info, @Nonnull Exception exception) {
        eventSupport.logger.error("While getting collection", exception);
        info.invalidDataCause = exception.getMessage();
        return info;
    }

    @Nonnull
    public <DATA> DATA getValue(@Nonnull LoadingCache<DataSource, DATA> cache, @Nonnull DataSource key) throws IOException {
        check(key);
        return GuavaCaches.getOrThrowIOException(cache, key);
    }

    @Nonnull
    public static File getRealFile(@Nonnull File[] paths, @Nonnull File file) throws FileNotFoundException {
        File result = Files2.getAbsoluteFile(paths, file);
        if (result == null) {
            throw new FileNotFoundException("Relative file '" + file.getPath() + "' outside paths");
        }
        if (!result.exists()) {
            throw new FileNotFoundException(result.getPath());
        }
        return result;
    }

    private static boolean hasMissingValuesAtExtremities(@Nonnull TsData data) {
        return !data.isEmpty() && (data.isMissing(0) || data.isMissing(data.getLength() - 1));
    }

    @Nonnull
    public static IConstraint<String> onProviderName(@Nonnull final String providerName) {
        return new IConstraint<String>() {
            @Override
            public String check(String t) {
                return providerName.equals(t) ? null : "Invalid provider name";
            }
        };
    }
}
