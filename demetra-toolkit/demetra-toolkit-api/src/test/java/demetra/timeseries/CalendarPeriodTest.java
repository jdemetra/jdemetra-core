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

import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CalendarPeriodTest {
    
    @Test
    public void testISO() {
        CalendarPeriod x = CalendarPeriod.of(LocalDate.of(2010, 2, 17), LocalDate.of(2010, 3, 17));

        assertThat(x.toISO8601())
                .isEqualTo("2010-02-17T00:00/2010-03-17T00:00");
        
        assertThat(CalendarPeriod.parse(x.toISO8601()))
                .isEqualTo(x);
    }
}
