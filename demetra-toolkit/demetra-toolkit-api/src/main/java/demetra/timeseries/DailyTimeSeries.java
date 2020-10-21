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
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
public class DailyTimeSeries implements TimeSeriesData<Day, DayObs> {

    private final LocalDate[] domain;
    private final DoubleSeq values;

    public static DailyTimeSeries of(List<DayObs> data) {
        int n = data.size();
        LocalDate[] days = new LocalDate[n];
        double[] v = new double[n];

        int pos = 0;
        for (DayObs obs : data) {
            days[pos] = obs.getDate();
            v[pos++] = obs.getValue();
        }
        return new DailyTimeSeries(days, DoubleSeq.of(v));
    }

    private DailyTimeSeries(LocalDate[] domain, DoubleSeq values) {
        this.domain = domain;
        this.values = values;
    }

    @Override
    public TimeSeriesDomain<Day> getDomain() {
        return Days.of(domain);
    }

    @Override
    public DoubleSeq getValues() {
        return values;
    }

    @Override
    public DayObs get(int index) throws IndexOutOfBoundsException {
        return DayObs.of(domain[index], values.get(index));
    }

    @NonNull
    @Override
    public Day getPeriod(@NonNegative int index) throws IndexOutOfBoundsException {
        return Day.of(domain[index]);
    }

    @Override
    public int length() {
        return domain.length;
    }

}
