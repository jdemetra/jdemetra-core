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
package ec.tstoolkit.ucarima;

import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.arima.AutoCovarianceFunction;
import ec.tstoolkit.arima.LinearModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaModelBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class WienerKolmogorovPreliminaryEstimatorPropertiesTest {

    public WienerKolmogorovPreliminaryEstimatorPropertiesTest() {
    }

    @Test
    public void testAirline() {
        UcarimaModel ucm = ucmAirline(-.6, -.8);
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator fe = wk.finalEstimator(0, true);
        WienerKolmogorovPreliminaryEstimatorProperties wkprop = new WienerKolmogorovPreliminaryEstimatorProperties();
        wkprop.setWienerKolmogorovEstimators(wk);
        wkprop.select(0, true);
        wkprop.setLag(0);
        // test the revision models
        for (int l = 0; l < 20; ++l) {
            LinearModel rm = wk.revisionModel(0, l);
            double se = Math.sqrt(rm.getInnovationVariance());
            for (int i = 0; i < 24; ++i) {
                assertTrue(Math.abs(fe.getModel().getFilter().getWeight(l + i + 1)
                        - rm.getFilter().getWeight(i) * se) < 1e-9);
            }
        }
    }

    @Test
    public void testErrors() {
        UcarimaModel ucm = ucmAirline(-.8, -.6);
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator fe = wk.finalEstimator(0, true);
        double[] evar = wk.totalErrorVariance(1, true, -12, 240);
        double[] rvar = wk.relativeRevisionVariance(1, true, -12, 240);
        double[] xvar = wk.revisionVariance(1, true, -12, 240);
        System.out.println("var");
        for (int l = 0; l < rvar.length; ++l) {
            System.out.print(rvar[l]);
            System.out.print('\t');
            System.out.println(xvar[l]);
        }
    }

    @Test
    public void testRevisions() {
        UcarimaModel ucm = ucmAirline(-.8, -.6);
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
        WienerKolmogorovEstimator fe = wk.finalEstimator(0, true);
        double[] xvar = wk.revisionVariance(1, true, 0, 60);
        System.out.println("revisions");
        for (int l = 0; l < xvar.length; ++l) {
            double[] evar = wk.revisionVariance(1, true, 60 - l-1, 1);
            System.out.print(evar[0]);
            System.out.print('\t');
            System.out.println(xvar[l]);
        }
    }

    @Test
    public void testVariations() {
        UcarimaModel ucm = ucmAirline(-.6, -.7);
        WienerKolmogorovEstimators wk = new WienerKolmogorovEstimators(ucm);
        System.out.println("variations revisions");
        double[] rv = wk.relativeRevisionVariance(1, true, 0, 101);
        for (int l = 0; l < 100; ++l) {
            System.out.print(rv[l + 1]);
            System.out.print('\t');
            System.out.print(wk.variationPrecision(1, l + 1, 1, true));
            System.out.print('\t');
            System.out.println(wk.variationRevisionVariance(1, 0, 1, l + 1));
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
