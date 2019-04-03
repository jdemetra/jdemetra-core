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

import java.util.List;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class CalendarTimeSeries implements TimeSeriesData<CalendarPeriod, CalendarPeriodObs> {

    private final CalendarPeriods domain;
    private final DoubleSeq values;

    public static CalendarTimeSeries of(List<CalendarPeriodObs> data) {
        int n = data.size();
        CalendarPeriod[] periods = new CalendarPeriod[n];
        double[] v = new double[n];

        int pos = 0;
        for (CalendarPeriodObs obs : data) {
            periods[pos] = obs.getPeriod();
            v[pos++] = obs.getValue();
        }
        return new CalendarTimeSeries(CalendarPeriods.of(periods), DoubleSeq.of(v));
    }

    private CalendarTimeSeries(CalendarPeriods domain, DoubleSeq values) {
        this.domain = domain;
        this.values = values;
    }

    @Override
    public TimeSeriesDomain<CalendarPeriod> getDomain() {
        return domain;
    }

    @Override
    public DoubleSeq getValues() {
        return values;
    }

    @Override
    public CalendarPeriodObs get(int index) throws IndexOutOfBoundsException {
        return CalendarPeriodObs.of(domain.get(index), values.get(index));
    }

    @Override
    public int length() {
        return values.length();
    }

}
