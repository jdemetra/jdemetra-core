/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.ssf.sts.splines;

import demetra.data.DoubleSeq;
import demetra.timeseries.calendars.CalendarUtility;

/**
 *
 * @author palatej
 */
@lombok.Value
public class DailySpline implements SplineDefinition {

    int startYear;
    int[] days;

    @Override
    public double getPeriod() {
        return 365.0;
    }

    @Override
    public DoubleSeq nodes() {
        return DoubleSeq.onMapping(days.length, i -> days[i]);
    }

    @Override
    public DoubleSeq observations(int period) {
        boolean lp = CalendarUtility.isLeap(startYear + period);
        return DoubleSeq.onMapping(lp ? 366 : 365, i -> (!lp) ? i : (i > 58) ? i - 1 : i);
    }

}
