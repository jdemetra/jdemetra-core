package demetra.tsprovider.util;

import org.junit.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyHandlerTest {

    @Test
    public void testPrefix() {
        Properties properties = new Properties();

        PropertyHandler<String> original = PropertyHandler.onString("abc", "xyz");
        {
            properties.clear();
            assertThat(original.get(properties::getProperty))
                    .isEqualTo("xyz");

            properties.put("abc", "world");
            assertThat(original.get(properties::getProperty))
                    .isEqualTo("world");

            properties.clear();
            original.set(properties::setProperty, "world");
            assertThat(original.get(properties::getProperty))
                    .isEqualTo("world");
        }

        PropertyHandler<String> withPrefix = original.withPrefix("hello.");
        {
            properties.clear();
            assertThat(withPrefix.get(properties::getProperty))
                    .isEqualTo("xyz");

            properties.put("hello.abc", "world");
            assertThat(withPrefix.get(properties::getProperty))
                    .isEqualTo("world");

            properties.clear();
            withPrefix.set(properties::setProperty, "world");
            assertThat(withPrefix.get(properties::getProperty))
                    .isEqualTo("world");
        }
    }
}
