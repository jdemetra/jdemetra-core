/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.modelling.regression;

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import static org.junit.Assert.*;
import jdplus.maths.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysVariableTest {

    public GenericTradingDaysVariableTest() {
    }

    @Test
    public void testTD6() {
        ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovars
                = ec.tstoolkit.timeseries.regression.GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        ec.tstoolkit.maths.matrices.Matrix om = new ec.tstoolkit.maths.matrices.Matrix(360, 6);
        ec.tstoolkit.timeseries.simplets.TsDomain odom
                = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 4, 360);
        ovars.data(odom, om.columnList());

        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD7);
        TsDomain dom = TsDomain.of(TsPeriod.monthly(1980, 5), 360);
        GenericTradingDaysVariable vars = new GenericTradingDaysVariable(td.getClustering(), td.isContrast(), td.isNormalized());
        FastMatrix m = Regression.matrix(dom, vars);
        for (int r = 0; r < m.getRowsCount(); ++r) {
            for (int c = 0; c < m.getColumnsCount(); ++c) {
                assertEquals(m.get(r, c), om.get(r, c), 1e-9);
            }
        }
    }

    @Test
    public void testTD1() {
        ec.tstoolkit.timeseries.regression.GregorianCalendarVariables ovars
                = ec.tstoolkit.timeseries.regression.GregorianCalendarVariables.getDefault(TradingDaysType.WorkingDays);
        ec.tstoolkit.maths.matrices.Matrix om = new ec.tstoolkit.maths.matrices.Matrix(360, 1);
        ec.tstoolkit.timeseries.simplets.TsDomain odom
                = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly, 1980, 2, 360);
        ovars.data(odom, om.columnList());

        GenericTradingDays td = GenericTradingDays.contrasts(DayClustering.TD2);
        TsDomain dom = TsDomain.of(TsPeriod.quarterly(1980, 3), 360);
        GenericTradingDaysVariable vars = new GenericTradingDaysVariable(td.getClustering(), td.isContrast(), td.isNormalized());
        FastMatrix m = Regression.matrix(dom, vars);
        for (int r = 0; r < m.getRowsCount(); ++r) {
            assertEquals(m.get(r, 0), om.get(r, 0), 1e-9);
        }
    }
}
