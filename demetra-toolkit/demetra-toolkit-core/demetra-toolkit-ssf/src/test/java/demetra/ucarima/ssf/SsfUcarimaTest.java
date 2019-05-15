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
package demetra.ucarima.ssf;

import demetra.data.Data;
import jd.data.DataBlockStorage;
import demetra.sarima.SarimaModel;
import demetra.arima.SarimaSpecification;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.implementations.CompositeSsf;
import demetra.ssf.univariate.DefaultSmoothingResults;
import demetra.ssf.univariate.SsfData;
import demetra.ucarima.ModelDecomposer;
import demetra.ucarima.SeasonalSelector;
import demetra.ucarima.TrendCycleSelector;
import demetra.ucarima.UcarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SsfUcarimaTest {

    public SsfUcarimaTest() {
    }

    @Test
    public void testDkSmoother() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        ucm = ucm.simplify();
        CompositeSsf ssf = SsfUcarima.of(ucm);
        SsfData data = new SsfData(Data.PROD);
        DefaultSmoothingResults sd = DkToolkit.sqrtSmooth(ssf, data, true, true);
        DataBlockStorage ds = DkToolkit.fastSmooth(ssf, data);
        int[] pos = ssf.componentsPosition();
        for (int i = 0; i < 3; ++i) {
            System.out.println(sd.getComponent(pos[i]));
            assertTrue(ds.item(pos[i]).distance(sd.getComponent(pos[i])) < 1e-9);
        }
        System.out.println(sd.getComponentVariance(0));
    }

    public static UcarimaModel ucmAirline(double th, double bth) {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.airline(true);
        SarimaModel sarima = SarimaModel.builder(spec)
                .theta(1, th)
                .btheta(1, bth)
                .build();

        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(sarima);
        ucm = ucm.setVarianceMax(-1, false);
        return ucm;
    }
}
