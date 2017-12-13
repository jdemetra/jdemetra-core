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
import java.time.temporal.ChronoUnit;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
@lombok.Getter
public enum GuessingUnit {

    YEAR(TsUnit.YEAR, TsPeriod.EPOCH, 2),
    HALF_YEAR(TsUnit.HALF_YEAR, TsPeriod.EPOCH, 6),
    QUADRI_MONTH(TsUnit.of(4, ChronoUnit.MONTHS), TsPeriod.EPOCH, 4),
    QUARTER(TsUnit.QUARTER, TsPeriod.EPOCH, 2),
    MONTH(TsUnit.MONTH, TsPeriod.EPOCH, 2),
    WEEK_MONDAY(TsUnit.WEEK, TsPeriod.EPOCH.plusDays(4), 3),
    DAY(TsUnit.DAY, TsPeriod.EPOCH, 2),
    HOUR(TsUnit.HOUR, TsPeriod.EPOCH, 2),
    MINUTE(TsUnit.MINUTE, TsPeriod.EPOCH, 2),
    SECOND(TsUnit.SECOND, TsPeriod.EPOCH, 2);

    private final TsUnit tsUnit;
    private final LocalDateTime reference;
    private final int minimumObsCount;

    public TsPeriod atId(long id) {
        return TsPeriod.builder().unit(tsUnit).reference(reference).id(id).build();
    }

    public TsPeriod atDate(LocalDateTime start) {
        return TsPeriod.builder().unit(tsUnit).reference(reference).date(start).build();
    }
}
