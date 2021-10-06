/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.math.linearfilters;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class SymmetricFiltersFactoryTest {
    
    public SymmetricFiltersFactoryTest() {
    }

    @Test
    public void testEven() {
        SymmetricFilter f1 = SymmetricFiltersFactory.makeSymmetricFilter(12);
        SymmetricFilter f2 = SymmetricFiltersFactory.makeSymmetricFilter(12, 2);
        assertTrue(IFiniteFilter.equals(f1, f2, 1e-9));
    }
    
    @Test
    public void testOdd() {
        SymmetricFilter f = SymmetricFiltersFactory.makeSymmetricFilter(7);
        assertEquals(f.weights().applyAsDouble(3), f.weights().applyAsDouble(0), 0);
        assertEquals(f.weights().applyAsDouble(-3), f.weights().applyAsDouble(0), 0);
    }
}
