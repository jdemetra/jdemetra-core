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
package demetra.sts;

import data.Data;
import demetra.ssf.akf.AkfToolkit;
import demetra.ssf.akf.DiffuseLikelihood;
import demetra.ssf.ckms.CkmsToolkit;
import demetra.ssf.dk.DkLikelihood;
import demetra.ssf.dk.DkToolkit;
import demetra.ssf.univariate.SsfData;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Jean Palate
 */
public class SsfBsmTest {

    static final int N = 50000;

    final SsfBsm bsm;

    public SsfBsmTest() {
        ModelSpecification mspec = new ModelSpecification();
        mspec.setSeasonalModel(SeasonalModel.Crude);
        BasicStructuralModel model = new BasicStructuralModel(mspec, 12);
        bsm = SsfBsm.of(model);
    }

    @Test
    public void testLikelihood() {
        SsfData data = new SsfData(Data.X);
        DkLikelihood ll1 = DkToolkit.likelihoodComputer(true, true).compute(bsm, data);
//        System.out.println(ll);
        DkLikelihood ll2 = CkmsToolkit.likelihoodComputer().compute(bsm, data);
//        System.out.println(ll2);
        DiffuseLikelihood ll3 = AkfToolkit.likelihoodComputer(true).compute(bsm, data);
//        System.out.println(ll3);
        assertEquals(ll1.logLikelihood(), ll3.logLikelihood(), 1e-6);
        assertEquals(ll1.logLikelihood(), ll2.logLikelihood(), 1e-6);
    }

    @Test
    @Ignore
    public void stressTestBsm() {
        SsfData data = new SsfData(Data.X);
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
