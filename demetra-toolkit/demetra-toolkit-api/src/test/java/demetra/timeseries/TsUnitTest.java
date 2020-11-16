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
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.*;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsUnitTest {

    @Test
    @SuppressWarnings("null")
    public void testFactories() {
        assertThatIllegalArgumentException().isThrownBy(() -> TsUnit.of(-1, MONTHS));
        assertThatNullPointerException().isThrownBy(() -> TsUnit.of(1, null));

        Stream.of(supportedChronoUnits)
                .forEach(o -> assertThat(TsUnit.of(1, o)).isNotNull());

        Stream.of(unsupportedChronoUnits)
                .forEach(o -> assertThatThrownBy(() -> TsUnit.of(1, o)).isInstanceOf(UnsupportedTemporalTypeException.class));
    }

    @Test
    public void testRatio() {
        // easy ratio
        assertThat(YEAR.ratioOf(CENTURY)).isEqualTo(100);
        assertThat(YEAR.ratioOf(DECADE)).isEqualTo(10);
        assertThat(YEAR.ratioOf(YEAR)).isEqualTo(1);
        assertThat(HALF_YEAR.ratioOf(YEAR)).isEqualTo(2);
        assertThat(QUARTER.ratioOf(YEAR)).isEqualTo(4);
        assertThat(MONTH.ratioOf(YEAR)).isEqualTo(12);
        assertThat(MONTH.ratioOf(QUARTER)).isEqualTo(3);

        // no ratio
        assertThat(YEAR.ratioOf(MONTH)).isEqualTo(NO_RATIO);
        assertThat(YEAR.ratioOf(QUARTER)).isEqualTo(NO_RATIO);
        assertThat(HALF_YEAR.ratioOf(QUARTER)).isEqualTo(NO_RATIO);

        // difficult ratio
        assertThat(MINUTE.ratioOf(YEAR)).isEqualTo(NO_STRICT_RATIO);
        assertThat(DAY.ratioOf(YEAR)).isEqualTo(NO_STRICT_RATIO);
    }

    @Test
    public void testParse() {
        assertThatThrownBy(() -> TsUnit.parse("hello")).isInstanceOf(DateTimeParseException.class);

        assertThat(parse("")).isEqualTo(UNDEFINED);
        assertThat(parse("P1000Y")).isEqualTo(TsUnit.of(1, MILLENNIA));
        assertThat(parse("P100Y")).isEqualTo(CENTURY);
        assertThat(parse("P10Y")).isEqualTo(DECADE);
        assertThat(parse("P1Y")).isEqualTo(YEAR);
        assertThat(parse("P6M")).isEqualTo(HALF_YEAR);
        assertThat(parse("P3M")).isEqualTo(QUARTER);
        assertThat(parse("P1M")).isEqualTo(MONTH);
        assertThat(parse("P7D")).isEqualTo(WEEK);
        assertThat(parse("P1D")).isEqualTo(DAY);
        assertThat(parse("PT1H")).isEqualTo(HOUR);
        assertThat(parse("PT1M")).isEqualTo(MINUTE);
        assertThat(parse("PT1S")).isEqualTo(SECOND);
    }

    @Test
    public void testToIsoString() {
        assertThat(UNDEFINED.toISO8601()).isEmpty();
        assertThat(TsUnit.of(1, MILLENNIA).toISO8601()).isEqualTo("P1000Y");
        assertThat(CENTURY.toISO8601()).isEqualTo("P100Y");
        assertThat(DECADE.toISO8601()).isEqualTo("P10Y");
        assertThat(YEAR.toISO8601()).isEqualTo("P1Y");
        assertThat(HALF_YEAR.toISO8601()).isEqualTo("P6M");
        assertThat(QUARTER.toISO8601()).isEqualTo("P3M");
        assertThat(MONTH.toISO8601()).isEqualTo("P1M");
        assertThat(WEEK.toISO8601()).isEqualTo("P7D");
        assertThat(DAY.toISO8601()).isEqualTo("P1D");
        assertThat(HOUR.toISO8601()).isEqualTo("PT1H");
        assertThat(MINUTE.toISO8601()).isEqualTo("PT1M");
        assertThat(SECOND.toISO8601()).isEqualTo("PT1S");
    }

    @Test
    public void testConstants() {
        assertThat(UNDEFINED)
                .isEqualTo(of(1, ChronoUnit.FOREVER))
                .extracting(TsUnit::getAmount, TsUnit::getChronoUnit)
                .containsExactly(1L, ChronoUnit.FOREVER);
    }

    @Test
    public void testGcd() {
        assertThat(gcd("P14M", "P14M"))
                .as("same chrono, same amount")
                .isEqualTo("P14M");

        assertThat(gcd("P14M", "P7M"))
                .as("same chrono, compatible amount")
                .isEqualTo("P7M");

        assertThat(gcd("P14M", "P12M"))
                .as("same chrono, uncompatible amount")
                .isEqualTo("P2M");

        assertThat(gcd("P2Y", "P2M"))
                .as("compatible chrono, same amount")
                .isEqualTo("P2M");

        assertThat(gcd("P2Y", "P12M"))
                .as("compatible chrono, compatible amount")
                .isEqualTo("P12M");

        assertThat(gcd("P2Y", "P26M"))
                .as("compatible chrono, uncompatible amount")
                .isEqualTo("P2M");

        assertThat(gcd("P2M", "P2D"))
                .as("uncompatible chrono, same amount")
                .isEqualTo("P1D");

        assertThat(gcd("P2M", "P10D"))
                .as("uncompatible chrono, compatible amount")
                .isEqualTo("P1D");

        assertThat(gcd("P2M", "P11D"))
                .as("uncompatible chrono, uncompatible amount")
                .isEqualTo("P1D");
    }

    private final ChronoUnit[] supportedChronoUnits = {
        FOREVER, MILLENNIA, CENTURIES, DECADES, YEARS, MONTHS, WEEKS, DAYS, HALF_DAYS, HOURS, MINUTES, SECONDS
    };

    private final ChronoUnit[] unsupportedChronoUnits = {
        ERAS, MILLIS, MICROS, NANOS
    };

    private static String gcd(String a, String b) {
        return TsUnit.gcd(TsUnit.parse(a), TsUnit.parse(b)).toISO8601();
    }
}
