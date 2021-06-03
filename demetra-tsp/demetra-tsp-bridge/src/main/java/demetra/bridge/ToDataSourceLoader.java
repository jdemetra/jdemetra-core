package demetra.bridge;

import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceLoader;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ToDataSourceLoader extends ToDataSourceProvider implements DataSourceLoader {

    public static @NonNull DataSourceLoader toDataSourceLoader(ec.tss.tsproviders.@NonNull IDataSourceLoader delegate) {
        return delegate instanceof FromDataSourceLoader
                ? ((FromDataSourceLoader) delegate).getDelegate()
                : new ToDataSourceLoader(delegate);
    }

    protected ToDataSourceLoader(ec.tss.tsproviders.IDataSourceLoader delegate) {
        super(delegate);
    }

    @lombok.NonNull
    @Override
    public ec.tss.tsproviders.IDataSourceLoader getDelegate() {
        return (ec.tss.tsproviders.IDataSourceLoader) super.getDelegate();
    }

    @Override
    public @NonNull Object newBean() {
        return getDelegate().newBean();
    }

    @Override
    public @NonNull DataSource encodeBean(@NonNull Object bean) throws IllegalArgumentException {
        return TsConverter.toDataSource(getDelegate().encodeBean(bean));
    }

    @Override
    public @NonNull Object decodeBean(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().decodeBean(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public boolean open(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().open(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public boolean close(@NonNull DataSource dataSource) throws IllegalArgumentException {
        return getDelegate().close(TsConverter.fromDataSource(dataSource));
    }

    @Override
    public void closeAll() {
        getDelegate().closeAll();
    }
}
