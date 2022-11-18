/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.timeseries.regression;

import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.calendars.Calendar;
import nbbrd.design.Development;
import demetra.timeseries.calendars.DayClustering;
import static demetra.timeseries.regression.GenericTradingDaysVariable.TD2;
import static demetra.timeseries.regression.GenericTradingDaysVariable.TD3;
import static demetra.timeseries.regression.GenericTradingDaysVariable.TD3c;
import static demetra.timeseries.regression.GenericTradingDaysVariable.TD4;
import static demetra.timeseries.regression.GenericTradingDaysVariable.TD7;
import java.time.DayOfWeek;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
@Development(status = Development.Status.Release)
public class GenericHolidaysVariable implements ITradingDaysVariable, ISystemVariable {

    private DayClustering clustering;
    private Calendar calendar;
    private DayOfWeek holiday;

    public GenericHolidaysVariable(DayClustering clustering, Calendar calendar) {
        this.clustering = clustering;
        this.calendar=calendar;
        this.holiday=DayOfWeek.SUNDAY;
    }

    @Override
    public int dim() {
        int n = clustering.getGroupsCount();
        return n-1;
     }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "Trading days";
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context){
        return description(clustering, idx);
    }


    public static String description(DayClustering dc, int idx) {
        if (dc.equals(DayClustering.TD2)) {
            return TD2[idx];
        } else if (dc.equals(DayClustering.TD7)) {
            return TD7[idx];
        } else if (dc.equals(DayClustering.TD3)) {
            return TD3[idx];
        } else if (dc.equals(DayClustering.TD3c)) {
            return TD3c[idx];
        } else if (dc.equals(DayClustering.TD4)) {
            return TD4[idx];
        } else {
            return "td-" + (idx + 1);
        }
    }

}
