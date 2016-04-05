/*
 * Copyright 2013-2014 National Bank of Belgium
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
package ec.tstoolkit.arima;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.ucarima.ModelDecomposer;
import ec.tstoolkit.ucarima.SeasonalSelector;
import ec.tstoolkit.ucarima.TrendCycleSelector;
import ec.tstoolkit.ucarima.UcarimaModel;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class SpectrumTest {

    public SpectrumTest() {
    }

    @Test
    public void testMinimize() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setP(3);
        spec.setD(1);
        spec.setBP(1);
        spec.setBD(1);
        spec.setBQ(1);
        SarimaModel arima = new SarimaModel(spec);
        double[] p = new double[]{.308, .0588, -0.2787, -0.5769, -.95};
        arima.setParameters(new DataBlock(p));
        Spectrum spectrum = arima.getSpectrum();
        Spectrum.Minimizer min = new Spectrum.Minimizer();
        Spectrum.Minimizer min2 = new Spectrum.Minimizer();
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 10000; ++i) {
        min.minimize(spectrum);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < 10000; ++i) {
        min2.minimize2(spectrum);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        System.out.println(min.getMinimum());
//        System.out.println(min2.getMinimum());
//        System.out.println(min.getMinimumFrequency());
//        System.out.println(min2.getMinimumFrequency());
        //           assertTrue(Math.abs(min.getMinimum()-min2.getMinimum())<1e-7);
        // Usual decomposers (Trend, Seasonal)
        TrendCycleSelector tsel = new TrendCycleSelector(.5);
        SeasonalSelector ssel = new SeasonalSelector(arima.getSpecification().getFrequency(), 3);
        ssel.setK(.5);
        ModelDecomposer decomposer = new ModelDecomposer();
        decomposer.add(tsel);
        decomposer.add(ssel);

        UcarimaModel ucm = decomposer.decompose(ArimaModel.create(arima));
        UcarimaModel ucm2 = decomposer.decompose(ArimaModel.create(arima));
        ArimaModel cmp = ucm.getComponent(1);
        min.minimize(cmp.getSpectrum());
        min2.minimize2(cmp.getSpectrum());
        // Canonical decomposition
        double var = ucm.setVarianceMax(-1, true, true);
        double var2 = ucm2.setVarianceMax(-1, true, false);
//        System.out.println(ucm.getComponent(0));
//        System.out.println(ucm2.getComponent(0));
//        System.out.println(ucm.getComponent(1));
//        System.out.println(ucm2.getComponent(1));
//        System.out.println(ucm.getComponent(2));
//        System.out.println(ucm2.getComponent(2));
//        System.out.println(ucm.getComponent(3));
//        System.out.println(ucm2.getComponent(3));
    }

    @Test
    public void testMinimize2() {
        SarimaSpecification spec = new SarimaSpecification(12);
        spec.setP(3);
        spec.setD(1);
        spec.setBP(1);
        spec.setBD(1);
        spec.setBQ(1);
        SarimaModel arima = new SarimaModel(spec);
        double[] p = new double[]{.308, .0588, -0.2787, -0.5769, -.95};
        arima.setParameters(new DataBlock(p));
        Spectrum spectrum = arima.getSpectrum();
        Spectrum.Minimizer min = new Spectrum.Minimizer();
        Spectrum.Minimizer min1 = new Spectrum.Minimizer();
        Spectrum.Minimizer min2 = new Spectrum.Minimizer();
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; ++i) {
            min.minimize(spectrum);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; ++i) {
            min1.minimize1(spectrum);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; ++i) {
            min2.minimize2(spectrum);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        System.out.println(min.getMinimum());
//        System.out.println(min1.getMinimum());
//        System.out.println(min2.getMinimum());
//        System.out.println(min.getMinimumFrequency());
//        System.out.println(min1.getMinimumFrequency());
//        System.out.println(min2.getMinimumFrequency());
        assertTrue(Math.abs(min.getMinimum() - min2.getMinimum()) < 1e-7);
        assertTrue(Math.abs(min.getMinimum() - min1.getMinimum()) < 1e-7);
    }
}
