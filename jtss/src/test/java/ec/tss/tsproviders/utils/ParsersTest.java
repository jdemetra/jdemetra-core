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

import ec.tss.tsproviders.utils.Parsers.Parser;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ParsersTest {

    @Test
    @SuppressWarnings("null")
    public void testCharParser() {
        Parser<Character> p = Parsers.charParser();
        assertThat(p.parse("hello")).isNull();
        assertThat(p.parse("h")).isEqualTo('h');
        assertThat(p.parse("\t")).isEqualTo('\t');
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testBoolParser() {
        Parser<Boolean> p = Parsers.boolParser();
        assertThat(p.parse("true")).isEqualTo(true);
        assertThat(p.parse("false")).isEqualTo(false);
        assertThat(p.parse("TRUE")).isEqualTo(true);
        assertThat(p.parse("FALSE")).isEqualTo(false);
        assertThat(p.parse("1")).isEqualTo(true);
        assertThat(p.parse("0")).isEqualTo(false);
        assertThat(p.parse("hello")).isNull();
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testCharsetParser() {
        Parser<Charset> p = Parsers.charsetParser();
        assertThat(p.parse("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(p.parse("hello")).isNull();
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testDoubleArrayParser() {
        Parser<double[]> p = Parsers.doubleArrayParser();
        assertThat(p.parse("[3.5,6.1]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[ 3.5  ,     6.1 ]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[3.5;6.1]")).isNull();
        assertThat(p.parse("3.5,6.1]")).isNull();
        assertThat(p.parse("hello")).isNull();
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFileParser() {
        Parser<File> p = Parsers.fileParser();
        assertThat(p.parse("test.xml")).isEqualTo(new File("test.xml"));
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testEnumParser() {
        Parser<TsFrequency> p = Parsers.enumParser(TsFrequency.class);
        assertThat(p.parse("Monthly")).isEqualTo(TsFrequency.Monthly);
        assertThat(p.parse("hello")).isNull();
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testIntParser() {
        Parser<Integer> p = Parsers.intParser();
        assertThat(p.parse("123")).isEqualTo(123);
        assertThat(p.parse("123.3")).isNull();
        assertThat(p.parse("hello")).isNull();
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testStringParser() {
        Parser<String> p = Parsers.stringParser();
        assertThat(p.parse("hello")).isEqualTo("hello");
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testOptional() {
        Parser<Integer> p = Parsers.intParser();
        assertThat(p.tryParse("hello").isPresent()).isFalse();
        assertThat(p.tryParse("123").isPresent()).isTrue();
        assertThat(p.tryParse("123").get()).isEqualTo(123);
    }

    @Test
    public void testStrictDatePattern() {
        Parser<Date> p = Parsers.onStrictDatePattern("yyyy-MM", Locale.ROOT);
        Date jan2010 = new GregorianCalendar(2010, 0, 1).getTime();
        assertThat(p.parse("2010-01")).isEqualTo(jan2010);
        assertThat(p.parse("2010-02")).isNotEqualTo(jan2010);
        assertThat(p.parse("2010-01-01")).isNull();
    }
}
