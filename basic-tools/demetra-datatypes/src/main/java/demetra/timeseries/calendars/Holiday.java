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
import demetra.timeseries.ValidityPeriod;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class Holiday {


    private final IHoliday day;
    private ValidityPeriod validity;

    public Holiday(IHoliday day) {
        this.day = day;
    }

    /**
     * @return the day
     */
    public IHoliday getDay() {
        return day;
    }

    public ValidityPeriod getValidityPeriod() {
        return validity;
    }

    public void setValidityPeriod(ValidityPeriod value) {
        validity = value;
    }

    public boolean isStartSpecified() {
        return validity != null && validity.isStartSpecified();
    }

    public boolean isEndSpecified() {
        return validity != null && validity.isEndSpecified();
    }

    public TsPeriod getStart() {
        return TsPeriod.of(TsUnit.DAILY, validity == null ? LocalDate.MIN : validity.getStart().toLocalDate());
    }

    public TsPeriod getEnd() {
        return TsPeriod.of(TsUnit.DAILY, validity == null ? LocalDate.MAX : validity.getEnd().toLocalDate());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Holiday && equals((Holiday) obj));
    }

    private boolean equals(Holiday other) {
        return day.equals(other.day) && Objects.equals(validity, other.validity);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.day.hashCode();
        hash = 97 * hash + Objects.hashCode(this.validity);
        return hash;
    }
}
