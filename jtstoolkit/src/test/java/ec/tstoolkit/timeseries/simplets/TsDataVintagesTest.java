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

package ec.tstoolkit.timeseries.simplets;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.data.DescriptiveStatistics;
import java.util.SortedSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author pcuser
 */
public class TsDataVintagesTest {
    
    public TsDataVintagesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //@Test
    public void test() {
        TsDataVintages<TsPeriod> vintages=new TsDataVintages<>();
        int nback=60;
        for (int i=0; i<=nback; ++i){
            CompositeResults rslt = TramoSeatsProcessingFactory.process(Data.P.drop(0, nback-i), TramoSeatsSpecification.RSA5);
            TsData sa=rslt.getData("sa", TsData.class);
            vintages.add(sa, sa.getLastPeriod());
        }
        
        SortedSet<TsPeriod> allVintages = vintages.allVintages();
        System.out.print(vintages.toMatrix(allVintages, true).getMatrix());
        TsData last=vintages.current();
        for (TsPeriod p : allVintages){
            System.out.print(p);
            System.out.print('\t');
            double[] dataVintages = vintages.dataVintages(p);
            DescriptiveStatistics stats=new DescriptiveStatistics(dataVintages);
            System.out.print(stats.getObservationsCount());
            System.out.print('\t');
            System.out.print(stats.getAverage());
            System.out.print('\t');
            System.out.print(stats.getStdev());
            System.out.print('\t');
            System.out.println(vintages.data(p, true).distance(last));
        }
        
        
    }
}
