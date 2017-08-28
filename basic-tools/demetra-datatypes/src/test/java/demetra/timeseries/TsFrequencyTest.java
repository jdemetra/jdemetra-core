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

import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsFrequencyTest {

    @Test
    public void testParse() {
        assertThatThrownBy(() -> TsFrequency.parse("hello")).isInstanceOf(DateTimeParseException.class);
        assertThat(TsFrequency.parse("P2Y")).isEqualTo(TsFrequency.of(2, ChronoUnit.YEARS));
        assertThat(TsFrequency.parse("P3M")).isEqualTo(TsFrequency.of(3, ChronoUnit.MONTHS));
        assertThat(TsFrequency.parse("PT4H")).isEqualTo(TsFrequency.of(4, ChronoUnit.HOURS));
    }
}
