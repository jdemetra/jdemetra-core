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
package ec.tss.tsproviders.utils;

import ec.tss.tsproviders.utils.Formatters.Formatter;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FormattersTest {

    @Test
    @SuppressWarnings("null")
    public void testCharFormatter() {
        Formatter<Character> f = Formatters.charFormatter();
        assertThat(f.format('h')).isEqualTo("h");
        assertThat(f.format('\t')).isEqualTo("\t");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testBoolFormatter() {
        Formatter<Boolean> f = Formatters.boolFormatter();
        assertThat(f.format(Boolean.TRUE)).isEqualTo("true");
        assertThat(f.format(Boolean.FALSE)).isEqualTo("false");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testCharsetFormatter() {
        Formatter<Charset> f = Formatters.charsetFormatter();
        assertThat(f.format(StandardCharsets.UTF_8)).isEqualTo("UTF-8");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testOfInstance() {
        Formatter<String> f = Formatters.ofInstance("hello");
        assertThat(f.format("lkj")).isEqualTo("hello");
        assertThat(Formatters.ofInstance(null).format("lkj")).isNull();
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testDoubleArrayFormatter() {
        Formatter<double[]> f = Formatters.doubleArrayFormatter();
        assertThat(f.format(new double[]{0.4, -4.5})).isEqualTo("[0.4, -4.5]");
        assertThat(f.format(new double[]{})).isEqualTo("[]");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testEnumFormatter() {
        Formatter<TsFrequency> f = Formatters.enumFormatter();
        assertThat(f.format(TsFrequency.Monthly)).isEqualTo("Monthly");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFileFormatter() {
        Formatter<File> f = Formatters.fileFormatter();
        assertThat(f.format(new File("test.xml"))).isEqualTo("test.xml");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testIntFormatter() {
        Formatter<Integer> f = Formatters.intFormatter();
        assertThat(f.format(42)).isEqualTo("42");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testStringFormatter() {
        Formatter<String> f = Formatters.stringFormatter();
        assertThat(f.format("hello")).isEqualTo("hello");
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFormatterTryFormat() {
        assertThat(Formatters.ofInstance("123").tryFormat(new Object()).get()).isEqualTo("123");
        assertThat(Formatters.ofInstance(null).tryFormat(new Object()).isPresent()).isFalse();
    }

    @Test
    public void testFormatterFormatAsString() {
        assertThat(Formatters.ofInstance("123").formatAsString(new Object())).isEqualTo("123");
        assertThat(Formatters.ofInstance(null).formatAsString(new Object())).isNull();
    }

    @Test
    public void testFormatterTryFormatAsString() {
        assertThat(Formatters.ofInstance("123").tryFormatAsString(new Object()).get()).isEqualTo("123");
        assertThat(Formatters.ofInstance(null).tryFormatAsString(new Object()).isPresent()).isFalse();
    }
}
