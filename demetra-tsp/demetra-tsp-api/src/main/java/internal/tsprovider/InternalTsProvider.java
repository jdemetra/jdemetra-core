/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
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
package internal.tsprovider;

import demetra.timeseries.TsMoniker;
import demetra.tsprovider.*;
import demetra.tsprovider.util.DataSourcePreconditions;
import demetra.tsprovider.util.Param;
import demetra.util.List2;
import internal.util.Strings;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

/**
 * Package-private supporting class for ts providers.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@lombok.experimental.UtilityClass
public class InternalTsProvider {

    public static final Consumer<Object> DO_NOTHING = o -> {
    };

    public static String getDisplayNameFromMessageOrClassName(IOException exception) {
        if (exception instanceof FileNotFoundException) {
            return "File not found: " + exception.getMessage();
        }
        String message = exception.getMessage();
        return !Strings.isNullOrEmpty(message) ? message : exception.getClass().getSimpleName();
    }

    private static abstract class ProviderPart {

        protected final String providerName;

        ProviderPart(String providerName) {
            this.providerName = Objects.requireNonNull(providerName);
        }

        protected void checkProvider(DataSource dataSource) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(providerName, dataSource);
        }

        protected void checkProvider(DataSet dataSet) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(providerName, dataSet);
        }

        protected void checkProvider(TsMoniker moniker) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(providerName, moniker);
        }
    }

    public static final class DataDisplayNameSupport extends ProviderPart implements HasDataDisplayName {

        private final Formatter<DataSource> dataSourceFormatter;
        private final Formatter<DataSet> dataSetFormatter;

        public DataDisplayNameSupport(String providerName, Formatter<DataSource> dataSourceFormatter, Formatter<DataSet> dataSetFormatter) {
            super(providerName);
            this.dataSourceFormatter = Objects.requireNonNull(dataSourceFormatter);
            this.dataSetFormatter = Objects.requireNonNull(dataSetFormatter);
        }

        @Override
        public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
            checkProvider(dataSource);
            String result = dataSourceFormatter.formatAsString(dataSource);
            if (result == null) {
                throw new IllegalArgumentException("Cannot format DataSource");
            }
            return result;
        }

        @Override
        public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
            checkProvider(dataSet);
            String result = dataSetFormatter.formatAsString(dataSet);
            if (result == null) {
                throw new IllegalArgumentException("Cannot format DataSet");
            }
            return result;
        }
    }

    public static final class DataMonikerSupport extends ProviderPart implements HasDataMoniker {

        private final Formatter<DataSource> dataSourceFormatter;
        private final Formatter<DataSet> dataSetFormatter;
        private final Parser<DataSource> dataSourceParser;
        private final Parser<DataSet> dataSetParser;

        public DataMonikerSupport(String providerName, Formatter<DataSource> dataSourceFormatter, Formatter<DataSet> dataSetFormatter, Parser<DataSource> dataSourceParser, Parser<DataSet> dataSetParser) {
            super(providerName);
            this.dataSourceFormatter = Objects.requireNonNull(dataSourceFormatter);
            this.dataSetFormatter = Objects.requireNonNull(dataSetFormatter);
            this.dataSourceParser = Objects.requireNonNull(dataSourceParser);
            this.dataSetParser = Objects.requireNonNull(dataSetParser);
        }

        @Override
        public TsMoniker toMoniker(DataSource dataSource) throws IllegalArgumentException {
            checkProvider(dataSource);
            String id = dataSourceFormatter.formatAsString(dataSource);
            if (id == null) {
                throw new IllegalArgumentException("Cannot format DataSource");
            }
            return TsMoniker.of(providerName, id);
        }

        @Override
        public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
            checkProvider(dataSet);
            String id = dataSetFormatter.formatAsString(dataSet);
            if (id == null) {
                throw new IllegalArgumentException("Cannot format DataSource");
            }
            return TsMoniker.of(providerName, id);
        }

        @Override
        public Optional<DataSet> toDataSet(TsMoniker moniker) throws IllegalArgumentException {
            checkProvider(moniker);
            return dataSetParser.parseValue(moniker.getId());
        }

        @Override
        public Optional<DataSource> toDataSource(TsMoniker moniker) throws IllegalArgumentException {
            checkProvider(moniker);
            return dataSourceParser.parseValue(moniker.getId());
        }
    }

    public static final class DataSourceBeanSupport<T> extends ProviderPart implements HasDataSourceBean<T> {

        private final Param<DataSource, T> param;
        private final String version;

        public DataSourceBeanSupport(String providerName, Param<DataSource, T> param, String version) {
            super(providerName);
            this.param = Objects.requireNonNull(param);
            this.version = Objects.requireNonNull(version);
        }

        @Override
        public T newBean() {
            return param.defaultValue();
        }

        @Override
        public DataSource encodeBean(Object bean) throws IllegalArgumentException {
            Objects.requireNonNull(bean);
            try {
                return DataSource.builder(providerName, version)
                        .put(param, (T) bean)
                        .build();
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public T decodeBean(DataSource dataSource) throws IllegalArgumentException {
            checkProvider(dataSource);
            return param.get(dataSource);
        }
    }

    public static final class DataSourceListSupport extends ProviderPart implements HasDataSourceList {

        private final List<DataSource> dataSources;
        private final DataSourceEventSupport eventSupport;
        private final Consumer<? super DataSource> cacheCleaner;

        public DataSourceListSupport(String providerName, Iterable<DataSource> dataSources, Consumer<? super DataSource> cacheCleaner) {
            super(providerName);
            this.dataSources = StreamSupport.stream(dataSources.spliterator(), false).collect(List2.toUnmodifiableList());
            this.eventSupport = DataSourceEventSupport.create();
            this.cacheCleaner = Objects.requireNonNull(cacheCleaner);
            dataSources.forEach(this::checkProvider);
        }

        @Override
        public void reload(DataSource dataSource) {
            checkProvider(dataSource);
            cacheCleaner.accept(dataSource);
            eventSupport.fireChanged(dataSource);
        }

        @Override
        public List<DataSource> getDataSources() {
            return dataSources;
        }

        @Override
        public void addDataSourceListener(DataSourceListener listener) {
            eventSupport.add(listener);
        }

        @Override
        public void removeDataSourceListener(DataSourceListener listener) {
            eventSupport.remove(listener);
        }
    }

    public static final class DataSourceMutableListSupport extends ProviderPart implements HasDataSourceMutableList {

        private final LinkedHashSet<DataSource> dataSources;
        private final DataSourceEventSupport eventSupport;
        private final Consumer<? super DataSource> cacheCleaner;

        public DataSourceMutableListSupport(String providerName, LinkedHashSet<DataSource> dataSources, Consumer<? super DataSource> cacheCleaner) {
            super(providerName);
            this.dataSources = Objects.requireNonNull(dataSources);
            this.eventSupport = DataSourceEventSupport.create();
            this.cacheCleaner = Objects.requireNonNull(cacheCleaner);
        }

        @Override
        public void reload(DataSource dataSource) {
            checkProvider(dataSource);
            cacheCleaner.accept(dataSource);
            eventSupport.fireChanged(dataSource);
        }

        @Override
        public boolean open(DataSource dataSource) throws IllegalArgumentException {
            checkProvider(dataSource);
            synchronized (dataSources) {
                if (dataSources.add(dataSource)) {
                    eventSupport.fireOpened(dataSource);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean close(DataSource dataSource) throws IllegalArgumentException {
            checkProvider(dataSource);
            synchronized (dataSources) {
                if (dataSources.remove(dataSource)) {
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
                eventSupport.fireAllClosed(providerName);
            }
        }

        @Override
        public List<DataSource> getDataSources() {
            synchronized (dataSources) {
                return List2.copyOf(dataSources);
            }
        }

        @Override
        public void addDataSourceListener(DataSourceListener listener) {
            eventSupport.add(listener);
        }

        @Override
        public void removeDataSourceListener(DataSourceListener listener) {
            eventSupport.remove(listener);
        }
    }

    public static final class FilePathSupport implements HasFilePaths {

        private static final File[] EMPTY = new File[0];

        private final Runnable onPathsChange;
        private final AtomicReference<File[]> paths;

        public FilePathSupport(Runnable onPathsChange) {
            this.onPathsChange = Objects.requireNonNull(onPathsChange);
            this.paths = new AtomicReference<>(EMPTY);
        }

        @Override
        public void setPaths(File[] paths) {
            File[] newValue = paths != null ? paths.clone() : EMPTY;
            if (!Arrays.equals(this.paths.getAndSet(newValue), newValue)) {
                onPathsChange.run();
            }
        }

        @Override
        public File[] getPaths() {
            return paths.get().clone();
        }
    }

    public static final class NoOpDataHierarchy extends ProviderPart implements HasDataHierarchy {

        public NoOpDataHierarchy(String providerName) {
            super(providerName);
        }

        @Override
        public List<DataSet> children(DataSource dataSource) throws IllegalArgumentException, IOException {
            checkProvider(dataSource);
            return Collections.emptyList();
        }

        @Override
        public List<DataSet> children(DataSet parent) throws IllegalArgumentException, IOException {
            checkProvider(parent);
            return Collections.emptyList();
        }
    }

    @lombok.extern.java.Log
    private static final class DataSourceEventSupport {

        /**
         * Creates a new DataSourceEventSupport that uses WeakReferences to
         * allows listeners to be garbage-collected and is thread-safe
         *
         * @return
         */
        @NonNull
        public static DataSourceEventSupport create() {
            Set<DataSourceListener> weakHashSet = Collections.newSetFromMap(new WeakHashMap<>());
            return new DataSourceEventSupport(Collections.synchronizedSet(weakHashSet));
        }

        private final Set<DataSourceListener> listeners;

        private DataSourceEventSupport(@NonNull Set<DataSourceListener> listeners) {
            this.listeners = listeners;
        }

        public void add(@NonNull DataSourceListener listener) {
            listeners.add(Objects.requireNonNull(listener));
        }

        public void remove(@NonNull DataSourceListener listener) {
            listeners.remove(Objects.requireNonNull(listener));
        }

        void fireOpened(@NonNull DataSource dataSource) {
            listeners.forEach((o) -> {
                try {
                    o.opened(dataSource);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "While sending open event", ex);
                }
            });
        }

        void fireClosed(@NonNull DataSource dataSource) {
            listeners.forEach((o) -> {
                try {
                    o.closed(dataSource);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "While sending close event", ex);
                }
            });
        }

        void fireAllClosed(@NonNull String providerName) {
            listeners.forEach((o) -> {
                try {
                    o.allClosed(providerName);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "While sending closeall event", ex);
                }
            });
        }

        void fireChanged(@NonNull DataSource dataSource) {
            listeners.forEach((o) -> {
                try {
                    o.changed(dataSource);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "While sending change event", ex);
                }
            });
        }
    }
}
