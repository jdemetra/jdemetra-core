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
package ec.tss.tsproviders.utils;

import com.google.common.collect.ImmutableList;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataSourceList;
import ec.tss.tsproviders.HasDataSourceMutableList;
import ec.tss.tsproviders.IDataSourceListener;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;

/**
 * Supporting class for {@link HasDataSourceList} and
 * {@link HasDataSourceMutableList}.
 *
 * @author Philippe Charles
 */
public final class DataSourceListSupport {

    @Nonnull
    public static HasDataSourceMutableList mutable(@Nonnull String providerName, @Nonnull Logger logger) {
        return new Impl(providerName, new LinkedHashSet<>(), DataSourceEventSupport.create(logger));
    }

    private DataSourceListSupport() {
        // static class
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class Impl implements HasDataSourceMutableList {

        private final String providerName;
        private final LinkedHashSet<DataSource> dataSources;
        private final DataSourceEventSupport eventSupport;

        private Impl(String providerName, LinkedHashSet<DataSource> dataSources, DataSourceEventSupport eventSupport) {
            this.providerName = providerName;
            this.dataSources = dataSources;
            this.eventSupport = eventSupport;
        }

        @Override
        public boolean open(DataSource dataSource) throws IllegalArgumentException {
            DataSourcePreconditions.checkProvider(providerName, dataSource);
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
            DataSourcePreconditions.checkProvider(providerName, dataSource);
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
    //</editor-fold>
}
