/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.maths.matrices.MatrixType;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor
public class HolidaysCorrectedTradingDays implements ITradingDaysVariable {

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
