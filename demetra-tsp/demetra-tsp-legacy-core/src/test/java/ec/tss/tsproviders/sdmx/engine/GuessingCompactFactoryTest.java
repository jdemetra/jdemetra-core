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
package ec.tss.tsproviders.sdmx.engine;

import static ec.tss.tsproviders.sdmx.engine.Utils.load;
import ec.tss.tsproviders.sdmx.model.SdmxSeries;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class GuessingCompactFactoryTest {

    @Test
    public void testIsValid() throws Exception {
        GuessingCompactFactory factory = new GuessingCompactFactory();
        assertFalse(factory.isValid(load("/sdmx-generic-sample.xml")));
        assertTrue(factory.isValid(load("/sdmx-compact-sample.xml")));
    }

    @Test
    public void testCreate() throws Exception {
        SdmxSource source = new GuessingCompactFactory().create(load("/sdmx-compact-sample.xml"));
        assertEquals(4, source.items.size());

        SdmxSeries s0 = (SdmxSeries) source.items.get(0);
        assertEquals("COLLECTION=B, FREQ=M, JD_CATEGORY=A, JD_TYPE=P, VIS_CTY=MX", s0.id);
        assertEquals(12, s0.data.get().getObsCount());
        assertEquals(TsFrequency.Monthly, s0.data.get().getFrequency());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 2000, 0), s0.data.get().getStart());
        assertEquals(new TsPeriod(TsFrequency.Monthly, 2000, 11), s0.data.get().getLastPeriod());
        assertArrayEquals(new double[]{3.14, 2.29, 3.14, 5.24, 3.14, 3.78, 3.65, 2.37, 3.14, 3.17, 3.34, 1.21}, s0.data.get().internalStorage(), 0);

        SdmxSeries s1 = (SdmxSeries) source.items.get(1);
        assertEquals("COLLECTION=B, FREQ=A, JD_CATEGORY=A, JD_TYPE=P, VIS_CTY=MX", s1.id);
        assertEquals(1, s1.data.get().getObsCount());
        assertEquals(TsFrequency.Yearly, s1.data.get().getFrequency());
        assertEquals(new TsPeriod(TsFrequency.Yearly, 2000, 0), s1.data.get().getStart());
        assertEquals(s1.data.get().getStart(), s1.data.get().getLastPeriod());
        assertArrayEquals(new double[]{3.14}, s1.data.get().internalStorage(), 0);
    }
}
