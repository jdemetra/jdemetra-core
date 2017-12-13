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
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsUnitTest {

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
        assertThat(UNDEFINED.toIsoString()).isEmpty();
        assertThat(DECADE.toIsoString()).isEqualTo("P10Y");
        assertThat(CENTURY.toIsoString()).isEqualTo("P100Y");
        assertThat(YEAR.toIsoString()).isEqualTo("P1Y");
        assertThat(HALF_YEAR.toIsoString()).isEqualTo("P6M");
        assertThat(QUARTER.toIsoString()).isEqualTo("P3M");
        assertThat(MONTH.toIsoString()).isEqualTo("P1M");
        assertThat(WEEK.toIsoString()).isEqualTo("P7D");
        assertThat(DAY.toIsoString()).isEqualTo("P1D");
        assertThat(HOUR.toIsoString()).isEqualTo("PT1H");
        assertThat(MINUTE.toIsoString()).isEqualTo("PT1M");
        assertThat(SECOND.toIsoString()).isEqualTo("PT1S");
    }

    @Test
    public void testConstants() {
        assertThat(UNDEFINED)
                .isEqualTo(of(1, ChronoUnit.FOREVER))
                .extracting(TsUnit::getAmount, TsUnit::getChronoUnit)
                .containsExactly(1L, ChronoUnit.FOREVER);
    }
}
