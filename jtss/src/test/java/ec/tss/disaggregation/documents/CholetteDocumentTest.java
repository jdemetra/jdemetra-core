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

package ec.tss.disaggregation.documents;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class CholetteDocumentTest {

    public CholetteDocumentTest() {
    }

//    @Test
    public void demo() {

        DataBlock D = new DataBlock(600);
        D.randomize(0);
        TsPeriod start = new TsPeriod(TsFrequency.Monthly, 1980, 0);
        TsData s = new TsData(start, D);
        TsData y = s.changeFrequency(TsFrequency.Yearly, TsAggregationType.Sum, true);
        Random rnd = new Random(1);
        for (int i = 0; i < y.getLength(); ++i) {
            y.set(i, y.get(i) + rnd.nextDouble() - .5);
        }
        Ts S = TsFactory.instance.createTs("original", null, s);
        Ts Y = TsFactory.instance.createTs("aggregated", null, y);
        CholetteDocument doc = new CholetteDocument();
        UniCholetteSpecification spec = new UniCholetteSpecification();
        doc.setInput(new Ts[]{S, Y});
        doc.setSpecification(spec);
        TsData bench = doc.getResults().getData(BenchmarkingResults.BENCHMARKED, TsData.class);

        TsData del = bench.changeFrequency(TsFrequency.Yearly, spec.getAggregationType(), true).minus(y);
        DescriptiveStatistics stat = new DescriptiveStatistics(del);
        assertTrue(Math.abs(stat.getAverage()) < 1e-3);
        assertTrue(stat.getStdev() < 1e-3);

    }
}