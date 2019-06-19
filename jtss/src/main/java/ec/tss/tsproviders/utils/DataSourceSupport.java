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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;

/**
 *
 * @author Philippe Charles
 */
public class DataSourceSupport implements HasDataSourceMutableList, HasDataMoniker, HasFilePaths {

    @NonNull
    public static DataSourceSupport create(@NonNull String providerName, @NonNull Logger logger) {
        return new DataSourceSupport(providerName, new LinkedHashSet<>(), DataSourceEventSupport.create(logger));
    }

    protected final String providerName;
    protected final Set<DataSource> dataSources;
    protected final List<DataSource> dataSourcesAsList;
    protected final DataSourceEventSupport eventSupport;
    protected final IConstraint<String> providerNameConstraint;
    private final HasFilePaths filePathsSupport;

    public DataSourceSupport(@NonNull String providerName, @NonNull Set<DataSource> dataSources, @NonNull DataSourceEventSupport eventSupport) {
        this.providerName = providerName;
        this.dataSources = dataSources;
        this.dataSourcesAsList = new ArrayList(dataSources);
        this.eventSupport = eventSupport;
        this.providerNameConstraint = onProviderName(providerName);
        this.filePathsSupport = HasFilePaths.of();
    }

    @NonNull
    public String getProviderName() {
        return providerName;
    }

    @NonNull
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

    @NonNull
    public DataSource check(@NonNull DataSource dataSource, IConstraint<DataSource>... constraints) throws IllegalArgumentException {
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

    @NonNull
    public DataSet check(@NonNull DataSet dataSet, IConstraint<DataSet>... constraints) throws IllegalArgumentException {
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

    @NonNull
    public <T> T checkBean(@NonNull Object bean, final Class<T> clazz) {
        Objects.requireNonNull(bean);
        doCheck(new IConstraint<Object>() {
            @Override
            public String check(Object t) {
                return clazz.isInstance(t) ? null : "Not valid bean";
            }
        }, bean);
        return (T) bean;
    }

    <T> void doCheck(@NonNull IConstraint<T> constraint, @NonNull T value) throws IllegalArgumentException {
        String message = constraint.check(value);
        if (message != null) {
            eventSupport.logger.debug(message);
            throw new IllegalArgumentException(message);
        }
    }

    <T> boolean doCheckQuietly(@NonNull IConstraint<T> constraint, @NonNull T value) {
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
    public void reload(DataSource dataSource) throws IllegalArgumentException {
        check(dataSource);
        eventSupport.fireChanged(dataSource);
    }

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
        return new TsMoniker(providerName, DataSet.uriFormatter().formatValueAsString(dataSet).get());
    }

    @Override
    public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
        check(dataSource);
        return new TsMoniker(providerName, DataSource.uriFormatter().formatValueAsString(dataSource).get());
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

    @NonNull
    public File getRealFile(@NonNull File file) throws FileNotFoundException {
        return filePathsSupport.resolveFilePath(file);
    }

    @Deprecated
    @NonNull
    public String getDisplayName(@NonNull IOException exception) throws IllegalArgumentException {
        String message = exception.getMessage();
        return !Strings.isNullOrEmpty(message) ? message : exception.getClass().getSimpleName();
    }

    @Deprecated
    @NonNull
    public TsInformation fillSeries(@NonNull TsInformation info, @NonNull OptionalTsData data) {
        return fillSeries(info, data, true);
    }

    @NonNull
    public TsInformation fillSeries(@NonNull TsInformation info, @NonNull OptionalTsData data, boolean cleanExtremities) {
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

    @Deprecated
    @NonNull
    public TsInformation fillSeries(@NonNull TsInformation info, @NonNull Exception exception) {
        reportException(info, exception, Exception::getMessage);
        return info;
    }

    public boolean reportException(@NonNull TsInformation info, @NonNull Exception ex, @NonNull IFormatter<? super IOException> formatter) {
        eventSupport.logger.info("Failed to get series", ex);
        info.data = null;
        info.invalidDataCause = ex instanceof IOException ? formatter.formatAsString((IOException) ex) : ex.getMessage();
        return false;
    }

    @Deprecated
    @NonNull
    public TsCollectionInformation fillCollection(@NonNull TsCollectionInformation info, @NonNull Exception exception) {
        reportException(info, exception, Exception::getMessage);
        return info;
    }

    public boolean reportException(@NonNull TsCollectionInformation info, @NonNull Exception ex, @NonNull IFormatter<? super IOException> formatter) {
        eventSupport.logger.info("Failed to get collection", ex);
        info.invalidDataCause = ex instanceof IOException ? formatter.formatAsString((IOException) ex) : ex.getMessage();
        return false;
    }

    @NonNull
    public <DATA> DATA getValue(@NonNull LoadingCache<DataSource, DATA> cache, @NonNull DataSource key) throws IOException {
        check(key);
        return GuavaCaches.getOrThrowIOException(cache, key);
    }

    @NonNull
    public static File getRealFile(@NonNull File[] paths, @NonNull File file) throws FileNotFoundException {
        File result = Files2.getAbsoluteFile(paths, file);
        if (result == null) {
            throw new FileNotFoundException("Relative file '" + file.getPath() + "' outside paths");
        }
        if (!result.exists()) {
            throw new FileNotFoundException(result.getPath());
        }
        return result;
    }

    private static boolean hasMissingValuesAtExtremities(@NonNull TsData data) {
        return !data.isEmpty() && (data.isMissing(0) || data.isMissing(data.getLength() - 1));
    }

    @NonNull
    public static IConstraint<String> onProviderName(@NonNull final String providerName) {
        return new IConstraint<String>() {
            @Override
            public String check(String t) {
                return providerName.equals(t) ? null : "Invalid provider name";
            }
        };
    }
}
