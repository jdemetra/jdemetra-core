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
import java.time.LocalDateTime;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class ValidityPeriod {

    private final LocalDateTime start, end;

    public boolean isStartSpecified() {
        return start != null;
    }

    public boolean isEndSpecified() {
        return end != null;
    }
}
