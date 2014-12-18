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

package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.ValidityPeriod;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class SpecialDayEvent {

    public final ISpecialDay day;
    private ValidityPeriod validity_;

    public SpecialDayEvent(ISpecialDay day) {
        this.day = day;
    }

    public ValidityPeriod getValidityPeriod() {
        return validity_;
    }

    public void setValidityPeriod(ValidityPeriod value) {
        validity_ = value;
    }

    public Day getStart() {
        return validity_ == null ? Day.BEG : validity_.getStart();
    }

    public Day getEnd() {
        return validity_ == null ? Day.END : validity_.getEnd();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SpecialDayEvent && equals((SpecialDayEvent) obj));
    }
    
    private boolean equals(SpecialDayEvent other) {
        return day.equals(other.day) && Objects.equals(validity_, other.validity_);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.day.hashCode();
        hash = 97 * hash + Objects.hashCode(this.validity_);
        return hash;
    }
}
