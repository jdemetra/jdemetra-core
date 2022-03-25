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
package demetra.tsprovider.util;

import org.assertj.core.util.DateUtil;
import org.junit.Test;

import java.text.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static demetra.tsprovider.util.ObsFormat.*;
import static java.util.Locale.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * @author Philippe Charles
 */
public class ObsFormatTest {

    @Test
    public void testDEFAULT() {
        assertThat(DEFAULT.newDateFormat())
                .satisfies(format -> {
                    Date date = DateUtil.parse("2000-01-01");
                    assertThat(format.format(date)).isEqualTo("2000-01-01T00:00:00");
                    assertThat(format.parse("2000-01-01T00:00:00")).isEqualTo(date);

                    Date datetime = DateUtil.parseDatetime("2003-04-26T03:01:02");
                    assertThat(format.format(datetime)).isEqualTo("2003-04-26T03:01:02");
                    assertThat(format.parse("2003-04-26T03:01:02")).isEqualTo(datetime);
                });

        assertThat(DEFAULT.newDateTimeFormatter())
                .satisfies(format -> {
                    LocalDate date = LocalDate.parse("2000-01-01");
                    assertThat(format.format(date)).isEqualTo("2000-01-01");
                    assertThat(format.parseBest("2000-01-01T00:00:00", temporalQueries)).isEqualTo(date.atStartOfDay());

                    LocalDateTime datetime = LocalDateTime.parse("2003-04-26T03:01:02");
                    assertThat(format.format(datetime)).isEqualTo("2003-04-26T03:01:02");
                    assertThat(format.parseBest("2003-04-26T03:01:02", temporalQueries)).isEqualTo(datetime);
                });

        assertThat(DEFAULT.newNumberFormat())
                .satisfies(format -> {
                    double number = 1234.5d;
                    assertThat(format.format(number)).isEqualTo("1234.5");
                    assertThat(format.parse("1234.5")).isEqualTo(number);
                });
    }

    @Test
    public void testNewNumberFormat() {
        double value = 1234.5d;
        Function<NumberFormat, String> withoutGrouping = format -> "1" + "234" + decimalSeparator(format) + "5";
        Function<NumberFormat, String> withGrouping = format -> "1" + groupingSeparator(format) + "234" + decimalSeparator(format) + "5";

        for (Locale locale : locales) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> builder().locale(locale).numberPattern(",.,.,.").build().newNumberFormat());

            assertThat(builder().locale(locale).build())
                    .extracting(ObsFormat::newNumberFormat)
                    .describedAs("NumberFormat with grouping pattern")
                    .satisfies(format -> {
                        String text = withoutGrouping.apply(format);
                        String textWithGroup = withGrouping.apply(format);
                        assertThat(format.parse(text)).isEqualTo(value);
                        assertThat(format.parse(textWithGroup)).isEqualTo(value);
                        assertThat(format.isGroupingUsed()).isTrue();
                        assertThat(format.format(value)).isEqualTo(textWithGroup);
                    });

            assertThat(builder().locale(locale).numberPattern("#.#").build())
                    .extracting(ObsFormat::newNumberFormat)
                    .describedAs("NumberFormat without grouping pattern")
                    .satisfies(format -> {
                        String text = withoutGrouping.apply(format);
                        String textWithGroup = withGrouping.apply(format);
                        assertThat(format.parse(text)).isEqualTo(value);
                        assertThat(format.parse(textWithGroup)).isNotEqualTo(value);
                        assertThat(format.isGroupingUsed()).isFalse();
                        assertThat(format.format(value)).isEqualTo(text);
                    });

