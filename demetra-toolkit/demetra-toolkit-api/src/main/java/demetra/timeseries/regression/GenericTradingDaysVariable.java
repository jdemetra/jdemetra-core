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

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
@Development(status = Development.Status.Release)
public class GenericTradingDaysVariable implements ITradingDaysVariable, ISystemVariable {

    private DayClustering clustering;
    private boolean contrast;
    private boolean normalized;

    public GenericTradingDaysVariable(GenericTradingDays td) {
        this.clustering = td.getClustering();
        this.contrast = td.isContrast();
        this.normalized = td.isNormalized();
    }

    @Override
    public int dim() {
        int n = clustering.getGroupsCount();
        return contrast ? n - 1 : n;
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(D context) {
        return "td";
    }

    @Override
    public <D extends TimeSeriesDomain<?>> String description(int idx, D context){
        return description(clustering, idx);
    }

    static final String[] WD = new String[]{"week", "week-end"};
    static final String[] TD = new String[]{"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
    static final String[] TD3 = new String[]{"week", "saturday", "sunday"};
    static final String[] TD3c = new String[]{"mon-thu", "fri-sat", "sunday"};
    static final String[] TD4 = new String[]{"mon-thu", "friday", "saturday", "sunday"};

    static String description(DayClustering dc, int idx) {
        if (dc.equals(DayClustering.TD2)) {
            return WD[idx];
        } else if (dc.equals(DayClustering.TD7)) {
            return TD[idx];
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
