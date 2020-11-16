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
package demetra.timeseries.calendars;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@Development(status = Development.Status.Alpha)
public class GenericTradingDays {

    private DayClustering clustering;
    private boolean contrast;
    private boolean normalized;

    public static GenericTradingDays contrasts(DayClustering clustering) {
        return new GenericTradingDays(clustering, true, false);
    }

    public static GenericTradingDays of(DayClustering clustering) {
        return new GenericTradingDays(clustering, false, false);
    }

    public static GenericTradingDays normalized(DayClustering clustering) {
        return new GenericTradingDays(clustering, false, true);
    }

    public int getCount() {
        int n = clustering.getGroupsCount();
        return contrast ? n - 1 : n;
    }

    public String getDescription(int idx) {
        return clustering.toString(idx);
    }

}
