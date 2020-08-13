/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.revisions.parametric;

import demetra.data.DoubleSeq;
import demetra.data.DoublesMath;
import demetra.revisions.parametric.AutoCorrelationTests;
import demetra.stats.TestResult;
import jdplus.linearmodel.BreuschGodfrey;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.stats.tests.LjungBox;
import jdplus.stats.tests.StatisticalTest;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class AutoCorrelationTestsComputer {

    public AutoCorrelationTests of(DoubleSeq v0, DoubleSeq v1, int nbg, int nlb) {

        LinearModel lm = LinearModel.builder()
                .y(v0)
                .addX(v1)
                .meanCorrection(true)
                .build();

        LeastSquaresResults lsr = Ols.compute(lm);
        
//        Simulation of the bug in Eurostat version.
//        Two problems: 
//        1. BG is not correctly specified  
//        2. Lag is missleading in JD+ 2.x (changed in 3.0)
//
//        DoubleSeq u = lsr.residuals();
//        
//        LinearModel.Builder lmb=LinearModel.builder()
//                .y(v0.drop(0, nbg))
//                .meanCorrection(true);
//        for (int i=nbg-1; i>=0; --i){
//            lmb.addX(u.drop(nbg-i, i));
//        }
//        LeastSquaresResults lsr2 = Ols.compute(lmb.build());

        BreuschGodfrey bg = new BreuschGodfrey(lsr)
                .lag(nbg);
        StatisticalTest bgtest = bg.build();

        DoubleSeq dv0 = DoublesMath.delta(v0, 1), dv1=DoublesMath.delta(v1, 1);
        double m0=dv0.average(), m1=dv1.average();
        dv0=dv0.fn(z->z-m0);
        dv1=dv1.fn(z->z-m1);
        DoubleSeq delta=DoublesMath.subtract(dv0, dv1);
        StatisticalTest lbtest = new LjungBox(delta)
//                .lag(freq) used in ra (Eurostat) !!? 
                .lag(1)
                .autoCorrelationsCount(nlb)
                .build();

//        AutoCorrelationTests.Builder builder = AutoCorrelationTests.builder()
//                .bgr2(lsr2.getR2())
//                .breuschGodfrey(new TestResult(0, 0, "Breusch-Godfrey"));
        
        
        AutoCorrelationTests.Builder builder = AutoCorrelationTests.builder();
        if (bgtest != null) {
            builder.bgr2(bg.getLeastSquaresResults().getR2())
                    .breuschGodfrey(new TestResult(bgtest.getValue(), bgtest.getPValue(), "Breusch-Godfrey"));
        }
        if (lbtest != null) {
            builder.ljungBox(new TestResult(lbtest.getValue(), lbtest.getPValue(), "Ljung-Box"));
        }
        return builder.build();
    }
}
