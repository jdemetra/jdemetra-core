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
package jdplus.timeseries.calendars;

import nbbrd.design.Development;
import demetra.timeseries.calendars.SingleDate;
import java.time.LocalDate;
import java.util.Iterator;

/**
 * TODO: move to basic
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
class SingleDateInfo implements HolidayInfo {

    final SingleDate fdate;

    SingleDateInfo(SingleDate fdate) {
        this.fdate = fdate;
    }

    @Override
    public LocalDate getDay() {
        return fdate.getDate();
    }

    static class SingleDateIterable implements Iterable<HolidayInfo> {

        private final SingleDate fdate;
        private final boolean valid;

        SingleDateIterable(SingleDate fdate, LocalDate fstart, LocalDate fend) {
            this.fdate = fdate;
            this.valid = ! fdate.getDate().isBefore(fstart) && fdate.getDate().isBefore(fend);
        }

        @Override
        public Iterator<HolidayInfo> iterator() {
            return new Iterator<HolidayInfo>() {
                boolean done =false;

                @Override
                public boolean hasNext() {
                    return valid && ! done;
                }

                @Override
                public HolidayInfo next() {
                    done = true;
                    return new SingleDateInfo(fdate);
                }
            };
        }
    }
}
