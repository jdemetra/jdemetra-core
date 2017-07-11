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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsVariableWindowTest {

    public TsVariableWindowTest() {
    }

    @Test
    public void testTD() {
        Matrix M1 = new Matrix(251, 6);
        Matrix M2 = new Matrix(251, 6);
        TsDomain all = new TsDomain(TsFrequency.Monthly, 1975, 7, 251);
        GregorianCalendarVariables var = GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        var.data(all, M1.columnList());
//        System.out.println("all");
//        System.out.println(M1);
        TsVariableWindow wnd1 = new TsVariableWindow(var, new Day(1970, Month.January, 0), new Day(1982, Month.April, 29));
        TsVariableWindow wnd2 = new TsVariableWindow(var, new Day(1982, Month.May, 0), Day.toDay());
        wnd1.data(all, M2.columnList());
        assertTrue(M1.distance(M2) > 1e-9);
//        System.out.println("wnd1");
//        System.out.println(M2);
        wnd2.data(all, M2.columnList());
//        System.out.println("wnd2");
//        System.out.println(M2);
        assertTrue(M1.distance(M2) < 1e-9);
    }

}
