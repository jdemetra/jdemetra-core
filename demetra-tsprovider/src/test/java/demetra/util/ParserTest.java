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
import static demetra.util.Parser.*;
import internal.util.InternalParser;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.DateUtil;

/**
 *
 * @author Philippe Charles
 */
public class ParserTest {

    @Test
    @SuppressWarnings("null")
    public void testOnDateTimeFormatter() {
        assertThatThrownBy(() -> onDateTimeFormatter(null, LocalDate::from)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> onDateTimeFormatter(DateTimeFormatter.ISO_DATE, null)).isInstanceOf(NullPointerException.class);
        assertCompliance(onDateTimeFormatter(DateTimeFormatter.ISO_DATE, LocalDate::from), "2003-04-26");

        LocalDate date = LocalDate.of(2003, 4, 26);
        LocalTime time = LocalTime.of(3, 1, 2);
        LocalDateTime dateTime = date.atTime(time);

        Parser<LocalDate> p1 = onDateTimeFormatter(DateTimeFormatter.ISO_DATE, LocalDate::from);
        assertThat(p1.parse("2003-04-26")).isEqualTo(date);
        assertThat(p1.parse("2003-04-26T03:01:02")).isNull();
        assertThat(p1.parse("03:01:02")).isNull();

        Parser<LocalDateTime> p2 = onDateTimeFormatter(DateTimeFormatter.ISO_DATE_TIME, LocalDateTime::from);
        assertThat(p2.parse("2003-04-26")).isNull();
        assertThat(p2.parse("2003-04-26T03:01:02")).isEqualTo(dateTime);
        assertThat(p2.parse("03:01:02")).isNull();

        Parser<LocalTime> p3 = onDateTimeFormatter(DateTimeFormatter.ISO_TIME, LocalTime::from);
        assertThat(p3.parse("2003-04-26")).isNull();
        assertThat(p1.parse("2003-04-26T03:01:02")).isNull();
        assertThat(p3.parse("03:01:02")).isEqualTo(time);
    }

    @Test
    public void testOnCharacter() {
        Parser<Character> p = onCharacter();
        assertCompliance(p, "x");
        assertThat(p.parse("hello")).isNull();
        assertThat(p.parse("h")).isEqualTo('h');
        assertThat(p.parse("\t")).isEqualTo('\t');
    }

    @Test
    public void testOnBoolean() {
        Parser<Boolean> p = onBoolean();
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
        Parser<Charset> p = onCharset();
        assertCompliance(p, "UTF-8");
        assertThat(p.parse("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnDoubleArray() {
        Parser<double[]> p = onDoubleArray();
        assertCompliance(p, "[3.5,6.1]");
        assertThat(p.parse("[3.5,6.1]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[ 3.5  ,     6.1 ]")).containsExactly(3.5, 6.1);
        assertThat(p.parse("[3.5;6.1]")).isNull();
        assertThat(p.parse("3.5,6.1]")).isNull();
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnFile() {
        Parser<File> p = onFile();
        assertCompliance(p, "test.xml");
        assertThat(p.parse("test.xml")).isEqualTo(new File("test.xml"));
    }

    @Test
    public void testOnEnum() {
        Parser<AggregationType> p = onEnum(AggregationType.class);
        assertCompliance(p, "Average");
        assertThat(p.parse("Average")).isEqualTo(AggregationType.Average);
        assertThat(p.parse("hello")).isNull();
    }

    @Test
    public void testOnInteger() {
        Parser<Integer> p = onInteger();
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
        Parser<String> p = onString();
        assertCompliance(p, "hello");
        assertThat(p.parse("hello")).isEqualTo("hello");
    }

    @Test
    public void testOnStrictDatePattern() {
        Parser<Date> p = onStrictDatePattern("yyyy-MM", Locale.ROOT);
        assertCompliance(p, "2010-01");
        Date jan2010 = DateUtil.parse("2010-01-01");
        assertThat(p.parse("2010-01")).isEqualTo(jan2010);
        assertThat(p.parse("2010-02")).isNotEqualTo(jan2010);
        assertThat(p.parse("2010-01-01")).isNull();
    }

    @Test
    public void testToLocale() {
        Locale locale;
        locale = InternalParser.parseLocale("fr");
        assertThat(locale.getLanguage()).isEqualTo("fr");
        locale = InternalParser.parseLocale("fr_BE");
        assertThat(locale.getLanguage()).isEqualTo("fr");
        locale = InternalParser.parseLocale("fr_BE_WIN");
        assertThat(locale.getLanguage()).isEqualTo("fr");
        assertThat(locale.getCountry()).isEqualTo("BE");
        assertThat(locale.getVariant()).isEqualTo("WIN");
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
