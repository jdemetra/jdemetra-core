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

package ec.satoolkit.diagnostics;

import data.Data;
import ec.tstoolkit.stats.Anova;
import ec.tstoolkit.stats.Anova.Row;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
public class IBTestTest {

    public IBTestTest() {
    }

    //@Test
    public void testSomeMethod() {
        TsData m1 = Data.M1.fullYears();
        TsData m2 = Data.M2;
        TsData m3 = Data.M3;

        IBTest ibTest = new IBTest();
        ibTest.setLambda(1600);
        ArrayList<TsData> s = new ArrayList<>();
        s.add(m1);
        s.add(m2);
        s.add(m3);
        ibTest.process(s);
//        StatisticalTest[] anova = ibTest.anova();
//        for (int i = 0; i < anova.length; ++i) {
//            System.out.print(anova[i].getValue());
//            System.out.print('\t');
//            System.out.print(anova[i].getPValue());
//            System.out.print('\t');
//            System.out.print(anova[i].getDistribution());
//            System.out.println();
//        }
        
        Anova test=ibTest.anova();
        List<Row> rows = test.getRows();
        for (Row row : rows){
            System.out.print(row.df);
            System.out.print('\t');
            System.out.print(row.ssq);
            System.out.print('\t');
            System.out.print(row.mssq());
            System.out.print('\t');
            System.out.print(row.ftest().getValue());
            System.out.print('\t');
            System.out.print(row.ftest().getPValue());
            System.out.print('\t');
            System.out.print(row.ftest().getDistribution());
            System.out.println();
           
        }
        TsDataTable q=new TsDataTable();
        q.insert(-1, m1);
        q.insert(-1, m2);
        q.insert(-1, m3);
            System.out.print(q);
    }
}
