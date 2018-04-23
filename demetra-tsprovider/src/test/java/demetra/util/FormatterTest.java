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
import static demetra.util.Formatter.*;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.DateUtil;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class FormatterTest {

    @Test
    @SuppressWarnings("null")
    public void testOnDateTimeFormatter() {
        assertThatThrownBy(() -> onDateTimeFormatter(null)).isInstanceOf(NullPointerException.class);
        assertCompliance(onDateTimeFormatter(DateTimeFormatter.ISO_DATE));

        LocalDate date = LocalDate.of(2003, 4, 26);
        LocalTime time = LocalTime.of(3, 1, 2);
        LocalDateTime dateTime = date.atTime(time);

        Formatter<TemporalAccessor> f1 = onDateTimeFormatter(DateTimeFormatter.ISO_DATE);
        assertThat(f1.format(date)).isEqualTo("2003-04-26");
        assertThat(f1.format(time)).isNull();
        assertThat(f1.format(dateTime)).isEqualTo("2003-04-26");

        Formatter<TemporalAccessor> f2 = onDateTimeFormatter(DateTimeFormatter.ISO_DATE_TIME);
        assertThat(f2.format(date)).isNull();
        assertThat(f2.format(time)).isNull();
        assertThat(f2.format(dateTime)).isEqualTo("2003-04-26T03:01:02");

        Formatter<TemporalAccessor> f3 = onDateTimeFormatter(DateTimeFormatter.ISO_TIME);
        assertThat(f3.format(date)).isNull();
        assertThat(f3.format(time)).isEqualTo("03:01:02");
        assertThat(f3.format(dateTime)).isEqualTo("03:01:02");
    }

    @Test
    @SuppressWarnings("null")
    public void testOnDateFormat() {
        assertThatThrownBy(() -> onDateFormat(null)).isInstanceOf(NullPointerException.class);
        assertCompliance(onDateFormat(DateUtil.newIsoDateTimeFormat()));

        Formatter<Date> f = onDateFormat(DateUtil.newIsoDateTimeFormat());
        assertThat(f.format(DateUtil.parseDatetime("2003-04-26T03:01:02"))).isEqualTo("2003-04-26T03:01:02");
    }

    @Test
    @SuppressWarnings("null")
    public void testOnNumberFormat() {
        assertThatThrownBy(() -> onNumberFormat(null)).isInstanceOf(NullPointerException.class);
        assertCompliance(onNumberFormat(NumberFormat.getInstance(Locale.ROOT)));

        Formatter<Number> f = onNumberFormat(NumberFormat.getInstance(Locale.ROOT));
        assertThat(f.format(3.14)).isEqualTo("3.14");
    }

    @Test
    public void testCharFormatter() {
        Formatter<Character> f = onCharacter();
        assertCompliance(f);
        assertThat(f.format('h')).isEqualTo("h");
        assertThat(f.format('\t')).isEqualTo("\t");
    }

    @Test
    @SuppressWarnings("null")
    public void testBoolFormatter() {
        Formatter<Boolean> f = onBoolean();
        assertCompliance(f);
        assertThat(f.format(Boolean.TRUE)).isEqualTo("true");
        assertThat(f.format(Boolean.FALSE)).isEqualTo("false");
    }

    @Test
    public void testCharsetFormatter() {
        Formatter<Charset> f = onCharset();
        assertCompliance(f);
        assertThat(f.format(StandardCharsets.UTF_8)).isEqualTo("UTF-8");
    }

    @Test
    public void testOfInstance() {
        Formatter<String> f = onConstant("hello");
        assertCompliance(f);
        assertThat(f.format("lkj")).isEqualTo("hello");
        assertThat(Formatter.onConstant(null).format("lkj")).isNull();
    }

    @Test
    public void testDoubleArrayFormatter() {
        Formatter<double[]> f = onDoubleArray();
        assertCompliance(f);
        assertThat(f.format(new double[]{0.4, -4.5})).isEqualTo("[0.4, -4.5]");
        assertThat(f.format(new double[]{})).isEqualTo("[]");
    }

    @Test
    public void testEnumFormatter() {
        Formatter<AggregationType> f = onEnum();
        assertCompliance(f);
        assertThat(f.format(AggregationType.Average)).isEqualTo("Average");
    }

    @Test
    public void testFileFormatter() {
        Formatter<File> f = onFile();
        assertCompliance(f);
        assertThat(f.format(new File("test.xml"))).isEqualTo("test.xml");
    }

    @Test
    public void testIntFormatter() {
        Formatter<Integer> f = onInteger();
        assertCompliance(f);
        assertThat(f.format(42)).isEqualTo("42");
    }

    @Test
    public void testStringFormatter() {
        Formatter<String> f = onString();
        assertCompliance(f);
        assertThat(f.format("hello")).isEqualTo("hello");
    }

    @Test
    public void testFormatterFormatValue() {
        assertThat(onConstant("123").formatValue(new Object()).get()).isEqualTo("123");
        assertThat(onConstant(null).formatValue(new Object()).isPresent()).isFalse();
    }

    @Test
    public void testFormatterFormatAsString() {
        assertThat(onConstant("123").formatAsString(new Object())).isEqualTo("123");
        assertThat(onConstant(null).formatAsString(new Object())).isNull();
    }

    @Test
    public void testFormatterFormatValueAsString() {
        assertThat(onConstant("123").formatValueAsString(new Object()).get()).isEqualTo("123");
        assertThat(onConstant(null).formatValueAsString(new Object()).isPresent()).isFalse();
    }

    @SuppressWarnings("null")
    private static void assertCompliance(Formatter<?> f) {
        assertThatThrownBy(() -> f.format(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.formatAsString(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.formatValue(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> f.formatValueAsString(null)).isInstanceOf(NullPointerException.class);
    }
}
