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

import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.linearmodel.Ols;
import jdplus.stats.linearmodel.LeastSquaresResults;
import java.util.Random;
import java.util.function.DoubleSupplier;

import jdplus.data.DataBlock;
import jdplus.math.matrices.FastMatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author palatej
 */
public class LeastSquaresResultsTest {
    
    public LeastSquaresResultsTest() {
    }

    @Test
    public void testEqualities() {
        int N=200;
        DataBlock y=DataBlock.make(N);
        Random rnd=new Random(0);
        y.set((DoubleSupplier)rnd::nextDouble);
        FastMatrix X=FastMatrix.make(N, 5);
        X.set((a,b)->rnd.nextDouble());
        
        LinearModel lm = LinearModel.builder()
                .y(y)
                .addX(X)
                .meanCorrection(true)
                .build();
        
        LeastSquaresResults lsr = Ols.compute(lm);
        
        // SST = SSE + SSR
        assertEquals(lsr.getModelSumOfSquares()+lsr.getErrorSumOfSquares(), lsr.getTotalSumOfSquares(), 1e-9);
        // R2 = SSR/SST
        assertEquals(lsr.getR2(), lsr.getModelSumOfSquares()/lsr.getTotalSumOfSquares(),1e-9);
        // F = MSR/MSE
        assertEquals(lsr.Ftest().getValue(), lsr.getModelMeanSquares()/lsr.getErrorMeanSquares(),1e-9);
        // K2 = SSR/MSE
        double khi2=lsr.getModelSumOfSquares()/lsr.getErrorMeanSquares();
        // khi2(5), mean=5
        assertTrue(khi2>2 && khi2<15);
        assertEquals(lsr.Ftest().getPvalue(), lsr.Khi2Test().getPvalue(), 0.2);
    }
    
}
