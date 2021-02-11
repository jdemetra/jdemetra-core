/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.Constant;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.TimeSeriesInterval;
import demetra.timeseries.regression.TrendConstant;
import jdplus.data.DataBlock;
import jdplus.math.linearfilters.BackFilter;
import jdplus.math.polynomials.UnitRoots;

/**
 *
 * @author Jean Palate
 */
class TrendConstantFactory implements RegressionVariableFactory<TrendConstant>{

    static TrendConstantFactory FACTORY=new TrendConstantFactory();

    private TrendConstantFactory(){}

    @Override
    public boolean fill(TrendConstant var, TsPeriod start, Matrix buffer) {
        double[] D = UnitRoots.D(var.getD()).times(UnitRoots.D(start.getUnit().getAnnualFrequency(), var.getBd())).toArray();
        int d = D.length - 1;
        int n=buffer.getRowsCount();
        DataBlock m = buffer.column(0);
        m.set(d, 1);
        for (int i = d + 1; i < n; ++i) {
            double s = 1;
            for (int j = 1; j <= d; ++j) {
                s -= m.get(i - j) * D[j];
            }
            m.set(i, s);
        }
         return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(TrendConstant var, D domain, Matrix buffer) {
        return false;
    }
  
}
