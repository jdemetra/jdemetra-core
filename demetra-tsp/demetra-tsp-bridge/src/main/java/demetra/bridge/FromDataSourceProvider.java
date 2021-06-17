/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.bridge;

import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceListener;
import demetra.tsprovider.DataSourceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public class FromDataSourceProvider extends FromTsProvider implements ec.tss.tsproviders.IDataSourceProvider {

    public static ec.tss.tsproviders.@NonNull IDataSourceProvider fromDataSourceProvider(@NonNull DataSourceProvider delegate) {
        return delegate instanceof ToDataSourceProvider
                ? ((ToDataSourceProvider) delegate).getDelegate()
                : new FromDataSourceProvider(delegate);
    }

    private final DataSourceListener changeListener;

    protected FromDataSourceProvider(DataSourceProvider delegate) {
        super(delegate);
        this.changeListener = new ChangeListener(delegate);
        delegate.addDataSourceListener(changeListener);
    }

    @Override
    public @NonNull DataSourceProvider getDelegate() {
        return (DataSourceProvider) super.getDelegate();
    }

    @Override
    public void reload(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        getDelegate().reload(TsConverter.toDataSource(dataSource));
    }

    @Override
    public @NonNull String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public @NonNull List<ec.tss.tsproviders.DataSource> getDataSources() {
        return getDelegate()
                .getDataSources()
                .stream()
                .map(TsConverter::fromDataSource)
                .collect(Collectors.toList());
    }

    @Override
    public void addDataSourceListener(ec.tss.tsproviders.@NonNull IDataSourceListener listener) {
        Objects.requireNonNull(listener);
        getDelegate().addDataSourceListener(ToDataSourceListener.toDataSourceListener(listener));
    }

    @Override
    public void removeDataSourceListener(ec.tss.tsproviders.@NonNull IDataSourceListener listener) {
        Objects.requireNonNull(listener);
        getDelegate().removeDataSourceListener(ToDataSourceListener.toDataSourceListener(listener));
    }

    @Override
    public @NonNull List<ec.tss.tsproviders.DataSet> children(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException, IOException {
        Objects.requireNonNull(dataSource);
        return getDelegate()
                .children(TsConverter.toDataSource(dataSource))
                .stream()
                .map(TsConverter::fromDataSet)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull List<ec.tss.tsproviders.DataSet> children(ec.tss.tsproviders.@NonNull DataSet parent) throws IllegalArgumentException, IOException {
        Objects.requireNonNull(parent);
        return getDelegate()
                .children(TsConverter.toDataSet(parent))
                .stream()
                .map(TsConverter::fromDataSet)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull String getDisplayName(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        return getDelegate().getDisplayName(TsConverter.toDataSource(dataSource));
    }

    @Override
    public @NonNull String getDisplayName(ec.tss.tsproviders.@NonNull DataSet dataSet) throws IllegalArgumentException {
        Objects.requireNonNull(dataSet);
        return getDelegate().getDisplayName(TsConverter.toDataSet(dataSet));
    }

    @Override
    public @NonNull String getDisplayNodeName(ec.tss.tsproviders.@NonNull DataSet dataSet) throws IllegalArgumentException {
        Objects.requireNonNull(dataSet);
        return getDelegate().getDisplayNodeName(TsConverter.toDataSet(dataSet));
    }

    @Override
    public ec.tss.@NonNull TsMoniker toMoniker(ec.tss.tsproviders.@NonNull DataSource dataSource) throws IllegalArgumentException {
        Objects.requireNonNull(dataSource);
        return TsConverter.fromTsMoniker(getDelegate().toMoniker(TsConverter.toDataSource(dataSource)));
    }

    @Override
    public ec.tss.@NonNull TsMoniker toMoniker(ec.tss.tsproviders.@NonNull DataSet dataSet) throws IllegalArgumentException {
        Objects.requireNonNull(dataSet);
        return TsConverter.fromTsMoniker(getDelegate().toMoniker(TsConverter.toDataSet(dataSet)));
    }

    @Override
    public ec.tss.tsproviders.@Nullable DataSource toDataSource(ec.tss.@NonNull TsMoniker moniker) throws IllegalArgumentException {
        Objects.requireNonNull(moniker);
        Optional<DataSource> result = getDelegate().toDataSource(TsConverter.toTsMoniker(moniker));
        return result.isPresent() ? TsConverter.fromDataSource(result.get()) : null;
    }

    @Override
    public ec.tss.tsproviders.@Nullable DataSet toDataSet(ec.tss.@NonNull TsMoniker moniker) throws IllegalArgumentException {
        Objects.requireNonNull(moniker);
        Optional<DataSet> result = getDelegate().toDataSet(TsConverter.toTsMoniker(moniker));
        return result.isPresent() ? TsConverter.fromDataSet(result.get()) : null;
    }

    @lombok.AllArgsConstructor
    private static final class ChangeListener implements DataSourceListener {

        @lombok.NonNull
        private final DataSourceProvider delegate;

        @Override
        public void opened(demetra.tsprovider.@NonNull DataSource dataSource) {
            Objects.requireNonNull(dataSource);
        }

        @Override
        public void closed(demetra.tsprovider.@NonNull DataSource dataSource) {
            Objects.requireNonNull(dataSource);
        }

        @Override
        public void changed(demetra.tsprovider.@NonNull DataSource dataSource) {
            Objects.requireNonNull(dataSource);
            ec.tss.TsMoniker moniker = TsConverter.fromTsMoniker(delegate.toMoniker(dataSource));
            notifyTsFactory(moniker);
        }

        private void notifyTsFactory(ec.tss.TsMoniker moniker) {
            try {
                ec.tss.TsCollection col = ec.tss.TsFactory.instance.createTsCollection("", moniker, ec.tss.TsInformationType.None);
                ec.tss.TsFactory.instance.query(col, ec.tss.TsInformationType.All);
            } catch (RuntimeException ex) {
                log.log(Level.WARNING, "While reloading data", ex);
            }
        }

        @Override
        public void allClosed(@NonNull String providerName) {
            Objects.requireNonNull(providerName);
        }
    }
}
