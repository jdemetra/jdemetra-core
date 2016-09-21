/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
package ec.tstoolkit.structural;

import ec.tstoolkit.eco.DiffuseLikelihood;
import ec.tstoolkit.ssf.DiffusePredictionErrorDecomposition;
import ec.tstoolkit.ssf.Filter;
import ec.tstoolkit.ssf.LikelihoodEvaluation;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.arima.SsfArima;
import ec.tstoolkit.ucarima.UcarimaModel;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class BsmMonitorTest {

    public BsmMonitorTest() {
    }

    @Test
    public void testBsm() {
        BsmMonitor monitor = new BsmMonitor();
        ModelSpecification mspec = new ModelSpecification();
        mspec.lUse = ComponentUse.Free;
        mspec.sUse = ComponentUse.Free;
        SeasonalModel[] models = new SeasonalModel[]{SeasonalModel.Crude, SeasonalModel.Dummy,
            SeasonalModel.HarrisonStevens, SeasonalModel.Trigonometric};
        double[] y = data.Data.P.internalStorage();
        for (int i = 0; i < models.length; ++i) {
            mspec.seasModel = models[i];
            monitor.setSpecification(mspec);
            boolean ok = monitor.process(y, 12);
            BasicStructuralModel model = monitor.getResult();
            UcarimaModel ucm = model.computeReducedModel(true);
            double ll1 = monitor.getLikelihood().getUncorrectedLogLikelihood();

            Filter filter = new Filter();
            filter.setSsf(new SsfArima(ucm.sum()));
            DiffusePredictionErrorDecomposition dp = new DiffusePredictionErrorDecomposition(false);
            filter.process(new SsfData(y, null), dp);
            DiffuseLikelihood ll = new DiffuseLikelihood();
            LikelihoodEvaluation.evaluate(dp, ll);
            double ll2 = ll.getUncorrectedLogLikelihood();
            assertTrue(Math.abs(ll2 - ll1) < 1e-3);
        }
    }

    @Test
    public void testCycle() {
        BsmMonitor monitor = new BsmMonitor();
        ModelSpecification mspec = new ModelSpecification();
        mspec.cUse = ComponentUse.Free;
        mspec.lUse = ComponentUse.Fixed;
        mspec.sUse = ComponentUse.Free;
        SeasonalModel[] models = new SeasonalModel[]{SeasonalModel.Crude, SeasonalModel.Dummy,
            SeasonalModel.HarrisonStevens, SeasonalModel.Trigonometric};
        double[] y = data.Data.P.internalStorage();
        for (int i = 0; i < models.length; ++i) {
            mspec.seasModel = models[i];
            monitor.setSpecification(mspec);
            boolean ok = monitor.process(y, 12);
            BasicStructuralModel model = monitor.getResult();
            UcarimaModel ucm = model.computeReducedModel(true);
            double ll1 = monitor.getLikelihood().getUncorrectedLogLikelihood();

            Filter filter = new Filter();
            filter.setSsf(new SsfArima(ucm.sum()));
            DiffusePredictionErrorDecomposition dp = new DiffusePredictionErrorDecomposition(false);
            filter.process(new SsfData(y, null), dp);
            DiffuseLikelihood ll = new DiffuseLikelihood();
            LikelihoodEvaluation.evaluate(dp, ll);
            double ll2 = ll.getUncorrectedLogLikelihood();
            assertTrue(Math.abs(ll2 - ll1) < 1e-3);
        }
    }

    @Test
    @Ignore
    public void testNile() {
        ModelSpecification mspec = new ModelSpecification();
        mspec.seasModel = SeasonalModel.Unused;
        double[] y = data.Data.Nile.internalStorage();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            BsmMonitor monitor = new BsmMonitor();
            monitor.setPrecision(1e-6);
            monitor.setSpecification(mspec);
            boolean ok = monitor.process(y, 1);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }
}
