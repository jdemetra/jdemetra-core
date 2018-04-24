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
package demetra.timeseries.calendars;

import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.DayClustering;
import demetra.data.DataBlock;
import demetra.maths.matrices.Matrix;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import ec.tstoolkit.timeseries.calendars.DefaultGregorianCalendarProvider;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysTest {

    public GenericTradingDaysTest() {
    }

    @Test
    public void testTD() {
        TsDomain md = TsDomain.of(TsPeriod.monthly(1980, 1), 360);
        Matrix M1 = Matrix.make(md.length(), 6);
        GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
        gtd.data(md, M1.columnList());
        ec.tstoolkit.timeseries.simplets.TsDomain omd = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 360);
        ec.tstoolkit.maths.matrices.Matrix oM1 = new ec.tstoolkit.maths.matrices.Matrix(omd.getLength(), 6);
        DefaultGregorianCalendarProvider.instance.calendarData(TradingDaysType.TradingDays, omd, oM1.columnList());
        for (int i = 0; i < 6; ++i) {
            assertTrue(distance(M1.column(i), oM1.column(i)) < 1e-9);
        }
    }

    @Test
    public void testWD() {
        TsDomain md = TsDomain.of(TsPeriod.monthly(1980, 1), 360);
        Matrix M1 = Matrix.make(md.length(), 1);
        GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD2);
        gtd.data(md, M1.columnList());
        ec.tstoolkit.timeseries.simplets.TsDomain omd = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 360);
        ec.tstoolkit.maths.matrices.Matrix oM1 = new ec.tstoolkit.maths.matrices.Matrix(omd.getLength(), 1);
        DefaultGregorianCalendarProvider.instance.calendarData(TradingDaysType.WorkingDays, omd, oM1.columnList());
        for (int i = 0; i < 1; ++i) {
            assertTrue(distance(M1.column(i), oM1.column(i)) < 1e-9);
        }
    }

    @Test
    @Ignore
    public void stressTestTD() {
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            TsDomain md = TsDomain.of(TsPeriod.monthly(1980, 1), 360);
            Matrix M1 = Matrix.make(md.length(), 6);
            GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
            gtd.data(md, M1.columnList());
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; ++i) {
            ec.tstoolkit.timeseries.simplets.TsDomain omd = new ec.tstoolkit.timeseries.simplets.TsDomain(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1980, 0, 360);
            ec.tstoolkit.maths.matrices.Matrix oM1 = new ec.tstoolkit.maths.matrices.Matrix(omd.getLength(), 6);
            DefaultGregorianCalendarProvider.instance.calendarData(TradingDaysType.TradingDays, omd, oM1.columnList());
        }
        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);

    }

    private double distance(DataBlock column, ec.tstoolkit.data.DataBlock column0) {
        return column.distance(
                DataBlock.ofInternal(column0.getData(), column0.getStartPosition(), column0.getEndPosition(), column0.getIncrement()));
    }

}
