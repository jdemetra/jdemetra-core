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

package ec.tstoolkit.maths;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ComplexTest {
    
    @Test
    public void testInv() {
        Complex complex = Complex.cart(54.654, 7.321);
        Assert.assertEquals(complex.inv(), new ComplexBuilder(complex).inv().build());
    }
    
    @Test
    public void testDiv() {
        Complex c1 = Complex.cart(54.654, 7.321);
        Complex c2 = Complex.cart(77, 66.12);
        Assert.assertEquals(c1.div(c2), new ComplexBuilder(c1).div(c2).build());
    }

 }
