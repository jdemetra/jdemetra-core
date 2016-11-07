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
package ec.benchmarking.simplets;

import data.Data;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TsCholetteTest {

    public TsCholetteTest() {
    }

    @Test
    public void testFullYears() {
        TsData x = addNoise(Data.P);
        TsData y = x.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        // add some noise to y
        TsCholette cholette = new TsCholette();
        TsData xc = cholette.process(x, y);
        TsData yc = xc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        assertTrue(y.distance(yc) < 1e-9);
    }

    @Test
    public void testPartialYears() {
        TsData x = Data.X.drop(5, 8);
        TsData y = x.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        x=addNoise(x);
        // add some noise to y
        TsCholette cholette = new TsCholette();
        TsData xc = cholette.process(x, y);
        TsData yc = xc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        assertTrue(y.distance(yc) < 1e-9);
        cholette.setRho(1);
        xc = cholette.process(x, y);
        yc = xc.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        assertTrue(y.distance(yc) < 1e-9);
//        TsDataTable table=new TsDataTable();
//        table.add(x, xc, y, yc);
//        System.out.println(table);
    }

    private static TsData addNoise(TsData s) {

        Random rnd = new Random();
        TsData snoisy = new TsData(s.getDomain());
        for (int i = 0; i < s.getLength(); ++i) {
            double x = s.get(i);
            snoisy.set(i, x * (1 + 0.1 * rnd.nextGaussian()));
        }
        return snoisy;
    }

}
