/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.maths.matrices.Matrix;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.GenericTradingDays;

/**
 *
 * @author palatej
 */
class TDFactory implements RegressionVariableFactory<GenericTradingDaysVariable> {

    static TDFactory FACTORY = new TDFactory();

    private TDFactory() {
    }

    @Override
    public boolean fill(GenericTradingDaysVariable var, TsPeriod start, Matrix buffer) {

        GenericTradingDays td;
        if (var.isContrast()) {
            td = GenericTradingDays.contrasts(var.getDayClustering());
        } else if (var.isNormalized()) {
            td = GenericTradingDays.normalized(var.getDayClustering());
        } else {
            td = GenericTradingDays.of(var.getDayClustering());
        }
        td.data(TsDomain.of(start, buffer.getRowsCount()), buffer.columnList());
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(GenericTradingDaysVariable var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
