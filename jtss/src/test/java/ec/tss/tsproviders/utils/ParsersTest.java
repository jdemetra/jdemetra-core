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
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ParsersTest {

    @Test
    public void testCharParser() {
        Parser<Character> p = Parsers.charParser();
        assertCompliance(p);
        assertThat(p.parse("hello")).isNull();
        assertThat(p.parse("h")).isEqualTo('h');
        assertThat(p.parse("\t")).isEqualTo('\t');
    }

    @Test
    public void testBoolParser() {
        Parser<Boolean> p = Parsers.boolParser();
        assertCompliance(p);
        assertThat(p.parse("true")).isEqualTo(true);
        assertThat(p.parse("false")).isEqualTo(false);
        assertThat(p.parse("TRUE")).isEqualTo(true);
        assertThat(p.parse("FALSE")).isEqualTo(false);
        assertThat(p.parse("1")).isEqualTo(true);
        assertThat(p.parse("0")).isEqualTo(false);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testCharsetParser() {
        Parser<Charset> p = Parsers.charsetParser();
        assertCompliance(p);
        assertThat(p.parse("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testDoubleArrayParser() {
        Parser<double[]> p = Parsers.doubleArrayParser();
        assertCompliance(p);
        assertThat(p.parse("[3.5,6.1]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[ 3.5  ,     6.1 ]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[3.5;6.1]")).isNull();
        assertThat(p.parse("3.5,6.1]")).isNull();
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testFileParser() {
        Parser<File> p = Parsers.fileParser();
        assertCompliance(p);
        assertThat(p.parse("test.xml")).isEqualTo(new File("test.xml"));
    }

    @Test
    public void testEnumParser() {
        Parser<TsFrequency> p = Parsers.enumParser(TsFrequency.class);
        assertCompliance(p);
        assertThat(p.parse("Monthly")).isEqualTo(TsFrequency.Monthly);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testIntParser() {
        Parser<Integer> p = Parsers.intParser();
        assertCompliance(p);
        assertThat(p.parse("123")).isEqualTo(123);
        assertThat(p.parse("123.3")).isNull();
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testStringParser() {
        Parser<String> p = Parsers.stringParser();
        assertCompliance(p);
        assertThat(p.parse("hello")).isEqualTo("hello");
    }

    @Test
    public void testOptional() {
        Parser<Integer> p = Parsers.intParser();
        assertThat(p.parseValue("hello").isPresent()).isFalse();
        assertThat(p.parseValue("123").isPresent()).isTrue();
        assertThat(p.parseValue("123").get()).isEqualTo(123);
    }

    @Test
    public void testStrictDatePattern() {
        Parser<Date> p = Parsers.onStrictDatePattern("yyyy-MM", Locale.ROOT);
        assertCompliance(p);
        Date jan2010 = new GregorianCalendar(2010, 0, 1).getTime();
        assertThat(p.parse("2010-01")).isEqualTo(jan2010);
        assertThat(p.parse("2010-02")).isNotEqualTo(jan2010);
        assertThat(p.parse("2010-01-01")).isNull();
    }

    @Test
    public void testOnDateFormat() {
        assertThatNullPointerException().isThrownBy(() -> Parsers.onDateFormat(null));

        assertCompliance(Parsers.onDateFormat(DateFormat.getDateInstance()));

        Parser<Date> p = Parsers.onDateFormat(new SimpleDateFormat("yyyy-MM", Locale.ROOT));
        assertThat(p.parse("2010-01")).isEqualTo("2010-01-01");
        assertThat(p.parse("2010-02")).isEqualTo("2010-02-01");
        assertThat(p.parse("2010-01-01")).isNull();
        assertThat(p.parse("2010-01x")).isNull();
        assertThat(p.parse("x2010-01")).isNull();
    }

    @Test
    public void testOnNumberFormat() {
        assertThatNullPointerException().isThrownBy(() -> Parsers.onNumberFormat(null));

        assertCompliance(Parsers.onNumberFormat(NumberFormat.getInstance()));

        assertThat(Parsers.onNumberFormat(NumberFormat.getInstance(Locale.ROOT)))
                .satisfies(p -> {
                    assertThat(p.parse("1234.5")).isEqualTo(1234.5);
                    assertThat(p.parse("1,234.5")).isEqualTo(1234.5);
                    assertThat(p.parse("1.234,5")).isNull();
                    assertThat(p.parse("1234.5x")).isNull();
                    assertThat(p.parse("x1234.5")).isNull();
                });

        assertThat(Parsers.onNumberFormat(NumberFormat.getInstance(Locale.FRANCE)))
                .satisfies(parser -> {
                    assertThat(parser.parse("1234,5")).isEqualTo(1234.5);
                    assertThat(parser.parse("1 234,5")).isEqualTo(1234.5);
                    assertThat(parser.parse("1\u00A0234,5")).isEqualTo(1234.5);
                    assertThat(parser.parse("1\u202F234,5")).isEqualTo(1234.5);
                    assertThat(parser.parse("1_234,5")).isNull();
                });
    }

    @SuppressWarnings("null")
    private static void assertCompliance(Parser<?> p) {
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.parseValue(null)).isInstanceOf(NullPointerException.class);
    }
}
