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
package ec.tss.tsproviders.utils;

import static ec.tss.tsproviders.utils.NumberFormats.*;
import java.text.NumberFormat;
import java.util.Locale;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class NumberFormatsTest {

    @Test
    public void testParseAll() {
        assertThatNullPointerException().isThrownBy(() -> parseAll(null, ""));
        assertThatNullPointerException().isThrownBy(() -> parseAll(NumberFormat.getInstance(), null));

        assertThat(NumberFormat.getInstance(Locale.ROOT))
                .satisfies(format -> {
                    assertThat(parseAll(format, "")).isNull();
                    assertThat(parseAll(format, "1234.5")).isEqualTo(1234.5);
                    assertThat(parseAll(format, "1,234.5")).isEqualTo(1234.5);
                    assertThat(parseAll(format, "x1234.5")).isNull();
                    assertThat(parseAll(format, "1234.5x")).isNull();
                });
    }

    @Test
    public void testSimplify() {
        assertThatNullPointerException().isThrownBy(() -> simplify(null, ""));
        assertThatNullPointerException().isThrownBy(() -> simplify(NumberFormat.getInstance(Locale.ROOT), null));

        assertThat(NumberFormat.getInstance(Locale.ROOT))
                .satisfies(without -> {
                    assertThat(simplify(without, "")).isEmpty();
                    assertThat(simplify(without, "1234.5")).isEqualTo("1234.5");
                    assertThat(simplify(without, "1,234.5")).isEqualTo("1,234.5");

                    assertThat(simplify(without, " ")).isEqualTo(" ");
                    assertThat(simplify(without, " 2")).isEqualTo(" 2");
                    assertThat(simplify(without, "1 ")).isEqualTo("1 ");
                    assertThat(simplify(without, "1 3")).isEqualTo("1 3");
                    assertThat(simplify(without, "1 234,5")).isEqualTo("1 234,5");
                    assertThat(simplify(without, "1\u00A0234,5")).isEqualTo("1\u00A0234,5");
                    assertThat(simplify(without, "1\u202F234,5")).isEqualTo("1\u202F234,5");
                });

        assertThat(NumberFormat.getInstance(Locale.FRANCE))
                .satisfies(with -> {
                    assertThat(simplify(with, "")).isEmpty();
                    assertThat(simplify(with, "1234.5")).isEqualTo("1234.5");
                    assertThat(simplify(with, "1,234.5")).isEqualTo("1,234.5");

                    assertThat(simplify(with, " 2")).isEqualTo(" 2");
                    assertThat(simplify(with, "1 ")).isEqualTo("1 ");
                    assertThat(simplify(with, "1 3")).isEqualTo("13");
                    assertThat(simplify(with, "1 234,5")).isEqualTo("1234,5");
                    assertThat(simplify(with, "1\u00A0234,5")).isEqualTo("1234,5");
                    assertThat(simplify(with, "1\u202F234,5")).isEqualTo("1234,5");
                });
    }
}
