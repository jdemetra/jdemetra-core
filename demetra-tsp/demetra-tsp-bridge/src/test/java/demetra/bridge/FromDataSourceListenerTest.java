package demetra.bridge;

import demetra.tsprovider.DataSource;
import demetra.tsprovider.DataSourceListener;
import ec.tss.tsproviders.IDataSourceListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class FromDataSourceListenerTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void test() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FromDataSourceListener(null));

        List<Object> callStack = new ArrayList<>();

        IDataSourceListener x = new FromDataSourceListener(new MockedListener(callStack));

        assertThatNullPointerException()
                .isThrownBy(() -> x.opened(null))
                .withMessageContaining("dataSource");

        assertThatNullPointerException()
                .isThrownBy(() -> x.closed(null))
                .withMessageContaining("dataSource");

        assertThatNullPointerException()
                .isThrownBy(() -> x.changed(null))
                .withMessageContaining("dataSource");

        assertThatNullPointerException()
                .isThrownBy(() -> x.allClosed(null))
                .withMessageContaining("providerName");

        x.opened(ec.tss.tsproviders.DataSource.of("opened", ""));
        assertThat(callStack).hasSize(1).last().isEqualTo(DataSource.of("opened", ""));

        x.closed(ec.tss.tsproviders.DataSource.of("closed", ""));
        assertThat(callStack).hasSize(2).last().isEqualTo(DataSource.of("closed", ""));

        x.changed(ec.tss.tsproviders.DataSource.of("changed", ""));
        assertThat(callStack).hasSize(3).last().isEqualTo(DataSource.of("changed", ""));

        x.allClosed("abc");
        assertThat(callStack).hasSize(4).last().isEqualTo("abc");
    }

    @lombok.AllArgsConstructor
    private static final class MockedListener implements DataSourceListener {

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
}
