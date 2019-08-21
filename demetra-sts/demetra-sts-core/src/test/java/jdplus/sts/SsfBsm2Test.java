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
package jdplus.sts;

import demetra.data.Data;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.SeasonalModel;
import jdplus.ssf.SsfComponent;
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.ckms.CkmsToolkit;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.implementations.CompositeSsf;
import jdplus.ssf.univariate.SsfData;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Jean Palate
 */
public class SsfBsm2Test {

    static final int N = 50000;

    final SsfBsm2 bsm;

    public SsfBsm2Test() {
        BsmSpec mspec = new BsmSpec();
        //mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        bsm = SsfBsm2.of(model);
    }

    @Test
    public void testAggregate1() {
        BsmSpec mspec = new BsmSpec();
        //mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        SsfComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
        SsfComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
        CompositeSsf composite = CompositeSsf.builder()
                .add(t)
                .add(seas)
                .measurementError(model.getVariance(Component.Noise))
                .build();

        SsfData data = new SsfData(Data.EXPORTS);
        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testAggregate2() {
        BsmSpec mspec = new BsmSpec();
        mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        SsfComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
        SsfComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
        CompositeSsf composite = CompositeSsf.builder()
                .add(t)
                .add(seas)
                .measurementError(model.getVariance(Component.Noise))
                .build();

        SsfData data = new SsfData(Data.EXPORTS);
        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testAggregate3() {
        BsmSpec mspec = new BsmSpec();
        mspec.setSeasonalModel(SeasonalModel.Dummy);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        SsfComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
        SsfComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
        CompositeSsf composite = CompositeSsf.builder()
                .add(t)
                .add(seas)
                .measurementError(model.getVariance(Component.Noise))
                .build();

        SsfData data = new SsfData(Data.EXPORTS);
        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testAggregate4() {
        BsmSpec mspec = new BsmSpec();
        mspec.setSeasonalModel(SeasonalModel.HarrisonStevens);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        SsfComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
        SsfComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
        CompositeSsf composite = CompositeSsf.builder()
                .add(t)
                .add(seas)
                .measurementError(model.getVariance(Component.Noise))
                .build();

        SsfData data = new SsfData(Data.EXPORTS);
        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testLikelihood() {
        SsfData data = new SsfData(Data.EXPORTS);
        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(bsm, data);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(bsm, data);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer(true, true, true).compute(bsm, data);
        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
        assertEquals(ll.logLikelihood(), ll3.logLikelihood(), 1e-6);
    }

    @Test
    @Ignore
    public void stressTestBsm() {
        SsfData data = new SsfData(Data.EXPORTS);
        testLikelihood();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer(true, true, false).compute(bsm, data);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("dk filter (sqr)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer(false, true, false).compute(bsm, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("dk filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            AkfToolkit.likelihoodComputer(true, true, false).compute(bsm, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("akf filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            CkmsToolkit.likelihoodComputer(true).compute(bsm, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("ckms filter");
        System.out.println(t1 - t0);

        BsmSpec mspec = new BsmSpec();
        //mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        SsfComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
        SsfComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
        CompositeSsf composite = CompositeSsf.builder()
                .add(t)
                .add(seas)
                .measurementError(model.getVariance(Component.Noise))
                .build();
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            CkmsToolkit.likelihoodComputer(true).compute(composite, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("ckms filter / composite");
        System.out.println(t1 - t0);

    }
}
