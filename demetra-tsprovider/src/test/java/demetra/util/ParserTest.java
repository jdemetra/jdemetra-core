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

import demetra.data.AggregationType;
import internal.util.InternalParser;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Philippe Charles
 */
public class ParserTest {

    @Test
    public void testOnCharacter() {
        Parser<Character> p = Parser.onCharacter();
        assertCompliance(p, "x");
        assertThat(p.parse("hello")).isNull();
        assertThat(p.parse("h")).isEqualTo('h');
        assertThat(p.parse("\t")).isEqualTo('\t');
    }

    @Test
    public void testOnBoolean() {
        Parser<Boolean> p = Parser.onBoolean();
        assertCompliance(p, "true");
        assertThat(p.parse("true")).isEqualTo(true);
        assertThat(p.parse("false")).isEqualTo(false);
        assertThat(p.parse("TRUE")).isEqualTo(true);
        assertThat(p.parse("FALSE")).isEqualTo(false);
        assertThat(p.parse("1")).isEqualTo(true);
        assertThat(p.parse("0")).isEqualTo(false);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnCharset() {
        Parser<Charset> p = Parser.onCharset();
        assertCompliance(p, "UTF-8");
        assertThat(p.parse("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnDoubleArray() {
        Parser<double[]> p = Parser.onDoubleArray();
        assertCompliance(p, "[3.5,6.1]");
        assertThat(p.parse("[3.5,6.1]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[ 3.5  ,     6.1 ]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[3.5;6.1]")).isNull();
        assertThat(p.parse("3.5,6.1]")).isNull();
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnFile() {
        Parser<File> p = Parser.onFile();
        assertCompliance(p, "test.xml");
        assertThat(p.parse("test.xml")).isEqualTo(new File("test.xml"));
    }

    @Test
    public void testOnEnum() {
        Parser<AggregationType> p = Parser.onEnum(AggregationType.class);
        assertCompliance(p, "Average");
        assertThat(p.parse("Average")).isEqualTo(AggregationType.Average);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnInteger() {
        Parser<Integer> p = Parser.onInteger();
        assertCompliance(p, "123");
        assertThat(p.parse("123")).isEqualTo(123);
        assertThat(p.parse("123.3")).isNull();
        assertThat(p.parse("hello")).isNull();
        assertThat(p.parseValue("hello").isPresent()).isFalse();
        assertThat(p.parseValue("123").isPresent()).isTrue();
        assertThat(p.parseValue("123").get()).isEqualTo(123);
    }

    @Test
    public void testOnString() {
        Parser<String> p = Parser.onString();
        assertCompliance(p, "hello");
        assertThat(p.parse("hello")).isEqualTo("hello");
    }

    @Test
    public void testOnStrictDatePattern() {
        Parser<Date> p = Parser.onStrictDatePattern("yyyy-MM", Locale.ROOT);
        assertCompliance(p, "2010-01");
        Date jan2010 = new GregorianCalendar(2010, 0, 1).getTime();
        assertThat(p.parse("2010-01")).isEqualTo(jan2010);
        assertThat(p.parse("2010-02")).isNotEqualTo(jan2010);
        assertThat(p.parse("2010-01-01")).isNull();
    }

    @Test
    public void testToLocale() {
        Locale locale;
        locale = InternalParser.parseLocale("fr");
        assertEquals("fr", locale.getLanguage());
        locale = InternalParser.parseLocale("fr_BE");
        assertEquals("fr", locale.getLanguage());
        locale = InternalParser.parseLocale("fr_BE_WIN");
        assertEquals("fr", locale.getLanguage());
        assertEquals("BE", locale.getCountry());
        assertEquals("WIN", locale.getVariant());
        assertThat(InternalParser.parseLocale("helloworld")).isNull();
        assertThat(InternalParser.parseLocale("fr_")).isNull();
        assertThat(InternalParser.parseLocale("fr_BE_")).isNull();
    }

    @SuppressWarnings("null")
    private static <T> void assertCompliance(Parser<T> p, CharSequence input) {
        assertThatThrownBy(() -> p.parse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.parseValue(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.andThen(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> p.orElse(null)).isInstanceOf(NullPointerException.class);
        assertThat(p.parse(input)).isEqualTo(p.parse(input));
        assertThat(p.parseValue(input)).contains(p.parse(input));
    }
}
