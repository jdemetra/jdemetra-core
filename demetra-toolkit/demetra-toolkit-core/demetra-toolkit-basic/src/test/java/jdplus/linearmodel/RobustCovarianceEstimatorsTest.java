/*
 * Copyright 2017 National Bank of Belgium
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

import jdplus.stats.linearmodel.LeastSquaresResults;
import jdplus.stats.linearmodel.RobustCovarianceEstimators;
import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.linearmodel.Ols;
import jdplus.data.DataBlock;
import demetra.data.DataSets;
import org.junit.jupiter.api.Test;
import demetra.data.DoubleSeq;
import jdplus.data.analysis.WindowFunction;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class RobustCovarianceEstimatorsTest {

    public RobustCovarianceEstimatorsTest() {
    }

    @Test
    public void testLongley() {
        double[] y = DataSets.Longley.y;

        LinearModel model = LinearModel.builder()
                .y(DoubleSeq.of(y))
                .meanCorrection(true)
                .addX(DoubleSeq.of(DataSets.Longley.x1))
                .addX(DoubleSeq.of(DataSets.Longley.x2))
                .addX(DoubleSeq.of(DataSets.Longley.x3))
                .addX(DoubleSeq.of(DataSets.Longley.x4))
                .addX(DoubleSeq.of(DataSets.Longley.x5))
                .addX(DoubleSeq.of(DataSets.Longley.x6))
                .build();

        LeastSquaresResults rslts = Ols.compute(model);
//        System.out.println(rslts.covariance());
        FastMatrix hac = RobustCovarianceEstimators.hac(model, rslts.getCoefficients(), WindowFunction.Bartlett, 5);
//        System.out.println(hac);
        DataBlock u = model.calcResiduals(rslts.getCoefficients());
        FastMatrix hc = RobustCovarianceEstimators.hc(model, rslts.getCoefficients(), i -> u.get(i));
//        System.out.println(hc);
    }

}
