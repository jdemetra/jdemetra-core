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

import static demetra.timeseries.TsFrequency.*;
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
                .extracting("origin", "freq", "offset")
                .containsExactly(DEFAULT_ORIGIN, MONTHLY, 493L);

        assertThat(quarterly(2011, 2))
                .extracting("origin", "freq", "offset")
                .containsExactly(DEFAULT_ORIGIN, QUARTERLY, 165L);
    }

    @Test
    public void testBuilder() {
        assertThat(builder().build()).isNotNull();

        assertThat(builder().freq(MONTHLY).build())
                .isEqualTo(new TsPeriod(DEFAULT_ORIGIN, MONTHLY, 0))
                .extracting("origin", "freq", "offset")
                .containsExactly(DEFAULT_ORIGIN, MONTHLY, 0L);

        assertThat(builder().freq(MONTHLY).origin(d2011_02_01).build())
                .isEqualTo(new TsPeriod(d2011_02_01_0000, MONTHLY, -493L))
                .extracting("origin", "freq", "offset")
                .containsExactly(d2011_02_01_0000, MONTHLY, -493L);

        assertThat(builder().freq(MONTHLY).origin(d2011_02_01_1337).build())
                .isEqualTo(new TsPeriod(d2011_02_01_1337, MONTHLY, -493L))
                .extracting("origin", "freq", "offset")
                .containsExactly(d2011_02_01_1337, MONTHLY, -493L);

        assertThat(builder().origin(d2011_02_01).freq(of(2, ChronoUnit.DECADES)).plus(2).build())
                .isEqualTo(new TsPeriod(d2011_02_01_0000, of(2, ChronoUnit.DECADES), 0))
                .extracting("origin", "freq", "offset")
                .containsExactly(d2011_02_01_0000, of(2, ChronoUnit.DECADES), 0L);

        assertThat(builder()).satisfies(o -> {
            assertThat(o.toShortString()).isEqualTo("P1M#0");
            assertThat(o.plus(1).toShortString()).isEqualTo("P1M#1");
            assertThat(o.plus(-2).toShortString()).isEqualTo("P1M#-1");
        });

        assertThat(TsPeriod.builder().freq(DAILY).origin(d2011_02_01).offset(2).build())
                .isEqualTo(TsPeriod.monthly(1970, 1).withFreq(DAILY).withOrigin(d2011_02_01).withOffset(2))
                .isNotEqualTo(TsPeriod.builder().freq(DAILY).offset(2).origin(d2011_02_01).build());
    }

    @Test
    public void testEquals() {
        assertThat(of(YEARLY, d2011_02_01))
                .isEqualTo(of(YEARLY, d2011_02_01))
                .isNotEqualTo(of(YEARLY, d2011_02_01).next())
                .isNotEqualTo(of(YEARLY, d2011_02_01).withOrigin(d2011_02_01))
                .isNotEqualTo(of(YEARLY, d2011_02_01).withFreq(HOURLY))
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

        assertThat(monthly(2011, 2).withOrigin(d2011_02_01))
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

        assertThat(monthly(2011, 2).withOrigin(d2011_02_01).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).withOrigin(d2011_02_01).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withOrigin(d2011_02_01).isAfter(monthly(2011, 1))).isTrue();

        assertThatThrownBy(() -> monthly(2011, 2).isAfter(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsBefore() {
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 1))).isFalse();

        assertThat(monthly(2011, 2).withOrigin(d2011_02_01).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).withOrigin(d2011_02_01).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withOrigin(d2011_02_01).isBefore(monthly(2011, 1))).isFalse();

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
        assertThatThrownBy(() -> of(YEARLY, d2011_02_01).withFreq(null)).isInstanceOf(NullPointerException.class);
        assertThat(of(YEARLY, d2011_02_01).withFreq(MONTHLY)).isEqualTo(of(MONTHLY, LocalDate.of(2011, 1, 1)));
        assertThat(of(MONTHLY, d2011_02_01).withFreq(YEARLY)).isEqualTo(of(YEARLY, d2011_02_01));
    }

    @Test
    public void testWithOrigin() {
        assertThatThrownBy(() -> of(YEARLY, d2011_02_01).withOrigin((LocalDate) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(YEARLY, d2011_02_01).withOrigin((LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThat(of(DAILY, d2011_02_01).withOrigin(d2011_02_01)).isEqualTo(new TsPeriod(d2011_02_01_0000, DAILY, 0));
        assertThat(of(DAILY, d2011_02_01.plusDays(1)).withOrigin(d2011_02_01)).isEqualTo(new TsPeriod(d2011_02_01_0000, DAILY, 1));
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
        assertThat(of(YEARLY, d2011_02_01).withOrigin(d2011_02_01).next().toShortString()).isEqualTo("P1Y#1@2011-02-01T00:00");
        assertThat(of(DAILY, d2011_02_01).toShortString()).isEqualTo("P1D#15006");
    }

    @Test
    public void testToBuilder() {
        assertThat(of(DAILY, d2011_02_01).toBuilder().build()).isEqualTo(of(DAILY, d2011_02_01));
    }

    @Test
    public void testParse() {
        assertThatThrownBy(() -> TsPeriod.parse("hello")).isInstanceOf(DateTimeParseException.class);
        assertThat(TsPeriod.parse("P1M#2")).isEqualTo(TsPeriod.builder().freq(MONTHLY).offset(2).build());
        assertThat(TsPeriod.parse("P1M#-1")).isEqualTo(TsPeriod.builder().freq(MONTHLY).offset(-1).build());
        assertThat(TsPeriod.parse("P1M#2@2011-02-01T00:00")).isEqualTo(TsPeriod.builder().freq(MONTHLY).origin(d2011_02_01).offset(2).build());
    }

    private final LocalDate d2011_02_01 = LocalDate.of(2011, 2, 1);
    private final LocalDateTime d2011_02_01_0000 = d2011_02_01.atStartOfDay();
    private final LocalDateTime d2011_02_01_1337 = d2011_02_01.atTime(13, 37);
}
