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

package ec.satoolkit.tramoseats;

import ec.tstoolkit.information.InformationSet;
import static ec.tstoolkit.modelling.arima.PreprocessingModel.dictionary;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author pcuser
 */
public class TramoSeatsSpecificationTest {
    
    public TramoSeatsSpecificationTest() {
    }

    @Test
    public void testInformation() {
        TramoSeatsSpecification spec = new TramoSeatsSpecification();
        InformationSet info = spec.write(true);
        TramoSeatsSpecification nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(spec, nspec);
        info = TramoSeatsSpecification.RSA0.write(true);
        nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(TramoSeatsSpecification.RSA0, nspec);
        info = TramoSeatsSpecification.RSA1.write(true);
        nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(TramoSeatsSpecification.RSA1, nspec);
        info = TramoSeatsSpecification.RSA2.write(true);
        nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(TramoSeatsSpecification.RSA2, nspec);
        info = TramoSeatsSpecification.RSA3.write(true);
        nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(TramoSeatsSpecification.RSA3, nspec);
        info = TramoSeatsSpecification.RSA4.write(true);
        nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(TramoSeatsSpecification.RSA4, nspec);
        info = TramoSeatsSpecification.RSA5.write(true);
        nspec = new TramoSeatsSpecification();
        nspec.read(info);
        assertEquals(TramoSeatsSpecification.RSA5, nspec);
    }
    
//    @Test
//    public void testUpdate(){
//        InformationSet info = TramoSeatsSpecification.RSA5.write(false);
//        Map<String, Class> dic=new LinkedHashMap<>();
//        info.fillDictionary(null, dic);
//        for (Entry<String, Class> entry : dic.entrySet()) {
//            System.out.print(entry.getKey());
//            System.out.print('\t');
//            System.out.println(info.search(entry.getKey(), entry.getValue()));
//            System.out.print('\t');
//            System.out.println(entry.getValue().getCanonicalName());
//        }
//        
//        info.set(InformationSet.split("tramo.regression.calendar.td.auto"), true);
//        
//        TramoSeatsSpecification nspec=new TramoSeatsSpecification();
//        nspec.read(info);
//        
//    }

//    @Test
//    public void testDictionay() {
//        TramoSeatsSpecification spec = new TramoSeatsSpecification();
//        ArrayList<String> dic = new ArrayList<String>();
//        spec.fillDictionary(null, dic);
//        for (String s : dic) {
//            System.out.println(s);
//        }
//    }
}
