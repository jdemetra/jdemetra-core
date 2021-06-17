package demetra.bridge;

import _util.MockedDataSourceListener;
import demetra.tsprovider.DataSource;
import ec.tss.tsproviders.IDataSourceListener;
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
                .isThrownBy(() -> FromDataSourceListener.fromDataSourceListener(null));

        List<Object> callStack = new ArrayList<>();

        IDataSourceListener x = FromDataSourceListener.fromDataSourceListener(new MockedDataSourceListener(callStack));

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
}
