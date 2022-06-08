/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.tsproviders.common.tsw;

import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.util.Arrays.asList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Philippe Charles
 */
public class TswFactoryTest {

    private static Path getResource(String name) throws URISyntaxException {
        return Paths.get(TswFactoryTest.class.getResource(name).toURI());
    }

    final TswFactory oldFactory = TswFactory.OldTswFactory.INSTANCE;
    final TswFactory newFactory = TswFactory.NewTswFactory.INSTANCE;

    @Test
    public void testEmptyFile() throws URISyntaxException, IOException {
        Path file = getResource("EmptyFile");
        for (TswFactory factory : asList(oldFactory, newFactory)) {
            List<TswSeries> items = factory.loadFile(file);
            assertTrue(items.isEmpty());
        }
    }

    @Test
    public void testMultiObsPerLine() throws URISyntaxException, IOException {
        Path file = getResource("MultiObsPerLine");
        for (TswFactory factory : asList(oldFactory, newFactory)) {
            List<TswSeries> items = factory.loadFile(file);
            assertEquals(1, items.size());

            TswSeries single = items.get(0);
            assertEquals("EXPORTS (Spain)", single.name);
            assertEquals(new TsDomain(TsFrequency.Monthly, 1976, 0, 155), single.data.get().getDomain());
        }
    }

    @Test
    public void testMultiTs() throws URISyntaxException, IOException {
        List<TswSeries> items = newFactory.loadFile(getResource("MultiTs"));
        assertEquals(16, items.size());

        TswSeries first = items.get(0);
        assertEquals("1    s0b44000.e2", first.name);
        assertEquals(192, first.data.get().getLength());
        assertEquals(TsFrequency.Monthly, first.data.get().getFrequency());

        TswSeries last = items.get(15);
        assertEquals("16   s0b72200.e2", last.name);
        assertEquals(192, last.data.get().getLength());
        assertEquals(TsFrequency.Monthly, last.data.get().getFrequency());
    }

    @Test
    public void testNanObs() throws URISyntaxException, IOException {
        Path file = getResource("NanObs");
        for (TswFactory factory : asList(oldFactory, newFactory)) {
            List<TswSeries> items = factory.loadFile(file);
            assertEquals(1, items.size());

            TswSeries single = items.get(0);
            assertEquals("ipiSPAIN", single.name);
            assertEquals(new TsDomain(TsFrequency.Monthly, 1983, 1, 158), single.data.get().getDomain());
            assertEquals(158 - 4, single.data.get().getObsCount());
            assertEquals(Double.NaN, single.data.get().get(4), 0);
        }
    }

    @Test
    public void testSingleTs() throws URISyntaxException, IOException {
        Path file = getResource("SingleTs");
        for (TswFactory factory : asList(oldFactory, newFactory)) {
            List<TswSeries> items = factory.loadFile(file);
            assertEquals(1, items.size());

            TswSeries single = items.get(0);
            assertEquals("Monetary Aggregate ALP (Spain)", single.name);
            assertEquals(new TsDomain(TsFrequency.Monthly, 1972, 0, 234), single.data.get().getDomain());
        }
    }

    @org.junit.Test(expected = IOException.class)
    public void testBinFile() throws URISyntaxException, IOException {
        Path file = getResource("blog_16x16.png");
        for (TswFactory factory : asList(oldFactory, newFactory)) {
            factory.loadFile(file);
        }
    }

    @Test
    public void testEmptyLines() throws URISyntaxException, IOException {
        Path file = getResource("EmptyLines");
        for (TswFactory factory : asList(oldFactory, newFactory)) {
            List<TswSeries> items = factory.loadFile(file);
            assertEquals(1, items.size());
        }
    }
}
