/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.tstoolkit.timeseries.calendars;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.DayClustering;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class GenericTradingDaysTest {

    public GenericTradingDaysTest() {
    }

    @Test
    public void testTD() {
        TsDomain md = new TsDomain(TsFrequency.Monthly, 1980, 0, 360);
        Matrix M1 = new Matrix(md.getLength(), 6);
        DefaultGregorianCalendarProvider.instance.calendarData(TradingDaysType.TradingDays, md, M1.columnList());

        Matrix M2 = new Matrix(md.getLength(), 6);
        GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD7);
        gtd.data(md, M2.columnList());

        for (int i = 0; i < 6; ++i) {
            assertTrue(M1.column(i).distance(M2.column(i)) < 1e-9);
        }
    }

    @Test
    public void testWD() {
        TsDomain md = new TsDomain(TsFrequency.Monthly, 1980, 0, 360);
        Matrix M1 = new Matrix(md.getLength(), 1);
        DefaultGregorianCalendarProvider.instance.calendarData(TradingDaysType.WorkingDays, md, M1.columnList());

        Matrix M2 = new Matrix(md.getLength(), 1);
        GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD2);
        gtd.data(md, M2.columnList());
        assertTrue(M1.column(0).distance(M2.column(0)) < 1e-9);
    }
    
    @Test
    //@Ignore
    public void testDisplay() {
        TsDomain md = new TsDomain(TsFrequency.Monthly, 1980, 0, 28*12);
        Matrix M = new Matrix(md.getLength(), 1);
        GenericTradingDays gtd = GenericTradingDays.contrasts(DayClustering.TD2);
        gtd.data(md, M.columnList());
        System.out.println(M);
        M = new Matrix(md.getLength(), 2);
        gtd = GenericTradingDays.contrasts(DayClustering.TD3);
        gtd.data(md, M.columnList());
        System.out.println(M);
        M = new Matrix(md.getLength(), 6);
        gtd = GenericTradingDays.contrasts(DayClustering.TD7);
        gtd.data(md, M.columnList());
        System.out.println(M);
    }
    
    @Test
    //@Ignore
    public void testDisplayNoContrasts() {
        TsDomain md = new TsDomain(TsFrequency.Monthly, 1980, 0, 28*12);
        Matrix M = new Matrix(md.getLength(), 2);
        GenericTradingDays gtd = GenericTradingDays.of(DayClustering.TD2);
        gtd.data(md, M.columnList());
        System.out.println(M);
        M = new Matrix(md.getLength(), 3);
        gtd = GenericTradingDays.of(DayClustering.TD3);
        gtd.data(md, M.columnList());
        System.out.println(M);
        M = new Matrix(md.getLength(), 7);
        gtd = GenericTradingDays.of(DayClustering.TD7);
        gtd.data(md, M.columnList());
        System.out.println(M);
    }
}
