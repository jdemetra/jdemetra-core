/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.data.DoubleSeq;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.ICalendarVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.PeriodicContrasts;
import demetra.timeseries.regression.modelling.RegressionVariables;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import java.util.ArrayList;
import java.util.List;
import jdplus.data.DataBlock;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class RegressionVariablesTest {

    public RegressionVariablesTest() {
    }

    @Test
    public void testData() {
        List<ITsVariable> vars = new ArrayList<>();
        PeriodicContrasts pc = new PeriodicContrasts(12);
        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        GenericTradingDaysVariable vtd = new GenericTradingDaysVariable(td.getClustering(), td.isContrast(), td.isNormalized());
        vars.add(pc);
        vars.add(vtd);

        TsDomain domain = TsDomain.of(TsPeriod.monthly(2000, 1), 200);
        MatrixType matrix = RegressionVariables.matrix(domain, vars);
        System.out.println(matrix);
        DataBlock c = DataBlock.make(matrix.getColumnsCount());
        c.set(i -> i + 1);
        DoubleSeq e = RegressionVariables.linearEffect(domain, vars, c, var -> var instanceof ICalendarVariable);
    }
}
