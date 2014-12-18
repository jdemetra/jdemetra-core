/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 * See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
package ec.tstoolkit.ssf;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import ec.tstoolkit.ssf.ucarima.SsfUcarima;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class FixedPointSmootherTest {

    public FixedPointSmootherTest() {
    }

//    @Test
    public void demoSomeMethod() {
        int fpos=120, nrev=60;
        SsfUcarima ssf = new SsfUcarima(ucmAirline(-.6, -.4));
        FixedPointSmoother fsm = new FixedPointSmoother(ssf, fpos);
        SsfNoData data = new SsfNoData(fpos+nrev);
        fsm.process(data);
        int spos = ssf.cmpPos(1);
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ssf.getUCModel());
        double[] wkv = wk.totalErrorVariance(1, true, 0, nrev);
        double[] wkrev = wk.relativeRevisionVariance(1, true, 0, nrev);
        double p0=fsm.P(0).get(spos, spos);
        for (int i = 0; i < nrev; ++i) {
            double p = fsm.P(i).get(spos, spos);
            System.out.print(p);
            System.out.print('\t');
            System.out.print(wkv[i]);
            System.out.print('\t');
             System.out.print(p0-p);
            System.out.print('\t');
            System.out.println(wkrev[i]);
        }
    }

    static UcarimaModel ucmAirline(double th, double bth) {
        SarimaModel sarima = new SarimaModelBuilder().createAirlineModel(12, th, bth);
        TrendCycleSelector tsel = new TrendCycleSelector();
        SeasonalSelector ssel = new SeasonalSelector(12);

        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(sarima));
        double var = ucm.setVarianceMax(-1, false);
        return ucm;
    }

}
