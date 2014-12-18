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

package ec.satoolkit.x13;

import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class X13SpecificationTest {
    
    public X13SpecificationTest() {
    }

    @Test
    public void testInformation() {
        X13Specification spec = new X13Specification();
        InformationSet info = spec.write(true);
        X13Specification nspec = new X13Specification();
        nspec.read(info);
        assertEquals(spec, nspec);
        info = X13Specification.RSAX11.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSAX11, nspec);
        info = X13Specification.RSA0.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSA0, nspec);
        info = X13Specification.RSA1.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSA1, nspec);
        info = X13Specification.RSA2.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSA2, nspec);
        info = X13Specification.RSA3.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSA3, nspec);
        info = X13Specification.RSA4.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSA4, nspec);
        info = X13Specification.RSA5.write(true);
        nspec = new X13Specification();
        nspec.read(info);
        assertEquals(X13Specification.RSA5, nspec);
    }

//    @Test
    public void demoDictionay() {
        X13Specification spec = new X13Specification();
        InformationSet write = spec.write(true);
        for (String s : write.getDictionary()) {
            System.out.println(s);
        }
    }
}
