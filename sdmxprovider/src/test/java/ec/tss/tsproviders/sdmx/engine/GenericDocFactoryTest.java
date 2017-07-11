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
import ec.tss.tsproviders.sdmx.model.SdmxGroup;
import ec.tss.tsproviders.sdmx.model.SdmxSeries;
import ec.tss.tsproviders.sdmx.model.SdmxSource;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class GenericDocFactoryTest {

    @Test
    public void testIsValid() throws Exception {
        GenericDocFactory factory = new GenericDocFactory();
        assertTrue(factory.isValid(load("/sdmx-generic-sample.xml")));
        assertFalse(factory.isValid(load("/sdmx-compact-sample.xml")));
    }

    @Test
    public void testCreate() throws Exception {
        SdmxSource source = new GenericDocFactory().create(load("/sdmx-generic-sample.xml"));
        assertEquals(1, source.items.size());

        SdmxGroup group;
        SdmxSeries series;

        group = (SdmxGroup) source.items.get(0);
        assertEquals("JD_TYPE=P, JD_CATEGORY=A, VIS_CTY=MX", group.id);
        assertEquals(2, group.series.size());

        series = group.series.get(0);
        assertEquals(TimeFormat.P1M, series.timeFormat);
        assertEquals("FREQ=M", series.id);
        assertEquals(new TsDomain(TsFrequency.Monthly, 2000, 0, 12), series.data.get().getDomain());
        assertArrayEquals(new double[]{3.14, 3.14, 4.29, 6.04, 5.18, 5.07, 3.13, 1.17, 1.14, 3.04, 1.14, 3.24}, series.data.get().internalStorage(), 0);

        series = group.series.get(1);
        assertEquals(TimeFormat.P1Y, series.timeFormat);
        assertEquals("FREQ=A", series.id);
        assertEquals(new TsDomain(TsFrequency.Yearly, 2000, 0, 1), series.data.get().getDomain());
        assertArrayEquals(new double[]{3.14}, series.data.get().internalStorage(), 0);
    }

    @Test
    public void testGenericP1Y() throws Exception {
        SdmxSource source = new GenericDocFactory().create(load("generic-P1Y.xml"));
        assertEquals(3, source.items.size());

        SdmxSeries series;
        TsData data;

        series = (SdmxSeries) source.items.get(0);
        assertEquals(TimeFormat.P1Y, series.timeFormat);
        assertEquals("NADET2008_INDICATOR=B1G, NADET2008_BRANCH=A38_AA, NADET2008_SECTOR=S1, NADET2008_PRICE=L, FREQUENCY=A", series.id);
        data = series.data.get();
        assertEquals(new TsDomain(TsFrequency.Yearly, 2009, 0, 5), data.getDomain());
        assertArrayEquals(new double[]{2722.8, 2911.5, 2836.2, 2708.5, 2797.7}, data.internalStorage(), 0);

        series = (SdmxSeries) source.items.get(2);
        assertEquals(TimeFormat.P1Y, series.timeFormat);
        assertEquals("NADET2008_INDICATOR=B1G, NADET2008_BRANCH=A38_AA, NADET2008_SECTOR=S11, NADET2008_PRICE=V, FREQUENCY=A", series.id);
        data = series.data.get();
        assertEquals(new TsDomain(TsFrequency.Yearly, 2009, 0, 5), data.getDomain());
        assertArrayEquals(new double[]{705.6, 815.8, 882.2, 960, 1199.4}, data.internalStorage(), 0);
    }

    @Test
    public void testGenericP1M() throws Exception {
        SdmxSource source = new GenericDocFactory().create(load("generic-P1M.xml"));
        assertEquals(3, source.items.size());

        SdmxSeries series;
        TsData data;

        series = (SdmxSeries) source.items.get(0);
        assertEquals(TimeFormat.P1M, series.timeFormat);
        assertEquals("CONSTRUCTION_PERMIT=PE, CONSTRUCTION_TYPE=NR, CONSTRUCTION_GEO=BE, CONSTRUCTION_ITEM=BU, FREQUENCY=M", series.id);
        data = series.data.get();
        assertEquals(new TsDomain(TsFrequency.Monthly, 2013, 6, 13), data.getDomain());
        assertEquals(428, data.get(0), 0);
        assertEquals(385, data.get(12), 0);
    }
}
