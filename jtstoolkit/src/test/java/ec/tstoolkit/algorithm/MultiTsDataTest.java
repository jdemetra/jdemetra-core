/*
 * Copyright 2016 National Bank of Belgium
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
package ec.tstoolkit.algorithm;

import data.Data;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MultiTsDataTest {

    private static final MultiTsData data;
    private static final int N = 10;

    static {

        TsData[] x = Data.rndAirlines(N, 24, -.5, -.6).toArray(new TsData[0]);
        data = new MultiTsData("test", x);
    }

    public MultiTsDataTest() {
    }

    @Test
    public void testDictionary() {
        assertTrue(data.getDictionary(true).size() == 1);
        assertTrue(data.getDictionary(false).size() == N);
    }

    @Test
    public void testgetData() {
        Map<String, Class> dictionary = data.getDictionary(false);
        dictionary.forEach((s, c) -> assertTrue(data.getData(s, c) != null));
        Map<String, Class> cdictionary = data.getDictionary(true);
        cdictionary.forEach((s, c) -> assertTrue(data.searchAll(s, c).size() == N));
    }
}
