/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.util;

import static demetra.util.Validations.*;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ValidationsTest {

    @Test
    @SuppressWarnings("null")
    public void testNotBlank1() {
        assertThat(notBlank("abc", "msg"))
                .isEqualTo("abc");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> notBlank("", "msg"))
                .withMessage("msg");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> notBlank("   ", "msg"))
                .withMessage("msg");

        assertThatNullPointerException()
                .isThrownBy(() -> notBlank(null, "msg"));
    }

    @Test
    @SuppressWarnings("null")
    public void testNotBlank2() {
        Function<String, String> customMsg = o -> String.format("Expected not blank, found '%s'", o);

        assertThat(notBlank("abc", customMsg))
                .isEqualTo("abc");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> notBlank("", customMsg))
                .withMessage("Expected not blank, found ''");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> notBlank("   ", customMsg))
                .withMessage("Expected not blank, found '   '");

        assertThatNullPointerException()
                .isThrownBy(() -> notBlank(null, customMsg));
    }

    @Test
    public void testMin1() {
        assertThat(min(10, 10, "msg"))
                .isEqualTo(10);

        assertThat(min(20, 10, "msg"))
                .isEqualTo(20);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> min(9, 10, "msg"))
                .withMessage("msg");
    }

    @Test
    public void testMin2() {
        IntFunction<String> customMsg = o -> String.format("Min value is 10; actual is %d", o);

        assertThat(min(10, 10, customMsg))
                .isEqualTo(10);

        assertThat(min(20, 10, customMsg))
                .isEqualTo(20);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> min(9, 10, customMsg))
                .withMessage("Min value is 10; actual is 9");
    }

    @Test
    @SuppressWarnings("null")
    public void testAtLeast1() {
        assertThat(atLeast(Arrays.asList("a", "b", "c"), 2, "msg"))
                .containsExactly("a", "b", "c");

        assertThat(atLeast(Arrays.asList("a", "b"), 2, "msg"))
                .containsExactly("a", "b");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> atLeast(Arrays.asList("a"), 2, "msg"))
                .withMessage("msg");

        assertThatNullPointerException()
                .isThrownBy(() -> atLeast(null, 2, "msg"));
    }

    @Test
    @SuppressWarnings("null")
    public void testAtLeast2() {
        IntFunction<String> customMsg = o -> String.format("Min size is 2; actual is %d", o);

        assertThat(atLeast(Arrays.asList("a", "b", "c"), 2, customMsg))
                .containsExactly("a", "b", "c");

        assertThat(atLeast(Arrays.asList("a", "b"), 2, customMsg))
                .containsExactly("a", "b");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> atLeast(Arrays.asList("a"), 2, customMsg))
                .withMessage("Min size is 2; actual is 1");

        assertThatNullPointerException()
                .isThrownBy(() -> atLeast(null, 2, customMsg));
    }

    @Test
    @SuppressWarnings("null")
    public void testOn1() {
        assertThat(on("abcd", str -> str.length() > 3, "msg"))
                .isEqualTo("abcd");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> on("abc", str -> str.length() > 3, "msg"))
                .withMessage("msg");

        assertThatNullPointerException()
                .isThrownBy(() -> on("abc", null, "msg"));
    }

    @Test
    @SuppressWarnings("null")
    public void testOn2() {
        Function<String, String> customMsg = o -> String.format("Min length is 4; actual is %d", o.length());

        assertThat(on("abcd", str -> str.length() > 3, customMsg))
                .isEqualTo("abcd");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> on("abc", str -> str.length() > 3, customMsg))
                .withMessage("Min length is 4; actual is 3");

        assertThatNullPointerException()
                .isThrownBy(() -> on("abc", null, customMsg));
    }
}
