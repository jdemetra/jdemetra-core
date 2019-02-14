/*
 * Copyright 2019 National Bank of Belgium
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
package demetra.tramo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class EasterSpecTest {

    @Test
    public void testClone() {
        EasterSpec spec = EasterSpec.builder().build();
        EasterSpec cspec = spec.toBuilder().build();
        assertTrue(spec.equals(cspec));
        assertTrue(cspec.isDefault());
        spec = spec.toBuilder().type(EasterSpec.Type.IncludeEaster).build();
        cspec = spec.toBuilder().build();
        assertTrue(spec.equals(cspec));
        assertFalse(cspec.isDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidation() {
        EasterSpec.builder()
                .duration(0)
                .build();
    }

}
