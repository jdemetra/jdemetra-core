package demetra.bridge;

import demetra.timeseries.TsMoniker;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceListener;
import demetra.tsprovider.DataSourceProvider;
import ec.tss.tsproviders.IDataSourceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ToDataSourceProvider extends ToTsProvider implements DataSourceProvider {

    public static @NonNull DataSourceProvider toDataSourceProvider(ec.tss.tsproviders.@NonNull IDataSourceProvider delegate) {
        return delegate instanceof FromDataSourceProvider
                ? ((FromDataSourceProvider) delegate).getDelegate()
                : new ToDataSourceProvider(delegate);
    }

    protected ToDataSourceProvider(ec.tss.tsproviders.IDataSourceProvider delegate) {
        super(delegate);
    }

    @lombok.NonNull
    @Override
    public ec.tss.tsproviders.IDataSourceProvider getDelegate() {
        return (IDataSourceProvider) super.getDelegate();
    }

    @Override
    public void reload(DataSource dataSource) {
        getDelegate().reload(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public @NonNull List<DataSource> getDataSources() {
        return getDelegate().getDataSources().stream().map(TsConverter::toDataSource).collect(Collectors.toList());
    }

    @Override
    public void addDataSourceListener(@NonNull DataSourceListener listener) {
        getDelegate().addDataSourceListener(FromDataSourceListener.fromDataSourceListener(listener));
    }

    @Override
    public void removeDataSourceListener(@NonNull DataSourceListener listener) {
        getDelegate().removeDataSourceListener(FromDataSourceListener.fromDataSourceListener(listener));
    }

    @Override
    public @NonNull String getDisplayName() {
        return getDelegate().getDisplayName();
    }

    @Override
    public @NonNull String getDisplayName(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().getDisplayName(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public @NonNull String getDisplayName(@NonNull DataSet dataSet) throws IllegalArgumentException {
        return getDelegate().getDisplayName(TsConverter.fromDataSet(dataSet));
    }

    @Override
    public @NonNull String getDisplayNodeName(@NonNull DataSet dataSet) throws IllegalArgumentException {
        return getDelegate().getDisplayNodeName(TsConverter.fromDataSet(dataSet));
    }

    @Override
    public @NonNull String getDisplayName(@NonNull IOException exception) throws IllegalArgumentException {
        return getDelegate().getDisplayName(exception);
    }

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSource dataSource) throws IllegalArgumentException, IOException {
        return getDelegate().children(TsConverter.fromDataSource(dataSource)).stream().map(TsConverter::toDataSet).collect(Collectors.toList());
    }

    @Override
    public @NonNull List<DataSet> children(@NonNull DataSet parent) throws IllegalArgumentException, IOException {
        return getDelegate().children(TsConverter.fromDataSet(parent)).stream().map(TsConverter::toDataSet).collect(Collectors.toList());
    }

    @Override
    public @NonNull TsMoniker toMoniker(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return TsConverter.toTsMoniker(getDelegate().toMoniker(TsConverter.fromDataSource(dataSource)));
    }

    @Override
    public @NonNull TsMoniker toMoniker(@NonNull DataSet dataSet) throws IllegalArgumentException {
        return TsConverter.toTsMoniker(getDelegate().toMoniker(TsConverter.fromDataSet(dataSet)));
    }

    @Override
    public @NonNull Optional<DataSource> toDataSource(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        return Optional.ofNullable(getDelegate().toDataSource(TsConverter.fromTsMoniker(moniker))).map(TsConverter::toDataSource);
    }

    @Override
    public @NonNull Optional<DataSet> toDataSet(@NonNull TsMoniker moniker) throws IllegalArgumentException {
        return Optional.ofNullable(getDelegate().toDataSet(TsConverter.fromTsMoniker(moniker))).map(TsConverter::toDataSet);
    }
}
