/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.io;

import com.google.common.collect.Iterators;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class IteratorWithIOTest {

    @Test
    public void testMap() throws IOException {
        // Integer -> String -> Double
        try (IteratorWithIO<Double> i = getSample().map(String::valueOf).map(Double::valueOf)) {
            while (i.hasNext()) {
                assertThat(i.next()).isInstanceOf(Double.class);
            }
        }
    }

    @Test
    public void testForEachRemaining() throws IOException {
        try (IteratorWithIO<Double> i = getSample().map(String::valueOf).map(Double::valueOf)) {
            i.forEachRemaining(o -> assertThat(o).isInstanceOf(Double.class));
        }
    }

    private IteratorWithIO<Integer> getSample() {
        return IteratorWithIO.checked(Iterators.forArray(1, 2, 3));
    }
}
