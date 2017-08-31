/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import static demetra.timeseries.TsUnit.*;
import static demetra.timeseries.TsPeriod.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsPeriodTest {

    @Test
    public void testFactories() {
        assertThatThrownBy(() -> of(null, d2011_02_01_0000)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(MONTHLY, (LocalDate) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(null, d2011_02_01)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(MONTHLY, (LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(null, 0)).isInstanceOf(NullPointerException.class);

        assertThat(monthly(2011, 2))
                .extracting("offset", "unit", "id")
                .containsExactly(DEFAULT_OFFSET, MONTHLY, 493L);

        assertThat(quarterly(2011, 2))
                .extracting("offset", "unit", "id")
                .containsExactly(DEFAULT_OFFSET, QUARTERLY, 165L);
    }

    @Test
    public void testBuilder() {
        assertThat(builder().build()).isNotNull();

        assertThat(builder().unit(MONTHLY).build())
                .isEqualTo(new TsPeriod(DEFAULT_OFFSET, MONTHLY, 0))
                .extracting("offset", "unit", "id")
                .containsExactly(DEFAULT_OFFSET, MONTHLY, 0L);

        assertThat(builder().unit(MONTHLY).offset(someOffset).build())
                .isEqualTo(new TsPeriod(someOffset, MONTHLY, -10L))
                .extracting("offset", "unit", "id")
                .containsExactly(someOffset, MONTHLY, -10L);

        assertThat(builder().unit(MONTHLY).offset(someOffset).build())
                .isEqualTo(new TsPeriod(someOffset, MONTHLY, -10L))
                .extracting("offset", "unit", "id")
                .containsExactly(someOffset, MONTHLY, -10L);

        assertThat(builder().offset(someOffset).unit(of(2, ChronoUnit.DECADES)).plus(2).build())
                .isEqualTo(new TsPeriod(someOffset, of(2, ChronoUnit.DECADES), -8))
                .extracting("offset", "unit", "id")
                .containsExactly(someOffset, of(2, ChronoUnit.DECADES), -8L);

        assertThat(builder()).satisfies(o -> {
            assertThat(o.toShortString()).isEqualTo("P1M#0");
            assertThat(o.plus(1).toShortString()).isEqualTo("P1M#1");
            assertThat(o.plus(-2).toShortString()).isEqualTo("P1M#-1");
        });

        assertThat(TsPeriod.builder().unit(DAILY).offset(someOffset).id(2).build())
                .isEqualTo(TsPeriod.monthly(1970, 1).withUnit(DAILY).withOffset(someOffset).withId(2))
                .isNotEqualTo(TsPeriod.builder().unit(DAILY).id(2).offset(someOffset).build());
    }

    @Test
    public void testEquals() {
        assertThat(of(YEARLY, d2011_02_01))
                .isEqualTo(of(YEARLY, d2011_02_01))
                .isNotEqualTo(of(YEARLY, d2011_02_01).next())
                .isNotEqualTo(of(YEARLY, d2011_02_01).withOffset(someOffset))
                .isNotEqualTo(of(YEARLY, d2011_02_01).withUnit(HOURLY))
                .isNotEqualTo(of(YEARLY, d2011_02_01).withDate(d2011_02_01.plusYears(1)))
                .isEqualTo(of(YEARLY, d2011_02_01).withDate(d2011_02_01_1337));
    }

    @Test
    public void testRange() {
        assertThat(monthly(2011, 2)).satisfies(o -> {
            assertThat(o.start()).isEqualTo(d2011_02_01_0000);
            assertThat(o.end()).isEqualTo(d2011_02_01_0000.plusMonths(1));
            assertThat(o.contains(d2011_02_01_0000)).isTrue();
            assertThat(o.contains(d2011_02_01_0000.plusDays(1))).isTrue();
            assertThat(o.contains(d2011_02_01_0000.plusMonths(1))).isFalse();
        });

        assertThat(minutely(2011, 2, 1, 13, 37)).satisfies(o -> {
            assertThat(o.start()).isEqualTo(d2011_02_01_1337);
            assertThat(o.end()).isEqualTo(d2011_02_01_1337.plusMinutes(1));
            assertThat(o.contains(d2011_02_01_1337)).isTrue();
            assertThat(o.contains(d2011_02_01_1337.plusSeconds(1))).isTrue();
            assertThat(o.contains(d2011_02_01_1337.plusMinutes(1))).isFalse();
        });
    }

    @Test
    public void testComparable() {
        assertThat(monthly(2011, 2))
                .isEqualByComparingTo(monthly(2011, 2))
                .isLessThan(monthly(2011, 3))
                .isGreaterThan(monthly(2011, 1));

        assertThat(monthly(2011, 2).withOffset(someOffset))
                .isEqualByComparingTo(monthly(2011, 2))
                .isLessThan(monthly(2011, 3))
                .isGreaterThan(monthly(2011, 1));

        assertThatThrownBy(() -> monthly(2011, 2).compareTo(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsAfter() {
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 1))).isTrue();

        assertThat(monthly(2011, 2).withOffset(someOffset).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).withOffset(someOffset).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withOffset(someOffset).isAfter(monthly(2011, 1))).isTrue();

        assertThatThrownBy(() -> monthly(2011, 2).isAfter(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsBefore() {
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 1))).isFalse();

        assertThat(monthly(2011, 2).withOffset(someOffset).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).withOffset(someOffset).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withOffset(someOffset).isBefore(monthly(2011, 1))).isFalse();

        assertThatThrownBy(() -> monthly(2011, 2).isBefore(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testNext() {
        assertThat(of(YEARLY, d2011_02_01).next()).isEqualTo(of(YEARLY, d2011_02_01.plusYears(1)));
    }

    @Test
    public void testPlus() {
        assertThat(of(YEARLY, d2011_02_01).plus(1)).isEqualTo(of(YEARLY, d2011_02_01).next());
        assertThat(of(YEARLY, d2011_02_01).plus(2)).isEqualTo(of(YEARLY, d2011_02_01.plusYears(2)));
        assertThat(of(YEARLY, d2011_02_01).plus(-1)).isEqualTo(of(YEARLY, d2011_02_01.plusYears(-1)));
        assertThat(of(HOURLY, d2011_02_01).plus(11)).isEqualTo(of(HOURLY, d2011_02_01_0000.plus(11, ChronoUnit.HOURS)));
    }

    @Test
    public void testWithFreq() {
        assertThatThrownBy(() -> of(YEARLY, d2011_02_01).withUnit(null)).isInstanceOf(NullPointerException.class);
        assertThat(of(YEARLY, d2011_02_01).withUnit(MONTHLY)).isEqualTo(of(MONTHLY, LocalDate.of(2011, 1, 1)));
        assertThat(of(MONTHLY, d2011_02_01).withUnit(YEARLY)).isEqualTo(of(YEARLY, d2011_02_01));
    }

    @Test
    public void testWithOffset() {
        assertThat(of(DAILY, d2011_02_01).withOffset(someOffset)).isEqualTo(new TsPeriod(someOffset, DAILY, 14996));
        assertThat(of(DAILY, d2011_02_01.plusDays(1)).withOffset(someOffset)).isEqualTo(new TsPeriod(someOffset, DAILY, 14997));
    }

    @Test
    public void testWithDate() {
        assertThatThrownBy(() -> of(YEARLY, d2011_02_01).withDate((LocalDate) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(YEARLY, d2011_02_01).withDate((LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThat(of(DAILY, d2011_02_01).withDate(d2011_02_01.plusDays(3))).isEqualTo(of(DAILY, d2011_02_01.plusDays(3)));
    }

    @Test
    public void testToShortString() {
        assertThat(of(YEARLY, d2011_02_01).toShortString()).isEqualTo("P1Y#41");
        assertThat(of(YEARLY, d2011_02_01).withOffset(someOffset).next().toShortString()).isEqualTo("P1Y#32@10");
        assertThat(of(DAILY, d2011_02_01).toShortString()).isEqualTo("P1D#15006");
    }

    @Test
    public void testToBuilder() {
        assertThat(of(DAILY, d2011_02_01).toBuilder().build()).isEqualTo(of(DAILY, d2011_02_01));
    }

    @Test
    public void testParse() {
        assertThatThrownBy(() -> TsPeriod.parse("hello")).isInstanceOf(DateTimeParseException.class);
        assertThat(TsPeriod.parse("P1M#2")).isEqualTo(TsPeriod.builder().unit(MONTHLY).id(2).build());
        assertThat(TsPeriod.parse("P1M#-1")).isEqualTo(TsPeriod.builder().unit(MONTHLY).id(-1).build());
        assertThat(TsPeriod.parse("P1M#2@10")).isEqualTo(TsPeriod.builder().unit(MONTHLY).offset(someOffset).id(2).build());
    }

    @Test
    public void testUntil() {
        assertThat(yearly(2010).until(yearly(2012))).isEqualTo(2);
        assertThat(yearly(2010).until(yearly(2010))).isEqualTo(0);
        assertThat(yearly(2010).until(yearly(2009))).isEqualTo(-1);

        assertThatThrownBy(() -> monthly(2011, 2).until(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    private final LocalDate d2011_02_01 = LocalDate.of(2011, 2, 1);
    private final int someOffset = 10;
    private final LocalDateTime d2011_02_01_0000 = d2011_02_01.atStartOfDay();
    private final LocalDateTime d2011_02_01_1337 = d2011_02_01.atTime(13, 37);
}
