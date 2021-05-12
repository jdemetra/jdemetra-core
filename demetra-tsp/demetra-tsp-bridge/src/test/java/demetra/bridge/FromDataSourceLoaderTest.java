package demetra.bridge;

import demetra.timeseries.TsProvider;
import demetra.tsprovider.*;
import demetra.tsprovider.stream.HasTsStream;
import demetra.tsprovider.stream.TsStreamAsProvider;
import demetra.tsprovider.util.Params;
import ec.tss.tsproviders.IDataSourceLoader;
import nbbrd.io.function.IORunnable;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class FromDataSourceLoaderTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FromDataSourceLoader(null));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testOpen() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedProvider());

        assertThatNullPointerException()
                .isThrownBy(() -> x.open(null));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testClose() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedProvider());

        assertThatNullPointerException()
                .isThrownBy(() -> x.close(null));
    }

    @Test
    public void testNewBean() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedProvider());

        assertThat(x.newBean()).isEqualTo("value");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testEncodeBean() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedProvider());

        assertThatNullPointerException()
                .isThrownBy(() -> x.encodeBean(null));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testDecodeBean() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedProvider());

        assertThatNullPointerException()
                .isThrownBy(() -> x.decodeBean(null));
    }

    private static final class MockedProvider implements DataSourceLoader<String> {

        private static final String NAME = "mocked";

        @lombok.experimental.Delegate
        private final HasDataSourceMutableList dataSourceMutableList = HasDataSourceMutableList.of(NAME);

        @lombok.experimental.Delegate
        private final HasDataHierarchy dataHierarchy = HasDataHierarchy.noOp(NAME);

        @lombok.experimental.Delegate
        private final HasDataDisplayName dataDisplayName = HasDataDisplayName.usingUri(NAME);

        @lombok.experimental.Delegate
        private final HasDataMoniker dataMoniker = HasDataMoniker.usingUri(NAME);

        @lombok.experimental.Delegate
        private final HasDataSourceBean<String> dataSourceBean = HasDataSourceBean.of(NAME, Params.onString("value", "key"), "");

        @lombok.experimental.Delegate
        private final TsProvider provider = TsStreamAsProvider.of(NAME, HasTsStream.noOp(NAME), dataMoniker, IORunnable.noOp().asUnchecked());
    }
}
