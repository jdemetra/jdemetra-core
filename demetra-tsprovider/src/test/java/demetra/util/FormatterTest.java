/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import demetra.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class FormatterTest {

    @Test
    public void testCharFormatter() {
        Formatter<Character> f = Formatter.onCharacter();
        assertCompliance(f);
        assertThat(f.format('h')).isEqualTo("h");
        assertThat(f.format('\t')).isEqualTo("\t");
    }

    @Test
    @SuppressWarnings("null")
    public void testBoolFormatter() {
        Formatter<Boolean> f = Formatter.onBoolean();
        assertCompliance(f);
        assertThat(f.format(Boolean.TRUE)).isEqualTo("true");
        assertThat(f.format(Boolean.FALSE)).isEqualTo("false");
    }

    @Test
    public void testCharsetFormatter() {
        Formatter<Charset> f = Formatter.onCharset();
        assertCompliance(f);
        assertThat(f.format(StandardCharsets.UTF_8)).isEqualTo("UTF-8");
    }

    @Test
    public void testOfInstance() {
        Formatter<String> f = Formatter.onConstant("hello");
        assertCompliance(f);
        assertThat(f.format("lkj")).isEqualTo("hello");
        assertThat(Formatter.onConstant(null).format("lkj")).isNull();
    }

    @Test
    public void testDoubleArrayFormatter() {
        Formatter<double[]> f = Formatter.onDoubleArray();
        assertCompliance(f);
        assertThat(f.format(new double[]{0.4, -4.5})).isEqualTo("[0.4, -4.5]");
        assertThat(f.format(new double[]{})).isEqualTo("[]");
    }

    @Test
    public void testEnumFormatter() {
        Formatter<TsFrequency> f = Formatter.onEnum();
        assertCompliance(f);
        assertThat(f.format(TsFrequency.Monthly)).isEqualTo("Monthly");
    }

    @Test
    public void testFileFormatter() {
        Formatter<File> f = Formatter.onFile();
        assertCompliance(f);
        assertThat(f.format(new File("test.xml"))).isEqualTo("test.xml");
    }

    @Test
    public void testIntFormatter() {
        Formatter<Integer> f = Formatter.onInteger();
        assertCompliance(f);
        assertThat(f.format(42)).isEqualTo("42");
    }

    @Test
    public void testStringFormatter() {
        Formatter<String> f = Formatter.onString();
        assertCompliance(f);
        assertThat(f.format("hello")).isEqualTo("hello");
    }

    @Test
    public void testFormatterFormatValue() {
        assertThat(Formatter.onConstant("123").formatValue(new Object()).get()).isEqualTo("123");
        assertThat(Formatter.onConstant(null).formatValue(new Object()).isPresent()).isFalse();
    }

    @Test
    public void testFormatterFormatAsString() {
        assertThat(Formatter.onConstant("123").formatAsString(new Object())).isEqualTo("123");
        assertThat(Formatter.onConstant(null).formatAsString(new Object())).isNull();
    }

    @Test
    public void testFormatterFormatValueAsString() {
        assertThat(Formatter.onConstant("123").formatValueAsString(new Object()).get()).isEqualTo("123");
        assertThat(Formatter.onConstant(null).formatValueAsString(new Object()).isPresent()).isFalse();
    }

    @SuppressWarnings("null")
    private static void assertCompliance(Formatter<?> f) {
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.formatAsString(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.formatValue(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.formatValueAsString(null)).isInstanceOf(NullPointerException.class);
    }
}
