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
package demetra.timeseries;

import nbbrd.design.Development;
import java.time.LocalDate;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@Development(status = Development.Status.Release)
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ValidityPeriod {

    public static final ValidityPeriod ALWAYS = new ValidityPeriod(LocalDate.MIN, LocalDate.MAX);

    /**
     *
     * @param date Start, included
     * @return
     */
    public static ValidityPeriod from(LocalDate date) {
        return date == null ? ALWAYS : new ValidityPeriod(date, LocalDate.MAX);
    }

    /**
     *
     * @param date End, excluded
     * @return
     */
    public static ValidityPeriod to(LocalDate date) {
        return date == null ? ALWAYS : new ValidityPeriod(LocalDate.MIN, date);
    }

    /**
     *
     * @param date0 Start, included
     * @param date1 End, excluded
     * @return
     */
    public static ValidityPeriod between(LocalDate date0, LocalDate date1) {
        if (date0 == null) {
            return to(date1);
        } else if (date1 == null) {
            return from(date0);
        } else {
            return new ValidityPeriod(date0, date1);
        }
    }

    private final LocalDate start, end;

    public boolean isStartSpecified() {
        return start != LocalDate.MIN;
    }

    public boolean isEndSpecified() {
        return end != LocalDate.MAX;
    }

}
