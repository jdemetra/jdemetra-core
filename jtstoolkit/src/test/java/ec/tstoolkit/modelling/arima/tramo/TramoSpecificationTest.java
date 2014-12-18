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

package ec.tstoolkit.modelling.arima.tramo;

import ec.tstoolkit.information.InformationSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class TramoSpecificationTest {

    @Test
    public void testInformation() {
        TramoSpecification spec = new TramoSpecification();
        InformationSet info = spec.write(true);
        TramoSpecification nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(spec, nspec);
        info = TramoSpecification.TR0.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR0, nspec);
        info = TramoSpecification.TR1.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR1, nspec);
        info = TramoSpecification.TR2.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR2, nspec);
        info = TramoSpecification.TR3.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR3, nspec);
        info = TramoSpecification.TR4.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR4, nspec);
        info = TramoSpecification.TR5.write(true);
        nspec = new TramoSpecification();
        nspec.read(info);
        assertEquals(TramoSpecification.TR5, nspec);
    }

//    @Test
//    public void testDictionay() {
//        TramoSpecification spec = new TramoSpecification();
//        ArrayList<String> dic = new ArrayList<String>();
//        spec.fillDictionary(null, dic);
//        for (String s : dic) {
//            System.out.println(s);
//        }
//    }
}
