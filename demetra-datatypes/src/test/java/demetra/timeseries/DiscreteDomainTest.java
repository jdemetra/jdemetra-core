/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.timeseries;

import java.time.LocalDateTime;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DiscreteDomainTest {

    @Test
    public void testSearch() {
        LocalDateTime[] d = new LocalDateTime[10];
        d[0] = LocalDateTime.now();
        for (int i = 1; i < d.length; ++i) {
            d[i] = d[i - 1].plusSeconds(i);
        }
        DiscreteDomain dd = DiscreteDomain.of(d);
        for (int i = 0; i < dd.length(); ++i) {
            assertTrue(dd.search(dd.elementAt(i).start()) == i);
        }
        assertTrue(dd.search(d[0].minusSeconds(1)) == -1);
        assertTrue(dd.search(d[d.length - 1].minusSeconds(1)) == -dd.length());
    }

}
