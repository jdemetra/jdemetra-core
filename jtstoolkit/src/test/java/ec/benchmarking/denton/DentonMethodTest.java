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
package ec.benchmarking.denton;

import ec.benchmarking.simplets.TsDenton;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class DentonMethodTest {

    public final TsData y, m, ym;

    public DentonMethodTest() {
        m = data.Data.X;
        ym = m.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        y = ym.clone();
        for (int i = 0; i < y.getLength(); ++i) {
            y.set(i, y.get(i) * (1 + 0.01 * i));
        }
    }

    @Test
    public void testSomeMethod() {
        DentonMethod denton = new DentonMethod();
        double[] process = denton.process(m, y);
        TsData mc0 = new TsData(m.getStart(), process, false);
         TsDataTable table = new TsDataTable();
         table.insert(-1, mc0);
       TsDenton mdenton = new TsDenton();
        mdenton.setMultiplicative(true);
        TsData mc = mdenton.process(m, y);
        assertTrue(mc0.distance(mc) < 1e-6);
        denton.setMultiplicative(false);
        process = denton.process(m, y);
        mc0 = new TsData(m.getStart(), process, false);
        mdenton.setMultiplicative(false);
        mc = mdenton.process(m, y);
        assertTrue(mc0.distance(mc) < 1e-6);
        denton.setMultiplicative(true);
        denton.setDifferencingOrder(2);
        process = denton.process(m, y);
        table.insert(-1, new TsData(m.getStart(), process, false));
         denton.setDifferencingOrder(3);
        process = denton.process(m, y);
        table.insert(-1, new TsData(m.getStart(), process, false));
       denton.setModifiedDenton(false);
        denton.setDifferencingOrder(1);
        process = denton.process(m, y);
        table.insert(-1, new TsData(m.getStart(), process, false));
        denton.setDifferencingOrder(2);
        process = denton.process(m, y);
        table.insert(-1, new TsData(m.getStart(), process, false));
        denton.setDifferencingOrder(3);
        process = denton.process(m, y);
        table.insert(-1, new TsData(m.getStart(), process, false));
        System.out.println(table);
    }

}
