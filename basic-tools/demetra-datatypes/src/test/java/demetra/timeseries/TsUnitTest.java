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
        assertThat(YEARLY.ratio(YEARLY)).isEqualTo(1);
        assertThat(HALF_YEARLY.ratio(YEARLY)).isEqualTo(2);
        assertThat(QUADRI_MONTHLY.ratio(YEARLY)).isEqualTo(3);
        assertThat(QUARTERLY.ratio(YEARLY)).isEqualTo(4);
        assertThat(BI_MONTHLY.ratio(YEARLY)).isEqualTo(6);
        assertThat(MONTHLY.ratio(YEARLY)).isEqualTo(12);
        assertThat(MONTHLY.ratio(QUARTERLY)).isEqualTo(3);

        // no ratio
        assertThat(YEARLY.ratio(MONTHLY)).isEqualTo(NO_RATIO);
        assertThat(YEARLY.ratio(QUARTERLY)).isEqualTo(NO_RATIO);
        assertThat(HALF_YEARLY.ratio(QUARTERLY)).isEqualTo(NO_RATIO);

        // difficult ratio
        assertThat(QUARTERLY.ratio(QUADRI_MONTHLY)).isEqualTo(NO_STRICT_RATIO);
        assertThat(MINUTELY.ratio(YEARLY)).isEqualTo(NO_STRICT_RATIO);
    }

    @Test
    public void testParse() {
        assertThatThrownBy(() -> TsUnit.parse("hello")).isInstanceOf(DateTimeParseException.class);

        assertThat(parse("")).isEqualTo(UNDEFINED);
        assertThat(parse("P1Y")).isEqualTo(YEARLY);
        assertThat(parse("P6M")).isEqualTo(HALF_YEARLY);
        assertThat(parse("P4M")).isEqualTo(QUADRI_MONTHLY);
        assertThat(parse("P3M")).isEqualTo(QUARTERLY);
        assertThat(parse("P2M")).isEqualTo(BI_MONTHLY);
        assertThat(parse("P1M")).isEqualTo(MONTHLY);
        assertThat(parse("P7D")).isEqualTo(WEEKLY);
        assertThat(parse("P1D")).isEqualTo(DAILY);
        assertThat(parse("PT1H")).isEqualTo(HOURLY);
        assertThat(parse("PT1M")).isEqualTo(MINUTELY);

        assertThat(parse("P1W")).isEqualTo(WEEKLY);
    }

    @Test
    public void testToIsoString() {
        assertThat(UNDEFINED.toIsoString()).isEmpty();
        assertThat(YEARLY.toIsoString()).isEqualTo("P1Y");
        assertThat(HALF_YEARLY.toIsoString()).isEqualTo("P6M");
        assertThat(QUADRI_MONTHLY.toIsoString()).isEqualTo("P4M");
        assertThat(QUARTERLY.toIsoString()).isEqualTo("P3M");
        assertThat(BI_MONTHLY.toIsoString()).isEqualTo("P2M");
        assertThat(MONTHLY.toIsoString()).isEqualTo("P1M");
        assertThat(WEEKLY.toIsoString()).isEqualTo("P7D");
        assertThat(DAILY.toIsoString()).isEqualTo("P1D");
        assertThat(HOURLY.toIsoString()).isEqualTo("PT1H");
        assertThat(MINUTELY.toIsoString()).isEqualTo("PT1M");
    }

    @Test
    public void testConstants() {
        assertThat(UNDEFINED)
                .isEqualTo(of(1, ChronoUnit.FOREVER))
                .extracting(TsUnit::getAmount, TsUnit::getChronoUnit)
                .containsExactly(1L, ChronoUnit.FOREVER);
    }
}
