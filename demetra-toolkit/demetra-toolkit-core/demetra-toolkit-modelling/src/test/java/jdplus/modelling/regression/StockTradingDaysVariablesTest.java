/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import demetra.timeseries.regression.StockTradingDays;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class StockTradingDaysVariablesTest {

    public StockTradingDaysVariablesTest() {
    }

    @Test
    public void testMonthly() {
        for (int i = 2; i < 120; ++i) {
            ec.tstoolkit.timeseries.regression.StockTradingDaysVariables ovar = new ec.tstoolkit.timeseries.regression.StockTradingDaysVariables(-3);
            ec.tstoolkit.timeseries.simplets.TsDomain odomain = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 2000, 0, i);
            ec.tstoolkit.maths.matrices.Matrix om = new ec.tstoolkit.maths.matrices.Matrix(i, 6);
            ovar.data(odomain, om.columnList());

            StockTradingDays var = new StockTradingDays(-3);
            TsPeriod start = TsPeriod.monthly(2000, 1);
            FastMatrix m = Regression.matrix(TsDomain.of(start, i), var);
            FastMatrix mc = FastMatrix.builder(om.internalStorage()).nrows(i).ncolumns(6).build();
            assertTrue(m.minus(mc).isZero(1e-9));
        }
    }

    @Test
    public void testQuarterly() {
        for (int i = 2; i < 40; ++i) {
            ec.tstoolkit.timeseries.regression.StockTradingDaysVariables ovar = new ec.tstoolkit.timeseries.regression.StockTradingDaysVariables(-3);
            ec.tstoolkit.timeseries.simplets.TsDomain odomain = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 2000, 0, i);
            ec.tstoolkit.maths.matrices.Matrix om = new ec.tstoolkit.maths.matrices.Matrix(i, 6);
            ovar.data(odomain, om.columnList());

            StockTradingDays var = new StockTradingDays(-3);
            TsPeriod start = TsPeriod.quarterly(2000, 1);
            FastMatrix m = Regression.matrix(TsDomain.of(start, i), var);
            FastMatrix mc = FastMatrix.builder(om.internalStorage()).nrows(i).ncolumns(6).build();
            assertTrue(m.minus(mc).isZero(1e-9));
        }
    }

    @Test
    public void testMonthly2() {
        for (int i = 2; i < 120; ++i) {
            ec.tstoolkit.timeseries.regression.StockTradingDaysVariables ovar = new ec.tstoolkit.timeseries.regression.StockTradingDaysVariables(17);
            ec.tstoolkit.timeseries.simplets.TsDomain odomain = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 2000, 0, i);
            ec.tstoolkit.maths.matrices.Matrix om = new ec.tstoolkit.maths.matrices.Matrix(i, 6);
            ovar.data(odomain, om.columnList());

            StockTradingDays var = new StockTradingDays(17);
            TsPeriod start = TsPeriod.monthly(2000, 1);
            FastMatrix m = Regression.matrix(TsDomain.of(start, i), var);
            FastMatrix mc = FastMatrix.builder(om.internalStorage()).nrows(i).ncolumns(6).build();
            assertTrue(m.minus(mc).isZero(1e-9));
        }
    }

    @Test
    public void testQuarterly2() {
        for (int i = 2; i < 40; ++i) {
            ec.tstoolkit.timeseries.regression.StockTradingDaysVariables ovar = new ec.tstoolkit.timeseries.regression.StockTradingDaysVariables(30);
            ec.tstoolkit.timeseries.simplets.TsDomain odomain = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 2000, 0, i);
            ec.tstoolkit.maths.matrices.Matrix om = new ec.tstoolkit.maths.matrices.Matrix(i, 6);
            ovar.data(odomain, om.columnList());

            StockTradingDays var = new StockTradingDays(30);
            TsPeriod start = TsPeriod.quarterly(2000, 1);
            FastMatrix m = Regression.matrix(TsDomain.of(start, i), var);
            FastMatrix mc = FastMatrix.builder(om.internalStorage()).nrows(i).ncolumns(6).build();
            assertTrue(m.minus(mc).isZero(1e-9));
        }
    }
}
