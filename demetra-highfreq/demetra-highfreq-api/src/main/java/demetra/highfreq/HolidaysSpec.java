/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.highfreq;

import demetra.data.Parameter;
import demetra.timeseries.calendars.CalendarManager;
import demetra.timeseries.calendars.HolidaysOption;
import demetra.timeseries.regression.HolidaysVariable;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder = true)
public final class HolidaysSpec {

    public final static HolidaysSpec DEFAULT_UNUSED = HolidaysSpec.builder().build();

    private String holidays;
    private HolidaysOption holidaysOption;
    private boolean single;
    private int[] nonWorkingDays;

    private Parameter[] coefficients;

    public boolean isUsed() {
        return holidays != null && !holidays.equals(CalendarManager.DEF);
    }

    public boolean hasFixedCoefficients() {
        return Parameter.hasFixedParameters(coefficients);
    }

    public static Builder builder() {
        return new Builder()
                .holidaysOption(HolidaysOption.Skip)
                .single(false)
                .nonWorkingDays(HolidaysVariable.NONWORKING_WE);
    }
    
}
