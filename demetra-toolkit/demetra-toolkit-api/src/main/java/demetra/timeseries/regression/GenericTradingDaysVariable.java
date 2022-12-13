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
import nbbrd.design.Development;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.TradingDaysType;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
@Development(status = Development.Status.Release)
public class GenericTradingDaysVariable implements ITradingDaysVariable, ISystemVariable {

    private DayClustering clustering;
    private GenericTradingDays.Type variableType;

    public GenericTradingDaysVariable(GenericTradingDays td) {
        this.clustering = td.getClustering();
        this.variableType=td.getType();
    }

    @Override
    public int dim() {
        int n = clustering.getGroupsCount();
        return variableType == GenericTradingDays.Type.CONTRAST ? n - 1 : n;
    }
    
    @Override
    public TradingDaysType getTradingDaysType(){
        return clustering.getType();
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "Trading days";
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context){
        return description(clustering, idx);
    }

    static final String[] TD2 = new String[]{"week", "week-end"};
    static final String[] TD2c = new String[]{"mon-sat", "sunday"};
    static final String[] TD7 = new String[]{"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    static final String[] TD3 = new String[]{"week", "saturday", "sunday"};
    static final String[] TD3c = new String[]{"mon-thu", "fri-sat", "sunday"};
    static final String[] TD4 = new String[]{"mon-thu", "friday", "saturday", "sunday"};

    public static String description(DayClustering dc, int idx) {
        if (dc.equals(DayClustering.TD2)) {
            return TD2[idx];
        } else if (dc.equals(DayClustering.TD7)) {
            return TD7[idx];
        } else if (dc.equals(DayClustering.TD3)) {
            return TD3[idx];
        } else if (dc.equals(DayClustering.TD2c)) {
            return TD2c[idx];
        } else if (dc.equals(DayClustering.TD3c)) {
            return TD3c[idx];
        } else if (dc.equals(DayClustering.TD4)) {
            return TD4[idx];
        } else {
            return "td-" + (idx + 1);
        }
    }

}
