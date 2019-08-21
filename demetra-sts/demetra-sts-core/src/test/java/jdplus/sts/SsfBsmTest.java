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
import jdplus.ssf.akf.AkfToolkit;
import jdplus.ssf.ckms.CkmsToolkit;
import jdplus.ssf.likelihood.DiffuseLikelihood;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.SsfData;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SsfBsmTest {

    static final int N = 50000;

    final SsfBsm2 bsm;

    public SsfBsmTest() {
        BsmSpec mspec = new BsmSpec();
        mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        model.setVariance(Component.Seasonal, 100);
        bsm = SsfBsm2.of(model);
    }

    @Test
    public void testLikelihood() {
        SsfData data = new SsfData(Data.PROD);
        DiffuseLikelihood ll1 = DkToolkit.likelihoodComputer(true, true).compute(bsm, data);
        System.out.println(ll1);
        DiffuseLikelihood ll2 = CkmsToolkit.likelihoodComputer().compute(bsm, data);
//        System.out.println(ll2);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer(true, true).compute(bsm, data);
//        System.out.println(ll3);
        assertEquals(ll1.logLikelihood(), ll3.logLikelihood(), 1e-6);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    @Ignore
    public void stressTestBsm() {
        SsfData data = new SsfData(Data.EXPORTS);
        testLikelihood();
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer().compute(bsm, data);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("dk filter (sqr)");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            DkToolkit.likelihoodComputer(false, false).compute(bsm, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("dk filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            AkfToolkit.likelihoodComputer(true).compute(bsm, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("akf filter");
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            CkmsToolkit.likelihoodComputer().compute(bsm, data);
        }
        t1 = System.currentTimeMillis();
        System.out.println("ckms filter");
        System.out.println(t1 - t0);
    }
}
