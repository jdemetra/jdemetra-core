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
import demetra.math.Constants;
import demetra.revisions.parametric.SignalNoise;
import demetra.stats.ProbabilityType;
import jdplus.dstats.F;
import jdplus.stats.linearmodel.LeastSquaresResults;
import jdplus.stats.linearmodel.LinearModel;
import jdplus.stats.linearmodel.Ols;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SignalNoiseComputer {

    public SignalNoise of(DoubleSeq p, DoubleSeq l) {
        DoubleSeq r=DoublesMath.subtract(l, p);
        // Skip meaningless models
        double nr = r.ssq();
        if (nr < Constants.getEpsilon()) {
            return null;
        }
        int n=r.length();
        SignalNoise.Builder builder = SignalNoise.builder();
        LinearModel lm = LinearModel.builder()
                .y(r)
                .meanCorrection(true)
                .addX(l)
                .build();
        try {
            LeastSquaresResults lsr = Ols.compute(lm);
            F f=new F(2, n-2);
            double fval=lsr.getR2()*n;
            builder.newsR2(lsr.getR2())
                    .newsF(fval)
                    .newsPvalue(f.getProbability(fval, ProbabilityType.Upper));
        }catch (Exception err){
        }
        lm = LinearModel.builder()
                .y(r)
                .meanCorrection(true)
                .addX(p)
                .build();
        try {
            LeastSquaresResults lsr = Ols.compute(lm);
            F f=new F(2, n-2);
            double fval=lsr.getR2()*n;
            builder.noiseR2(lsr.getR2())
                    .noiseF(fval)
                    .noisePvalue(f.getProbability(fval, ProbabilityType.Upper));
        }catch (Exception err){
            
        }
        return builder.build();
    }
   
}
