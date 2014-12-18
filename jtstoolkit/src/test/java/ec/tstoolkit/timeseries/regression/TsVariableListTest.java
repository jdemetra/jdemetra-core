/*
* Copyright 2013 National Bank of Belgium
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
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class TsVariableListTest {
    
    public TsVariableListTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() {
        TsVariableList X=new TsVariableList();
        
        GregorianCalendarVariables td=GregorianCalendarVariables.getDefault(TradingDaysType.TradingDays);
        X.add(td);
        X.add(new LeapYearVariable(LengthOfPeriodType.LeapYear));
        X.add(new ChangeOfRegime(td, ChangeOfRegimeType.ZeroStarted, new Day(2000, Month.January, 0)));
        X.add(new SeasonalDummies(TsFrequency.Monthly));
        
        assertTrue(X.getVariablesCount() == 6+1+6+11);
        // Gets all the variables for 40 years
        Matrix matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 1980, 0, 480));
        // The matrix is full rank
        assertTrue(matrix.rank() == X.getVariablesCount());
        // Gets all the variables since 1/1/2000
        matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 2000, 0, 240));
        // The matrix is rank-deficient (the TD and the TD with change of regime are identical)
        assertTrue(matrix.rank() == X.getVariablesCount()-6);
        // TD, TD with change of regime, LP (or 6 + 6 + 1)
        assertTrue(X.selectCompatible(ICalendarVariable.class).getVariablesCount() == 13);
        // TD only
        assertTrue(X.select(ITradingDaysVariable.class).getVariablesCount() == 6);
        // description of the regression variables
//        for (ITsVariable var : X.items()){
//            System.out.println(var.getDescription());
//            if (var.getDim() > 1)
//                for (int j=0; j<var.getDim(); ++j)
//             System.out.println("    "+var.getItemDescription(j));
//       }
    }
    
}
