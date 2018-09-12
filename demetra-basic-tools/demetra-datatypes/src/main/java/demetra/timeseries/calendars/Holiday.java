/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package demetra.timeseries.calendars;

import demetra.design.Development;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsPeriod;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
public class Holiday {

    private final IHoliday day;
    private ValidityPeriod validityPeriod;

    public Holiday(IHoliday day) {
        this.day = day;
        validityPeriod=null;
    }

    public Holiday(IHoliday day, ValidityPeriod validityPeriod) {
        this.day = day;
        this.validityPeriod=validityPeriod;
    }

    public boolean isStartSpecified() {
        return validityPeriod != null && validityPeriod.isStartSpecified();
    }

    public boolean isEndSpecified() {
        return validityPeriod != null && validityPeriod.isEndSpecified();
    }

    public TsPeriod getStart() {
        return TsPeriod.of(TsUnit.DAY, validityPeriod == null ? LocalDate.MIN : validityPeriod.getStart().toLocalDate());
    }

    public TsPeriod getEnd() {
        return TsPeriod.of(TsUnit.DAY, validityPeriod == null ? LocalDate.MAX : validityPeriod.getEnd().toLocalDate());
    }

}
