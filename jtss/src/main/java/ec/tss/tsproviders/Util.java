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
package ec.tss.tsproviders;

import com.google.common.collect.ImmutableList;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.utils.DataSourceEventSupport;
import ec.tss.tsproviders.utils.DataSourcePreconditions;
import ec.tss.tsproviders.utils.IConfig;
import ec.tss.tsproviders.utils.IFormatter;
import ec.tss.tsproviders.utils.IParam;
import ec.tss.tsproviders.utils.IParser;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Package-private supporting class for ts providers.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
final class Util {

    private Util() {
        // static class
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

    static final class DataDisplayNameSupport extends ProviderPart implements HasDataDisplayName {

        private final IFormatter<DataSource> dataSourceFormatter;
        private final IFormatter<DataSet> dataSetFormatter;

        DataDisplayNameSupport(String providerName, IFormatter<DataSource> dataSourceFormatter, IFormatter<DataSet> dataSetFormatter) {
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

    static final class DataMonikerSupport extends ProviderPart implements HasDataMoniker {

        private final IFormatter<DataSource> dataSourceFormatter;
        private final IFormatter<DataSet> dataSetFormatter;
        private final IParser<DataSource> dataSourceParser;
        private final IParser<DataSet> dataSetParser;

        DataMonikerSupport(String providerName, IFormatter<DataSource> dataSourceFormatter, IFormatter<DataSet> dataSetFormatter, IParser<DataSource> dataSourceParser, IParser<DataSet> dataSetParser) {
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
            return new TsMoniker(providerName, id);
        }

        @Override
        public TsMoniker toMoniker(DataSet dataSet) throws IllegalArgumentException {
            checkProvider(dataSet);
            String id = dataSetFormatter.formatAsString(dataSet);
            if (id == null) {
                throw new IllegalArgumentException("Cannot format DataSource");
            }
            return new TsMoniker(providerName, id);
        }

        @Override
        public DataSet toDataSet(TsMoniker moniker) throws IllegalArgumentException {
            checkProvider(moniker);
            String id = moniker.getId();
            return id != null ? dataSetParser.parse(id) : null;
        }

        @Override
        public DataSource toDataSource(TsMoniker moniker) throws IllegalArgumentException {
            checkProvider(moniker);
            String id = moniker.getId();
            return id != null ? dataSourceParser.parse(id) : null;
        }
    }

    static final class DataSourceBeanSupport<T> extends ProviderPart implements HasDataSourceBean<T> {

        private final IParam<DataSource, T> param;
        private final String version;

        DataSourceBeanSupport(String providerName, IParam<DataSource, T> param, String version) {
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
                IConfig.Builder<?, DataSource> builder = DataSource.builder(providerName, version);
                param.set(builder, (T) bean);
                return builder.build();
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

    static final class DataSourceListSupport extends ProviderPart implements HasDataSourceList {

        private final List<DataSource> dataSources;

        DataSourceListSupport(String providerName, Iterable<DataSource> dataSources) {
            super(providerName);
            this.dataSources = ImmutableList.copyOf(dataSources);
            this.dataSources.forEach(this::checkProvider);
        }

        @Override
        public List<DataSource> getDataSources() {
            return dataSources;
        }

        @Override
        public void addDataSourceListener(IDataSourceListener listener) {
            Objects.requireNonNull(listener);
        }

        @Override
        public void removeDataSourceListener(IDataSourceListener listener) {
            Objects.requireNonNull(listener);
        }
    }

    static final class DataSourceMutableListSupport extends ProviderPart implements HasDataSourceMutableList {

        private final LinkedHashSet<DataSource> dataSources;
        private final DataSourceEventSupport eventSupport;

        DataSourceMutableListSupport(String providerName, LinkedHashSet<DataSource> dataSources, DataSourceEventSupport eventSupport) {
            super(providerName);
            this.dataSources = Objects.requireNonNull(dataSources);
            this.eventSupport = Objects.requireNonNull(eventSupport);
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
                return ImmutableList.copyOf(dataSources);
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
    }

    static final class FilePathSupport implements HasFilePaths {

        private static final File[] EMPTY = new File[0];

        private final Runnable onPathsChange;
        private final AtomicReference<File[]> paths;

        FilePathSupport(Runnable onPathsChange) {
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

    static final class NoOpDataHierarchy extends ProviderPart implements HasDataHierarchy {

        NoOpDataHierarchy(String providerName) {
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
}
