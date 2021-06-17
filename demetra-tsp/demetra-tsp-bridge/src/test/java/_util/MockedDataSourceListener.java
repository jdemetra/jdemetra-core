package _util;

import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceListener;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

@lombok.AllArgsConstructor
public final class MockedDataSourceListener implements DataSourceListener {

    @lombok.NonNull
    private final List<Object> stack;

    @Override
    public void opened(@NonNull DataSource dataSource) {
        stack.add(dataSource);
    }

    @Override
    public void closed(@NonNull DataSource dataSource) {
        stack.add(dataSource);
    }

    @Override
    public void changed(@NonNull DataSource dataSource) {
        stack.add(dataSource);
    }

    @Override
    public void allClosed(@NonNull String providerName) {
        stack.add(providerName);
    }
}
