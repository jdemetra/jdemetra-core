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

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class EasterVariableTest {
    
    public EasterVariableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void testJulianEaster() {
        TsVariableList X=new TsVariableList();
        X.add(new EasterVariable());
        X.add(new JulianEasterVariable());
        Matrix matrix = X.all().matrix(new TsDomain(TsFrequency.Monthly, 1900, 0, 2400));
        // Prints the monthly regression variables for years from 1900 to 2100
        System.out.println(matrix);
    }
}

class JulianEasterVariable extends AbstractSingleTsVariable{

    @Override
    public void data(TsPeriod start, DataBlock data) {
        // inefficient algorithm
        
        // Create first a series initialized at 0
        TsData var=new TsData(new TsDomain(start, data.getLength()), 0);
       
        YearIterator iter=new YearIterator(var);
        while (iter.hasMoreElements()){
            TsDataBlock cur=iter.nextElement();
            Day julianEaster = ec.tstoolkit.timeseries.calendars.Utilities.julianEaster(cur.start.getYear(), true);
            // Creates the period that contains Easter
            TsPeriod p=new TsPeriod(start.getFrequency(), julianEaster);
            // search its position in this data block.
            int pos=p.minus(cur.start);
            // the series is NOT CORRECTED for long term mean effect.
            if (pos >= 0 && pos <cur.data.getLength()){
                cur.data.set(pos, 1);
            }
        }
        data.copy(var);
    }

    @Override
    public String getDescription() {
        return "Julian Easter";
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        // Julian Easter (in Gregorian dates falls in April or in May) 
        return domain.getFrequency() == TsFrequency.Monthly || domain.getFrequency() == TsFrequency.QuadriMonthly;
    }
 }
