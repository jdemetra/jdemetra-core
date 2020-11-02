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

import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.math.matrices.MatrixType;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor
public class HolidaysCorrectedTradingDays implements ITradingDaysVariable, ISystemVariable {

    public static interface HolidaysCorrector {

        /**
         * Gets the number of days corresponding to the holidays
         *
         * @param domain
         * @return The (weighted) number of holidays for each period of the
         * domain. The different columns of the matrix correspond to
         * Mondays...Sundays
         */
        MatrixType holidaysCorrection(TsDomain domain);

    }

    private DayClustering clustering;
    private boolean contrast;
    private boolean normalized;
    private HolidaysCorrector corrector;

    public HolidaysCorrectedTradingDays(GenericTradingDays td, HolidaysCorrector corrector) {
        this.clustering = td.getClustering();
        this.contrast = td.isContrast();
        this.normalized = td.isNormalized();
        this.corrector = corrector;
    }

    @Override
    public int dim() {
        int n = clustering.getGroupsCount();
        return contrast ? n - 1 : n;
    }

}
