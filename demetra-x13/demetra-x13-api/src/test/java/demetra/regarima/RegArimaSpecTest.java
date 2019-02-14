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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Mats Maggi
 */
public class RegArimaSpecTest {
    
    @Test
    public void testCloneDefaults() {
        assertEquals(RegArimaSpec.RG0, RegArimaSpec.RG0.toBuilder().build());
        assertEquals(RegArimaSpec.RG1, RegArimaSpec.RG1.toBuilder().build());
        assertEquals(RegArimaSpec.RG2, RegArimaSpec.RG2.toBuilder().build());
        assertEquals(RegArimaSpec.RG3, RegArimaSpec.RG3.toBuilder().build());
        assertEquals(RegArimaSpec.RG4, RegArimaSpec.RG4.toBuilder().build());
        assertEquals(RegArimaSpec.RG5, RegArimaSpec.RG5.toBuilder().build());
    }
}
