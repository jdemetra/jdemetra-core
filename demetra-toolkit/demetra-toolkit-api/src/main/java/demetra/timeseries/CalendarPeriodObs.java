/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.timeseries;

import java.time.LocalDate;

/**
 *
 * @author Jean Palate
 */
@lombok.Value(staticConstructor = "of")
public class CalendarPeriodObs implements TimeSeriesObs<CalendarPeriod> {

    @lombok.NonNull
    LocalDate start, end;
    double value;
    
    public static CalendarPeriodObs of(CalendarPeriod period, double value){
        return new CalendarPeriodObs(period.getStart(), period.getEnd(), value);
    }
    
    @Override
    public CalendarPeriod getPeriod(){
        return CalendarPeriod.of(start, end);
    }
}
