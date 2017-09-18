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
package internal.tsprovider.util;

import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import java.time.LocalDateTime;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.Getter
public enum GuessingUnit {

    YEARLY(TsUnit.YEARLY, 0, 2),
    HALF_YEARLY(TsUnit.HALF_YEARLY, 0, 6),
    QUADRI_MONTHLY(TsUnit.QUADRI_MONTHLY, 0, 4),
    QUARTERLY(TsUnit.QUARTERLY, 0, 2),
    MONTHLY(TsUnit.MONTHLY, 0, 2),
    WEEKLY_MONDAY(TsUnit.WEEKLY, 4, 3),
    DAILY(TsUnit.DAILY, 0, 2),
    HOURLY(TsUnit.HOURLY, 0, 2),
    MINUTELY(TsUnit.MINUTELY, 0, 2);

    private final TsUnit tsUnit;
    private final int offset;
    private final int minimumObsCount;

    public TsPeriod atId(long id) {
        return TsPeriod.builder().unit(tsUnit).offset(offset).id(id).build();
    }

    public TsPeriod atDate(LocalDateTime start) {
        return TsPeriod.builder().unit(tsUnit).offset(offset).date(start).build();
    }
}