            assertThat(builder().locale(locale).ignoreNumberGrouping(true).build())
                    .extracting(ObsFormat::newNumberFormat)
                    .describedAs("NumberFormat with grouping pattern with ignore grouping")
                    .satisfies(format -> {
                        String text = withoutGrouping.apply(format);
                        String textWithGroup = withGrouping.apply(format);
                        assertThat(format.parse(text)).isEqualTo(value);
                        assertThat(format.parse(textWithGroup)).isNotEqualTo(value);
                        assertThat(format.isGroupingUsed()).isFalse();
                        assertThat(format.format(value)).isEqualTo(text);
                    });
        }
    }

    @Test
    public void testNewDateFormat() throws ParseException {
        for (Locale locale : locales) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> builder().locale(locale).dateTimePattern("c").build().newDateFormat());
        }

        Date value = DateUtil.parse("2000-01-01");

        DateFormat f1 = builder().locale(FRANCE).build().newDateFormat();
        assertThat(f1.format(value)).isEqualTo("1 janv. 2000");
        assertThat(f1.parse("1 janv. 2000")).isEqualTo(value);

        DateFormat f2 = builder().locale(US).build().newDateFormat();
        assertThat(f2.format(value)).isEqualTo("Jan 1, 2000");
        assertThat(f2.parse("Jan 1, 2000")).isEqualTo(value);

        DateFormat f3 = builder().locale(FRANCE).dateTimePattern("yyyy-MMM").build().newDateFormat();
        assertThat(f3.format(value)).isEqualTo("2000-janv.");
        assertThat(f3.parse("2000-janv.")).isEqualTo(value);

        DateFormat f4 = builder().locale(US).dateTimePattern("yyyy-MMM").build().newDateFormat();
        assertThat(f4.format(value)).isEqualTo("2000-Jan");
        assertThat(f4.parse("2000-Jan")).isEqualTo(value);

        BiConsumer<ObsFormat, DateFormat> equivalence = (o, r) -> {
            try {
                DateFormat l = o.newDateFormat();
                assertThat(l.parseObject(r.format(value))).isEqualTo(value);
                assertThat(r.parseObject(l.format(value))).isEqualTo(value);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        };

        equivalence.accept(builder().locale(FRANCE).dateTimePattern("yyyy-MMM").build(), new SimpleDateFormat("yyyy-MMM", FRANCE));
        equivalence.accept(builder().locale(NULL_AS_SYSTEM_DEFAULT).dateTimePattern("yyyy-MMM").build(), new SimpleDateFormat("yyyy-MMM"));
        equivalence.accept(builder().locale(FRANCE).build(), SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, FRANCE));
        equivalence.accept(builder().locale(NULL_AS_SYSTEM_DEFAULT).build(), SimpleDateFormat.getDateInstance());
    }

    @Test
    public void testNewDateTimeFormatter() {
        for (Locale locale : locales) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> builder().locale(locale).dateTimePattern("p").build().newDateTimeFormatter());

            DateTimeFormatter formatter = builder().locale(locale).build().newDateTimeFormatter();
            for (LocalDateTime date : dates) {
                assertThat(formatter.parseBest(formatter.format(date), temporalQueries)).isEqualTo(date);
            }
        }

        LocalDateTime value = LocalDate.of(2000, 1, 1).atStartOfDay();

        DateTimeFormatter f1 = builder().locale(FRANCE).build().newDateTimeFormatter();
        assertThat(f1.format(value)).isEqualTo("1 janv. 2000 00:00:00");
        assertThat(f1.parseBest("1 janv. 2000", temporalQueries)).isEqualTo(value);

        DateTimeFormatter f2 = builder().locale(US).build().newDateTimeFormatter();
        assertThat(f2.format(value)).isEqualTo("Jan 1, 2000 12:00:00 AM");
        assertThat(f2.parseBest("Jan 1, 2000", temporalQueries)).isEqualTo(value);

        DateTimeFormatter f3 = builder().locale(FRANCE).dateTimePattern("yyyy-MMM").build().newDateTimeFormatter();
        assertThat(f3.format(value)).isEqualTo("2000-janv.");
        assertThat(f3.parseBest("2000-janv.", temporalQueries)).isEqualTo(value);

        DateTimeFormatter f4 = builder().locale(US).dateTimePattern("yyyy-MMM").build().newDateTimeFormatter();
        assertThat(f4.format(value)).isEqualTo("2000-Jan");
        assertThat(f4.parseBest("2000-Jan", temporalQueries)).isEqualTo(value);

        BiConsumer<ObsFormat, DateFormat> equivalence = (l, r) -> {
            try {
                Date x = Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
                assertThat(l.dateTimeParser().parse(r.format(x))).isEqualTo(value);
                assertThat(r.parseObject(l.dateTimeFormatter().formatAsString(value))).isEqualTo(x);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        };

        equivalence.accept(builder().locale(FRANCE).dateTimePattern("yyyy-MMM").build(), new SimpleDateFormat("yyyy-MMM", FRANCE));
        equivalence.accept(builder().locale(NULL_AS_SYSTEM_DEFAULT).dateTimePattern("yyyy-MMM").build(), new SimpleDateFormat("yyyy-MMM"));
        equivalence.accept(builder().locale(FRANCE).build(), SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, FRANCE));
        equivalence.accept(builder().locale(NULL_AS_SYSTEM_DEFAULT).build(), SimpleDateFormat.getDateInstance());
    }

    private final TemporalQuery[] temporalQueries = {LocalDateTime::from, o -> LocalDate.from(o).atStartOfDay()};

    private final Locale[] locales = {
            NULL_AS_SYSTEM_DEFAULT, FRANCE, FRENCH, US, JAPAN
    };

    private final LocalDateTime[] dates = {
            LocalDate.of(2000, 01, 01).atStartOfDay(),
            LocalDate.of(2000, 01, 01).atTime(11, 0, 0),
            LocalDate.of(2000, 01, 01).atTime(23, 59, 59),
            LocalDate.of(2000, 12, 31).atStartOfDay(),
            LocalDate.of(2000, 12, 31).atTime(12, 0, 0),
            LocalDate.of(2000, 12, 31).atTime(23, 59, 59)
    };

    private static char groupingSeparator(NumberFormat format) {
        return (((DecimalFormat) format).getDecimalFormatSymbols()).getGroupingSeparator();
    }

    private static char decimalSeparator(NumberFormat format) {
        return (((DecimalFormat) format).getDecimalFormatSymbols()).getDecimalSeparator();
    }
}
