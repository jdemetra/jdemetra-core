package demetra.bridge;

import _util.MockedDataSourceLoader;
import ec.tss.tsproviders.IDataSourceLoader;
import org.junit.jupiter.api.Test;

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
        IDataSourceLoader x = new FromDataSourceLoader(new MockedDataSourceLoader());

        assertThatNullPointerException()
                .isThrownBy(() -> x.open(null));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testClose() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedDataSourceLoader());

        assertThatNullPointerException()
                .isThrownBy(() -> x.close(null));
    }

    @Test
    public void testNewBean() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedDataSourceLoader());

        assertThat(x.newBean()).isEqualTo("value");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testEncodeBean() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedDataSourceLoader());

        assertThatNullPointerException()
                .isThrownBy(() -> x.encodeBean(null));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testDecodeBean() {
        IDataSourceLoader x = new FromDataSourceLoader(new MockedDataSourceLoader());

        assertThatNullPointerException()
                .isThrownBy(() -> x.decodeBean(null));
    }
}
