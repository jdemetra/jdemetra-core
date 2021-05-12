package demetra.bridge;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class ConverterExceptionTest {

    @SuppressWarnings({"ThrowableNotThrown", "ConstantConditions"})
    @Test
    public void testFactories() {
        assertThatNullPointerException()
                .isThrownBy(() -> ConverterException.of(null, String.class, ""))
                .withMessageContaining("from");

        assertThatNullPointerException()
                .isThrownBy(() -> ConverterException.of(String.class, null, ""))
                .withMessageContaining("to");

        assertThatNullPointerException()
                .isThrownBy(() -> ConverterException.of(String.class, String.class, null))
                .withMessageContaining("value");
    }
}
