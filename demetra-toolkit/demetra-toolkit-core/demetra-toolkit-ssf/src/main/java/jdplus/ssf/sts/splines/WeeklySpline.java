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
import demetra.math.Constants;
import demetra.timeseries.calendars.CalendarUtility;

/**
 *
 * @author palatej
 */
@lombok.Value
public class WeeklySpline implements SplineDefinition {

    private static final double DENOM = 1.0 / 7.0, PERIOD = 365 * DENOM;

    private int startYear, startDay;
    int[] days;

    @Override
    public double getPeriod() {
        return PERIOD;
    }

    @Override
    public DoubleSeq nodes() {
        return DoubleSeq.onMapping(days.length, i -> days[i] * DENOM);
    }

    @Override
    public DoubleSeq observations(int period) {
        // number of days (full years) since the startYear to this period
        int bsum = 0;
        for (int i = 0; i < period; ++i) {
            bsum += CalendarUtility.isLeap(startYear + i) ? 366 : 365;
        }
        boolean lp = CalendarUtility.isLeap(startYear + period);
        int nstartDay = startDay - (bsum % 7);
        if (nstartDay < 0) {
            nstartDay += 7;
        }
        int nweeks = 52;
        if (nstartDay < (lp ? 2 : 1)) {
            ++nweeks;
        }
        double day = nstartDay * DENOM, day2 = (nstartDay - 1) * DENOM, FEB = 58.1 * DENOM; // to avoid roundoff errors
        return DoubleSeq.onMapping(nweeks, i -> (!lp) ? day + i : i > FEB ? day2 + i : day + i);
    }

}
