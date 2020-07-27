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
import demetra.eco.EcoException;
import demetra.math.Constants;
import demetra.revisions.parametric.Coefficient;
import demetra.revisions.parametric.OlsTest;
import demetra.revisions.parametric.RegressionTests;
import demetra.stats.TestResult;
import jdplus.data.DataBlock;
import jdplus.linearmodel.HeteroskedasticityTest;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.stats.tests.Arch;
import jdplus.stats.tests.JarqueBera;
import jdplus.stats.tests.StatisticalTest;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class OlsTestComputer {
    
    public OlsTest of(DoubleSeq y, DoubleSeq... x) {
        // Skip meaningless models
        double ny = y.ssq();
        if (ny < Constants.getEpsilon()) {
            return null;
        }
        OlsTest.Builder builder = OlsTest.builder();
        LinearModel lm = LinearModel.builder()
                .y(y)
                .meanCorrection(true)
                .addX(x)
                .build();
        try {
            LeastSquaresResults lsr = Ols.compute(lm);
            DoubleSeq coef = lsr.getCoefficients();
             DataBlock diag = lsr.covariance().diagonal();
            
            HeteroskedasticityTest bp = HeteroskedasticityTest.builder(lsr)
                    .type(HeteroskedasticityTest.Type.BreuschPagan)
                    .fisherTest(true);
            
            StatisticalTest bptest = bp.build();
            
            HeteroskedasticityTest w = HeteroskedasticityTest.builder(lsr)
                    .type(HeteroskedasticityTest.Type.White)
                    .fisherTest(false);
            
            StatisticalTest wtest = w.build();
            
            JarqueBera jb = new JarqueBera(lsr.residuals())
                    .correctionForSample()
                    .degreeOfFreedomCorrection(1);
            StatisticalTest jbtest = jb.build();
            
            Arch.Lm arch = Arch.lm(lsr.residuals());
            StatisticalTest artest = arch.build();
            
            RegressionTests.Builder tbuilder = RegressionTests.builder()
                    .jarqueBera(new TestResult(jbtest.getValue(), jbtest.getPValue(), "Jarque-Bera"))
                    .kurtosis(jb.getKurtosis())
                    .skewness(jb.getSkewness());
            
            if (bptest != null) {
                tbuilder.bpr2(bp.getLeastSquaresResultsOnSquaredResiduals().getR2())
                        .breuschPagan(new TestResult(bptest.getValue(), bptest.getPValue(), "Breusch-Pagan"));
            }
            if (wtest != null) {
                tbuilder.wr2(w.getLeastSquaresResultsOnSquaredResiduals().getR2())
                        .white(new TestResult(wtest.getValue(), wtest.getPValue(), "White"));
            }
            if (artest != null) {
                tbuilder.archr2(arch.getLeastSquaresResults().getR2())
                        .arch(new TestResult(artest.getValue(), artest.getPValue(), "Arch"));
            }
            
            Coefficient[] c = new Coefficient[1+x.length];
            for (int i=0; i<c.length; ++i){
                StatisticalTest t = lsr.Ttest(i);
                c[i]=new Coefficient(coef.get(i), Math.sqrt(diag.get(i)), t.getValue(), t.getPValue());
            };
            
            builder.R2(lsr.getR2())
                    .F(lsr.Ftest().getValue())
                    .n(lm.getObservationsCount())
                    .coefficients(c)
                    .diagnostics(tbuilder.build());
            
        } catch (EcoException err) {
        }
        
        return builder.build();
    }
    
}
