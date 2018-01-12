/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.r;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class LocalPolynomialFiltersTest {
    
    public LocalPolynomialFiltersTest() {
    }

    @Test
    public void testHenderson() {
        double[] rslt = LocalPolynomialFilters.filter(Data.NILE, 11, 3, "Henderson", "DAF", .5);
        assertTrue(rslt != null);
//        System.out.println(DoubleSequence.ofInternal(rslt));
    }
    
}
