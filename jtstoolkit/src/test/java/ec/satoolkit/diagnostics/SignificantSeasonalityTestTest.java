/*
 * Copyright 2015 National Bank of Belgium
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
/*
 */
package ec.satoolkit.diagnostics;

import data.Data;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PCUser
 */
public class SignificantSeasonalityTestTest {
    
    public SignificantSeasonalityTestTest() {
    }

    @Test
    public void testSomeMethod() {
        CompositeResults process = TramoSeatsProcessingFactory.process(Data.M1, TramoSeatsSpecification.RSAfull);
        int[] nsig=SignificantSeasonalityTest.test(process, .01);
        System.out.println(".01");
        for (int i=0; i<nsig.length; ++i){
 //           System.out.println(nsig[i]);
        }
        int[] nsig1=SignificantSeasonalityTest.test(process, .05);
        System.out.println(".05");
        for (int i=0; i<nsig.length; ++i){
            assertTrue(nsig1[i]>=nsig[i]);
//            System.out.println(nsig1[i]);
        }
        process = TramoSeatsProcessingFactory.process(process.getData("sa", TsData.class), TramoSeatsSpecification.RSA1);
        nsig=SignificantSeasonalityTest.test(process, .01);
        System.out.println(".01");
        for (int i=0; i<nsig.length; ++i){
            assertTrue(nsig[i]<2);
//            System.out.println(nsig[i]);
        }
        nsig1=SignificantSeasonalityTest.test(process, .05);
        System.out.println(".05");
        for (int i=0; i<nsig.length; ++i){
//            assertTrue(nsig1[i]<2);
            System.out.println(nsig1[i]);
        }
   }
    
}
