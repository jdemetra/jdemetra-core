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

package ec.tss.sa.output;

import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.processors.TramoSeatsProcessor;
import ec.tss.sa.processors.X13Processor;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class BasicConfigurationTest {
    
    private static final List<ISaProcessingFactory> facs;
    
    static {
        facs=new ArrayList<>();
        facs.add(new TramoSeatsProcessor());
        facs.add(new X13Processor());
    }
    
    public BasicConfigurationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    @Ignore
    public void testAllSeries() {
        for (String s : BasicConfiguration.allSeries(true, facs))
            System.out.println(s);
    }

    @Test
    @Ignore
    public void testAllCompactDettails() {
        for (String s : BasicConfiguration.allDetails(true, facs))
            System.out.println(s);
    }
    
    @Test
    @Ignore
    public void testAllDettails() {
        for (String s : BasicConfiguration.allDetails(false, facs))
            System.out.println(s);
    }
    
}
