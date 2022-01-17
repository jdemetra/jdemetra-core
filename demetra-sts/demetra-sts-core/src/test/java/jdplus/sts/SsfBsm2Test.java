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
import jdplus.ssf.StateStorage;
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.akf.QRSmoother;
import jdplus.ssf.ckms.CkmsToolkit;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SsfBsm2Test {

    static final int N = 50000;

    static final SsfBsm2 BSM;

    static {
        BsmData model = new BsmData(BsmSpec.DEFAULT, 12);
        BSM = SsfBsm2.of(model);

    }

    public SsfBsm2Test() {
    }

    @Test
    public void testAggregate1() {
//        BsmSpec mspec = BsmSpec.builder()
//                .build();
//        //mspec.setSeasonalModel(SeasonalModel.Crude);
//        BsmData model = new BsmData(mspec, 12);
//        StateComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
//        StateComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
//        CompositeSsf composite = CompositeSsf.builder()
//                .add(t, LocalLinearTrend.defaultLoading())
//                .add(seas, SeasonalComponent.defaultLoading())
//                .measurementError(model.getVariance(Component.Noise))
//                .build();
//
//        SsfData data = new SsfData(Data.EXPORTS);
//        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
//        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
//        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testAggregate2() {
//        BsmSpec mspec = new BsmSpec();
//        mspec.setSeasonalModel(SeasonalModel.Crude);
//        BsmData model = new BsmData(mspec, 12);
//        StateComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
//        StateComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
//        CompositeSsf composite = CompositeSsf.builder()
//                .add(t, LocalLinearTrend.defaultLoading())
//                .add(seas, SeasonalComponent.defaultLoading())
//                .measurementError(model.getVariance(Component.Noise))
//                .build();
//
//        SsfData data = new SsfData(Data.EXPORTS);
//        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
//        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
//        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testAggregate3() {
//        BsmSpec mspec = new BsmSpec();
//        mspec.setSeasonalModel(SeasonalModel.Dummy);
//        BsmData model = new BsmData(mspec, 12);
//        StateComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
//        StateComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
//        CompositeSsf composite = CompositeSsf.builder()
//                .add(t, LocalLinearTrend.defaultLoading())
//                .add(seas, SeasonalComponent.defaultLoading())
//                .measurementError(model.getVariance(Component.Noise))
//                .build();
//
//        SsfData data = new SsfData(Data.EXPORTS);
//        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
//        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
//        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testAggregate4() {
//        BsmSpec mspec = new BsmSpec();
//        mspec.setSeasonalModel(SeasonalModel.HarrisonStevens);
//        BsmData model = new BsmData(mspec, 12);
//        StateComponent t = LocalLinearTrend.of(model.getVariance(Component.Level), model.getVariance(Component.Slope));
//        StateComponent seas = SeasonalComponent.of(model.specification().getSeasonalModel(), 12, model.getVariance(Component.Seasonal));
//        CompositeSsf composite = CompositeSsf.builder()
//                .add(t, LocalLinearTrend.defaultLoading())
//                .add(seas, SeasonalComponent.defaultLoading())
//                .measurementError(model.getVariance(Component.Noise))
//                .build();
//
//        SsfData data = new SsfData(Data.EXPORTS);
//        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(composite, data);
//        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(SsfBsm2.of(model), data);
//        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    public void testLikelihood() {
        SsfData data = new SsfData(Data.EXPORTS);
        DiffuseLikelihood ll = DkToolkit.likelihoodComputer(true, true, true).compute(BSM, data);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer(true).compute(BSM, data);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer(true, true, true).compute(BSM, data);
        DiffuseLikelihood ll4 = AkfToolkit.robustLikelihoodComputer(true, true).compute(BSM, data);
        assertEquals(ll.logLikelihood(), ll2.logLikelihood(), 1e-6);
        assertEquals(ll.logLikelihood(), ll3.logLikelihood(), 1e-6);
        assertEquals(ll.logLikelihood(), ll4.logLikelihood(), 1e-6);
    }

    @Test
    public void testSmoothing() {
        SsfData data = new SsfData(Data.EXPORTS);
        QRSmoother smoother=new QRSmoother();
        StateStorage rslt = smoother.process(BSM, data, true);
        System.out.println(rslt.getComponent(0));
        System.out.println(rslt.getComponentVariance(0));
        DefaultSmoothingResults fs = DkToolkit.smooth(BSM, data, true, true);
        System.out.println(fs.getComponent(0));
        System.out.println(fs.getComponentVariance(0));
    }

    public static void stressTestBsm() {
        SsfData data = new SsfData(Data.EXPORTS);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer(true, true, false).compute(BSM, data);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("dk filter (sqr)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer(false, true, false).compute(BSM, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("dk filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            AkfToolkit.likelihoodComputer(true, true, false).compute(BSM, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("akf filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            AkfToolkit.robustLikelihoodComputer(true, false).compute(BSM, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("robust akf filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            CkmsToolkit.likelihoodComputer(true).compute(BSM, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("ckms filter");
        System.out.println(t1 - t0);


    }

    public static void main(String[] args) {
        stressTestBsm();
    }
}
