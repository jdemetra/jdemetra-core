/*
 * Copyright 2019 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.regarima;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Mats Maggi
 */
public class OutlierSpecTest {

    @Test
    public void testDefaultCriticalValue() {
        OutlierSpec spec = OutlierSpec.builder()
                .build();
        assertTrue(spec.getDefaultCriticalValue() == 0);
        spec = spec.toBuilder()
                .type(new SingleOutlierSpec("AO", 2))
                .build();
        
        assertNotNull(spec.getTypes());
        assertTrue(spec.getTypes().size() == 1);
        assertTrue(spec.getTypes().get(0).getCriticalValue() == 2);
        assertTrue("AO".equals(spec.getTypes().get(0).getType()));

        spec = spec.toBuilder()
                .defaultCriticalValue(1.5)
                .build();
        
        assertTrue(spec.getDefaultCriticalValue() == 1.5);
        assertTrue(spec.getTypes().get(0).getCriticalValue() == 1.5);
    }
}
