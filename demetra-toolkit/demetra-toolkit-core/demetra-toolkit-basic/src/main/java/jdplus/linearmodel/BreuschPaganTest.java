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
package jdplus.linearmodel;

import demetra.design.BuilderPattern;
import jdplus.dstats.Chi2;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.TestType;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class BreuschPaganTest {

    public StatisticalTest of(LeastSquaresResults lsr) {
        lsr.residuals();
        LinearModel lm = LinearModel.builder()
                .y(lsr.residuals().fastOp(z -> z * z))
                .addX(lsr.X())
                .meanCorrection(true)
                .build();
        LeastSquaresResults lsr2 = Ols.compute(lm);
        int n=lm.getObservationsCount(), p=lm.getXCount();
        double r2=lsr2.getR2();
        return new StatisticalTest(new Chi2(p), n * r2, TestType.Upper, true);
    }
}
