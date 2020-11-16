/*
 * Copyright 2020 National Bank of Belgium
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

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TimePeriodTest {

    @Test
    public void testISO() {
        TimePeriod x = TimePeriod.of(LocalDateTime.of(2010, 2, 17, 11, 3), LocalDateTime.of(2010, 3, 17, 11, 3));

        assertThat(x.toISO8601())
                .isEqualTo("2010-02-17T11:03/2010-03-17T11:03");

        assertThat(TimePeriod.parse(x.toISO8601()))
                .isEqualTo(x);
    }
}
