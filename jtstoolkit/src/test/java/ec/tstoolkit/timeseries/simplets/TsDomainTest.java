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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsDomainTest {

    public TsDomainTest() {
    }

    @Test
    public void testChangeFreq() {
        TsPeriod lstart = new TsPeriod(TsFrequency.Quarterly, 1980, 0),
                hstart = new TsPeriod(TsFrequency.Monthly, 1980, 2);
        TsDomain ldom = new TsDomain(lstart, 4);
        TsDomain hdom = new TsDomain(hstart, 16);
        TsDomain hdomc = ldom.changeFrequency(hdom.getFrequency(), true);
        TsDomain ldomc = hdom.changeFrequency(ldom.getFrequency(), true);
//        System.out.println(ldom);
//        System.out.println(hdomc);
//        System.out.println(hdom);
//        System.out.println(ldomc);
        assertTrue(hdom.contains(ldomc.changeFrequency(hdom.getFrequency(), true)));
        assertTrue(ldom.contains(hdomc.changeFrequency(ldom.getFrequency(), true)));
    }

}
