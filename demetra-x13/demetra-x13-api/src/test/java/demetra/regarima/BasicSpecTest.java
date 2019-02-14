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

import demetra.timeseries.TimeSelector;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Mats Maggi
 */
public class BasicSpecTest {
    @Test
    public void testDefault() {
        assertTrue(BasicSpec.builder()
                .span(TimeSelector.all())
                .preProcessing(true)
                .build().isDefault());
    }
    
    @Test(expected = NullPointerException.class)
    public void testSpanNonNull() {
        BasicSpec.builder()
                .span(null)
                .build();
    }
    
    @Test
    public void testNotDefault() {
        assertFalse(BasicSpec.builder()
                .span(TimeSelector.first(12))
                .build().isDefault());
    }
}
